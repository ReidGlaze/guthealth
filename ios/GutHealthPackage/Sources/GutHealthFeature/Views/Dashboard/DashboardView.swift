import SwiftUI
import UIKit

public struct DashboardView: View {
    @State private var selectedDate: Date = Calendar.current.startOfDay(for: Date())
    @State private var meals: [Meal] = []
    @State private var symptoms: [Symptom] = []
    @State private var poopLogs: [PoopLog] = []
    @State private var showSettings = false
    @State private var streakDays: Int = 0
    @State private var allRecentMeals: [Meal] = []
    @State private var allRecentSymptoms: [Symptom] = []
    @State private var allRecentPoopLogs: [PoopLog] = []
    // Delete confirmation
    @State private var mealToDelete: Meal? = nil
    @State private var symptomToDelete: Symptom? = nil
    @State private var poopLogToDelete: PoopLog? = nil
    // Poop photo reveal state
    @State private var revealedPoopPhotoIds: Set<String> = []

    private let authService = AuthService.shared

    private var isToday: Bool {
        Calendar.current.isDateInToday(selectedDate)
    }

    public init() {}

    public var body: some View {
        NavigationStack {
            ScrollView {
                VStack(spacing: AppSpacing.lg) {
                    dateNavigationBar
                    if isToday {
                        streakCard
                        checklistCard
                    }
                    mealsSection
                    symptomsSection
                    poopLogsSection
                }
                .padding(.horizontal, AppSpacing.md)
                .padding(.top, AppSpacing.sm)
            }
            .background(AppColors.background)
            .navigationTitle("Dashboard")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) {
                    Button {
                        showSettings = true
                    } label: {
                        Image(systemName: "gearshape.fill")
                            .foregroundColor(AppColors.primary)
                    }
                }
            }
            .sheet(isPresented: $showSettings) {
                SettingsView()
            }
            .task { await loadData() }
            .refreshable { await loadData() }
            .confirmationDialog("Delete Meal?", isPresented: Binding(
                get: { mealToDelete != nil },
                set: { if !$0 { mealToDelete = nil } }
            ), titleVisibility: .visible) {
                Button("Delete", role: .destructive) {
                    Task { await deleteMealConfirmed() }
                }
                Button("Cancel", role: .cancel) { mealToDelete = nil }
            } message: {
                Text("This entry will be permanently removed.")
            }
            .confirmationDialog("Delete Symptom?", isPresented: Binding(
                get: { symptomToDelete != nil },
                set: { if !$0 { symptomToDelete = nil } }
            ), titleVisibility: .visible) {
                Button("Delete", role: .destructive) {
                    Task { await deleteSymptomConfirmed() }
                }
                Button("Cancel", role: .cancel) { symptomToDelete = nil }
            } message: {
                Text("This entry will be permanently removed.")
            }
            .confirmationDialog("Delete Poop Log?", isPresented: Binding(
                get: { poopLogToDelete != nil },
                set: { if !$0 { poopLogToDelete = nil } }
            ), titleVisibility: .visible) {
                Button("Delete", role: .destructive) {
                    Task { await deletePoopLogConfirmed() }
                }
                Button("Cancel", role: .cancel) { poopLogToDelete = nil }
            } message: {
                Text("This entry will be permanently removed.")
            }
        }
    }

    // MARK: - Date Navigation Bar

    private var dateNavigationBar: some View {
        HStack(spacing: AppSpacing.md) {
            Button {
                withAnimation(.easeInOut(duration: 0.2)) {
                    selectedDate = Calendar.current.date(byAdding: .day, value: -1, to: selectedDate) ?? selectedDate
                }
                Task { await loadEntriesForDate() }
            } label: {
                Image(systemName: "chevron.left")
                    .font(.system(size: 16, weight: .semibold))
                    .foregroundColor(AppColors.primary)
                    .frame(width: 36, height: 36)
                    .background(AppColors.primaryContainer)
                    .cornerRadius(AppRadius.md)
            }

            Spacer()

            Button {
                // Tapping the date label could open a date picker in a future iteration.
                // For now it's a non-interactive label.
            } label: {
                Text(dateLabel)
                    .font(AppTypography.headline)
                    .foregroundColor(AppColors.text)
            }
            .buttonStyle(.plain)

            Spacer()

            Button {
                withAnimation(.easeInOut(duration: 0.2)) {
                    selectedDate = Calendar.current.date(byAdding: .day, value: 1, to: selectedDate) ?? selectedDate
                }
                Task { await loadEntriesForDate() }
            } label: {
                Image(systemName: "chevron.right")
                    .font(.system(size: 16, weight: .semibold))
                    .foregroundColor(isToday ? AppColors.textTertiary : AppColors.primary)
                    .frame(width: 36, height: 36)
                    .background(isToday ? AppColors.border.opacity(0.5) : AppColors.primaryContainer)
                    .cornerRadius(AppRadius.md)
            }
            .disabled(isToday)
        }
        .padding(.horizontal, AppSpacing.xs)
    }

    private var dateLabel: String {
        if isToday { return "Today" }
        let calendar = Calendar.current
        if calendar.isDateInYesterday(selectedDate) { return "Yesterday" }
        let formatter = DateFormatter()
        let currentYear = calendar.component(.year, from: Date())
        let selectedYear = calendar.component(.year, from: selectedDate)
        formatter.dateFormat = selectedYear == currentYear ? "EEEE, MMM d" : "EEEE, MMM d, yyyy"
        return formatter.string(from: selectedDate)
    }

    // MARK: - Streak Card (today only)

    private var streakNextMilestone: Int {
        streakDays < 7 ? 7 : (streakDays < 14 ? 14 : 21)
    }

    private var streakProgress: Double {
        min(1.0, Double(streakDays) / Double(streakNextMilestone))
    }

    private var streakCard: some View {
        VStack(alignment: .leading, spacing: AppSpacing.sm) {
            HStack(spacing: AppSpacing.md) {
                Image(systemName: "flame.fill")
                    .font(.title2)
                    .foregroundColor(streakDays >= 7 ? AppColors.success : AppColors.warning)
                VStack(alignment: .leading, spacing: 2) {
                    Text("Logging Streak")
                        .font(AppTypography.headline)
                        .foregroundColor(AppColors.text)
                    Text(streakMessage)
                        .font(AppTypography.footnote)
                        .foregroundColor(streakDays >= 7 ? AppColors.success : AppColors.warning)
                }
                Spacer()
                Text("\(streakDays)d")
                    .font(AppTypography.title2)
                    .foregroundColor(streakDays >= 7 ? AppColors.success : AppColors.warning)
            }
            GeometryReader { geo in
                ZStack(alignment: .leading) {
                    RoundedRectangle(cornerRadius: 4)
                        .fill(AppColors.border)
                        .frame(height: 8)
                    RoundedRectangle(cornerRadius: 4)
                        .fill(streakDays >= 7 ? AppColors.success : AppColors.warning)
                        .frame(width: geo.size.width * streakProgress, height: 8)
                }
            }
            .frame(height: 8)
            HStack {
                Text("Next milestone: Day \(streakNextMilestone)")
                    .font(AppTypography.caption1)
                    .foregroundColor(AppColors.textTertiary)
                Spacer()
                Text("\(streakDays)/\(streakNextMilestone)")
                    .font(AppTypography.caption1)
                    .foregroundColor(AppColors.textTertiary)
            }
        }
        .padding(AppSpacing.md)
        .frame(maxWidth: .infinity)
        .cardStyle()
    }

    private var streakMessage: String {
        switch streakDays {
        case 0: return "Start your streak! Log your first entry today."
        case 7: return "Milestone! AI correlation engine unlocked!"
        case 14: return "2-week streak! Patterns are getting clearer."
        case 21: return "3-week streak! Great habits forming."
        default:
            if streakDays >= 7 {
                return "\(streakDays)-day streak! Your correlation engine is ready."
            } else {
                return "Day \(streakDays) \u{2014} \(7 - streakDays) more days until trigger analysis!"
            }
        }
    }

    // MARK: - Today's Checklist (today only)

    @ViewBuilder
    private var checklistCard: some View {
        VStack(alignment: .leading, spacing: AppSpacing.sm) {
            Text("Today's Checklist")
                .font(AppTypography.headline)
                .foregroundColor(AppColors.text)

            Divider()

            checklistItem(title: "Meal logged", isComplete: meals.count > 0, icon: "fork.knife")
            checklistItem(title: "Symptoms checked", isComplete: symptoms.count > 0, icon: "waveform.path.ecg")
            checklistItem(title: "Poop tracked", isComplete: poopLogs.count > 0, icon: "drop.fill")
        }
        .padding(AppSpacing.md)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(AppColors.surfaceElevated)
        .cornerRadius(AppRadius.lg)
        .shadow(color: .black.opacity(0.05), radius: 8, x: 0, y: 2)
    }

    @ViewBuilder
    private func checklistItem(title: String, isComplete: Bool, icon: String) -> some View {
        HStack(spacing: AppSpacing.sm) {
            Image(systemName: isComplete ? "checkmark.circle.fill" : "circle")
                .foregroundColor(isComplete ? AppColors.success : AppColors.textTertiary)
                .font(.title3)
            Image(systemName: icon)
                .foregroundColor(isComplete ? AppColors.primary : AppColors.textTertiary)
                .frame(width: 20)
            Text(title)
                .font(AppTypography.body)
                .foregroundColor(isComplete ? AppColors.text : AppColors.textSecondary)
            Spacer()
            if isComplete {
                Text("Done")
                    .font(AppTypography.caption1)
                    .fontWeight(.semibold)
                    .foregroundColor(AppColors.success)
            }
        }
        .padding(.vertical, 2)
    }

    // MARK: - Meals Section

    private var mealsSection: some View {
        VStack(alignment: .leading, spacing: AppSpacing.sm) {
            sectionHeader(title: "Meals", icon: "fork.knife", count: meals.count, color: AppColors.primary)

            if meals.isEmpty {
                emptyEntry(message: "No meals logged", detail: "Tap the Log tab to add a meal")
            } else {
                ForEach(meals) { meal in
                    mealRow(meal)
                        .swipeActions(edge: .trailing, allowsFullSwipe: false) {
                            Button(role: .destructive) {
                                mealToDelete = meal
                            } label: {
                                Label("Delete", systemImage: "trash")
                            }
                        }
                }
            }
        }
    }

    private func mealRow(_ meal: Meal) -> some View {
        VStack(alignment: .leading, spacing: AppSpacing.sm) {
            HStack(alignment: .top, spacing: AppSpacing.sm) {
                // Meal type icon
                Image(systemName: "fork.knife.circle.fill")
                    .font(.title2)
                    .foregroundColor(AppColors.primary)

                VStack(alignment: .leading, spacing: 4) {
                    HStack {
                        Text(meal.mealType.rawValue.capitalized)
                            .font(AppTypography.headline)
                            .foregroundColor(AppColors.text)
                        Spacer()
                        Text(shortTime(meal.createdAt))
                            .font(AppTypography.caption1)
                            .foregroundColor(AppColors.textTertiary)
                    }
                    Text("\(meal.foods.count) \(meal.foods.count == 1 ? "food" : "foods") logged")
                        .font(AppTypography.footnote)
                        .foregroundColor(AppColors.textSecondary)
                }
            }

            // Inline photo thumbnail
            if let urlStr = meal.photoUrl, let url = URL(string: urlStr) {
                AsyncImage(url: url) { phase in
                    switch phase {
                    case .success(let image):
                        image
                            .resizable()
                            .scaledToFill()
                            .frame(height: 120)
                            .frame(maxWidth: .infinity)
                            .clipped()
                            .cornerRadius(AppRadius.md)
                    case .failure:
                        EmptyView()
                    case .empty:
                        RoundedRectangle(cornerRadius: AppRadius.md)
                            .fill(AppColors.border)
                            .frame(height: 80)
                            .overlay(ProgressView())
                    @unknown default:
                        EmptyView()
                    }
                }
            }

            // Foods list with FODMAP badge
            if !meal.foods.isEmpty {
                VStack(alignment: .leading, spacing: 4) {
                    ForEach(meal.foods.prefix(3), id: \.name) { food in
                        HStack(spacing: AppSpacing.xs) {
                            Circle()
                                .fill(fodmapColor(food.fodmapLevel))
                                .frame(width: 8, height: 8)
                            Text(food.name)
                                .font(AppTypography.caption1)
                                .foregroundColor(AppColors.textSecondary)
                            if !food.servingSize.isEmpty {
                                Text("(\(food.servingSize))")
                                    .font(AppTypography.caption2)
                                    .foregroundColor(AppColors.textTertiary)
                            }
                        }
                    }
                    if meal.foods.count > 3 {
                        Text("+\(meal.foods.count - 3) more")
                            .font(AppTypography.caption2)
                            .foregroundColor(AppColors.textTertiary)
                    }
                }
            }

            // High FODMAP badge
            if meal.foods.contains(where: { $0.fodmapLevel == .high }) {
                Text("High FODMAP")
                    .font(AppTypography.caption2)
                    .foregroundColor(.white)
                    .padding(.horizontal, 8)
                    .padding(.vertical, 3)
                    .background(AppColors.fodmapHigh)
                    .cornerRadius(AppRadius.sm)
            }
        }
        .padding(AppSpacing.md)
        .cardStyle()
    }

    // MARK: - Symptoms Section

    private var symptomsSection: some View {
        VStack(alignment: .leading, spacing: AppSpacing.sm) {
            sectionHeader(title: "Symptoms", icon: "waveform.path.ecg", count: symptoms.count, color: AppColors.warning)

            if symptoms.isEmpty {
                emptyEntry(message: "No symptoms logged", detail: "Tap the Log tab to record how you're feeling")
            } else {
                ForEach(symptoms) { symptom in
                    symptomRow(symptom)
                        .swipeActions(edge: .trailing, allowsFullSwipe: false) {
                            Button(role: .destructive) {
                                symptomToDelete = symptom
                            } label: {
                                Label("Delete", systemImage: "trash")
                            }
                        }
                }
            }
        }
    }

    private func symptomRow(_ symptom: Symptom) -> some View {
        VStack(alignment: .leading, spacing: AppSpacing.sm) {
            HStack(alignment: .top, spacing: AppSpacing.sm) {
                Image(systemName: symptom.type.icon)
                    .font(.title2)
                    .foregroundColor(AppColors.warning)

                VStack(alignment: .leading, spacing: 4) {
                    HStack {
                        Text(symptom.type.rawValue.capitalized)
                            .font(AppTypography.headline)
                            .foregroundColor(AppColors.text)
                        if symptom.severity >= 7 {
                            Text("Severe")
                                .font(AppTypography.caption2)
                                .foregroundColor(.white)
                                .padding(.horizontal, 6)
                                .padding(.vertical, 2)
                                .background(AppColors.error)
                                .cornerRadius(AppRadius.sm)
                        }
                        Spacer()
                        Text(shortTime(symptom.createdAt))
                            .font(AppTypography.caption1)
                            .foregroundColor(AppColors.textTertiary)
                    }

                    Text("Severity: \(symptom.severity)/10")
                        .font(AppTypography.footnote)
                        .foregroundColor(AppColors.textSecondary)
                }
            }
        }
        .padding(AppSpacing.md)
        .cardStyle()
    }

    // MARK: - Poop Logs Section

    private var poopLogsSection: some View {
        VStack(alignment: .leading, spacing: AppSpacing.sm) {
            sectionHeader(title: "Poop Logs", icon: "drop.fill", count: poopLogs.count, color: AppColors.fodmapModerate)

            if poopLogs.isEmpty {
                emptyEntry(message: "No poop logs", detail: "Tap the Log tab to track your gut health")
            } else {
                ForEach(poopLogs) { log in
                    poopLogRow(log)
                        .swipeActions(edge: .trailing, allowsFullSwipe: false) {
                            Button(role: .destructive) {
                                poopLogToDelete = log
                            } label: {
                                Label("Delete", systemImage: "trash")
                            }
                        }
                }
            }
        }
        .padding(.bottom, AppSpacing.xl)
    }

    private func poopLogRow(_ log: PoopLog) -> some View {
        let logId = log.id ?? ""
        let isRevealed = revealedPoopPhotoIds.contains(logId)

        return VStack(alignment: .leading, spacing: AppSpacing.sm) {
            HStack(alignment: .top, spacing: AppSpacing.sm) {
                Image(systemName: "drop.fill")
                    .font(.title2)
                    .foregroundColor(AppColors.fodmapModerate)

                VStack(alignment: .leading, spacing: 4) {
                    HStack {
                        Text("Bristol Type \(log.bristolType)")
                            .font(AppTypography.headline)
                            .foregroundColor(AppColors.text)
                        if log.urgency == .emergency {
                            Text("Emergency")
                                .font(AppTypography.caption2)
                                .foregroundColor(.white)
                                .padding(.horizontal, 6)
                                .padding(.vertical, 2)
                                .background(AppColors.error)
                                .cornerRadius(AppRadius.sm)
                        } else if log.urgency == .urgent {
                            Text("Urgent")
                                .font(AppTypography.caption2)
                                .foregroundColor(.white)
                                .padding(.horizontal, 6)
                                .padding(.vertical, 2)
                                .background(AppColors.warning)
                                .cornerRadius(AppRadius.sm)
                        }
                        Spacer()
                        Text(shortTime(log.createdAt))
                            .font(AppTypography.caption1)
                            .foregroundColor(AppColors.textTertiary)
                    }
                    Text(PoopLog.bristolDescriptions[log.bristolType] ?? "")
                        .font(AppTypography.footnote)
                        .foregroundColor(AppColors.textSecondary)

                    Text("Color: \(log.color.rawValue.capitalized)")
                        .font(AppTypography.caption1)
                        .foregroundColor(AppColors.textTertiary)
                }
            }

            // Poop photo — hidden by default, reveal on tap
            if let urlStr = log.photoUrl, let url = URL(string: urlStr) {
                if isRevealed {
                    VStack(alignment: .leading, spacing: AppSpacing.xs) {
                        AsyncImage(url: url) { phase in
                            switch phase {
                            case .success(let image):
                                image
                                    .resizable()
                                    .scaledToFill()
                                    .frame(height: 120)
                                    .frame(maxWidth: .infinity)
                                    .clipped()
                                    .cornerRadius(AppRadius.md)
                            case .failure:
                                EmptyView()
                            case .empty:
                                RoundedRectangle(cornerRadius: AppRadius.md)
                                    .fill(AppColors.border)
                                    .frame(height: 80)
                                    .overlay(ProgressView())
                            @unknown default:
                                EmptyView()
                            }
                        }
                        Button {
                            revealedPoopPhotoIds.remove(logId)
                        } label: {
                            Text("Hide photo")
                                .font(AppTypography.caption2)
                                .foregroundColor(AppColors.textTertiary)
                        }
                        .buttonStyle(.plain)
                    }
                } else {
                    Button {
                        revealedPoopPhotoIds.insert(logId)
                    } label: {
                        HStack(spacing: AppSpacing.xs) {
                            Image(systemName: "eye.slash")
                                .font(.caption)
                            Text("Tap to view photo")
                                .font(AppTypography.caption1)
                        }
                        .foregroundColor(AppColors.primary)
                        .padding(.horizontal, AppSpacing.sm)
                        .padding(.vertical, AppSpacing.xs)
                        .background(AppColors.primaryContainer)
                        .cornerRadius(AppRadius.sm)
                    }
                    .buttonStyle(.plain)
                }
            }
        }
        .padding(AppSpacing.md)
        .cardStyle()
    }

    // MARK: - Section Header

    private func sectionHeader(title: String, icon: String, count: Int, color: Color) -> some View {
        HStack(spacing: AppSpacing.xs) {
            Image(systemName: icon)
                .font(.system(size: 14, weight: .semibold))
                .foregroundColor(color)
            Text(title)
                .font(AppTypography.title3)
                .foregroundColor(AppColors.text)
            Spacer()
            if count > 0 {
                Text("\(count)")
                    .font(AppTypography.caption1)
                    .foregroundColor(.white)
                    .padding(.horizontal, 8)
                    .padding(.vertical, 3)
                    .background(color)
                    .cornerRadius(AppRadius.sm)
            }
        }
    }

    // MARK: - Empty Entry

    private func emptyEntry(message: String, detail: String) -> some View {
        VStack(spacing: AppSpacing.xs) {
            Text(message)
                .font(AppTypography.subhead)
                .foregroundColor(AppColors.textSecondary)
            Text(detail)
                .font(AppTypography.caption1)
                .foregroundColor(AppColors.textTertiary)
                .multilineTextAlignment(.center)
        }
        .frame(maxWidth: .infinity)
        .padding(AppSpacing.md)
        .background(AppColors.surfaceElevated.opacity(0.5))
        .cornerRadius(AppRadius.md)
    }

    // MARK: - Helpers

    private func fodmapColor(_ level: FodmapLevel) -> Color {
        switch level {
        case .low: return AppColors.fodmapLow
        case .moderate: return AppColors.fodmapModerate
        case .high: return AppColors.fodmapHigh
        case .unknown: return AppColors.textTertiary
        }
    }

    private func shortTime(_ date: Date) -> String {
        let formatter = DateFormatter()
        formatter.timeStyle = .short
        formatter.dateStyle = .none
        return formatter.string(from: date)
    }

    // MARK: - Delete Actions

    private func deleteMealConfirmed() async {
        guard let uid = authService.currentUserId, let meal = mealToDelete, let id = meal.id else { return }
        try? await FirestoreService.shared.deleteMeal(uid: uid, mealId: id)
        UINotificationFeedbackGenerator().notificationOccurred(.success)
        mealToDelete = nil
        await loadEntriesForDate()
    }

    private func deleteSymptomConfirmed() async {
        guard let uid = authService.currentUserId, let symptom = symptomToDelete, let id = symptom.id else { return }
        try? await FirestoreService.shared.deleteSymptom(uid: uid, symptomId: id)
        UINotificationFeedbackGenerator().notificationOccurred(.success)
        symptomToDelete = nil
        await loadEntriesForDate()
    }

    private func deletePoopLogConfirmed() async {
        guard let uid = authService.currentUserId, let log = poopLogToDelete, let id = log.id else { return }
        try? await FirestoreService.shared.deletePoopLog(uid: uid, logId: id)
        UINotificationFeedbackGenerator().notificationOccurred(.success)
        poopLogToDelete = nil
        await loadEntriesForDate()
    }



    // MARK: - Data Loading

    private func loadData() async {
        guard let uid = authService.currentUserId else { return }

        async let entriesTask: () = loadEntriesForDate()
        async let allMealsTask: () = loadAllRecentMeals(uid: uid)
        async let allSymptomsTask: () = loadAllRecentSymptoms(uid: uid)
        async let allPoopTask: () = loadAllRecentPoopLogs(uid: uid)

        _ = await (entriesTask, allMealsTask, allSymptomsTask, allPoopTask)

        await loadStreak(uid: uid)
    }

    private func loadEntriesForDate() async {
        guard let uid = authService.currentUserId else { return }

        async let mealTask = FirestoreService.shared.getMealsForDate(uid: uid, date: selectedDate)
        async let symptomTask = FirestoreService.shared.getSymptomsForDate(uid: uid, date: selectedDate)
        async let poopTask = FirestoreService.shared.getPoopLogsForDate(uid: uid, date: selectedDate)

        meals = (try? await mealTask) ?? []
        symptoms = (try? await symptomTask) ?? []
        poopLogs = (try? await poopTask) ?? []
    }

    private func loadAllRecentMeals(uid: String) async {
        allRecentMeals = (try? await FirestoreService.shared.getMeals(uid: uid, limit: 50)) ?? []
    }

    private func loadAllRecentSymptoms(uid: String) async {
        allRecentSymptoms = (try? await FirestoreService.shared.getSymptoms(uid: uid, limit: 50)) ?? []
    }

    private func loadAllRecentPoopLogs(uid: String) async {
        allRecentPoopLogs = (try? await FirestoreService.shared.getPoopLogs(uid: uid, limit: 50)) ?? []
    }

    private func formatDateKey(_ date: Date) -> String {
        let formatter = DateFormatter()
        formatter.dateFormat = "yyyy-MM-dd"
        return formatter.string(from: date)
    }

    // MARK: - Streak Calculation

    private func loadStreak(uid: String) async {
        var streak = 0
        let calendar = Calendar.current

        for daysBack in 0..<30 {
            guard let dayDate = calendar.date(byAdding: .day, value: -daysBack, to: Date()) else { break }
            let dayStart = calendar.startOfDay(for: dayDate)
            guard let dayEnd = calendar.date(byAdding: .day, value: 1, to: dayStart) else { break }

            let hasMeal = allRecentMeals.contains { $0.createdAt >= dayStart && $0.createdAt < dayEnd }
            let hasSymptom = allRecentSymptoms.contains { $0.createdAt >= dayStart && $0.createdAt < dayEnd }
            let hasPoop = allRecentPoopLogs.contains { $0.createdAt >= dayStart && $0.createdAt < dayEnd }

            if hasMeal || hasSymptom || hasPoop {
                streak += 1
            } else {
                break
            }
        }

        streakDays = streak
    }
}
