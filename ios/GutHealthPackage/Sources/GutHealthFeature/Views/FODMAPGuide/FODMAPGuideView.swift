import SwiftUI

public struct FODMAPGuideView: View {
    @State private var searchText = ""
    @State private var foods: [FodmapFood] = []
    @State private var isLoading = false
    @State private var selectedFilter: FodmapLevel? = nil
    @State private var userProfile: UserProfile?

    private let authService = AuthService.shared

    public init() {}

    public var body: some View {
        NavigationStack {
            VStack(spacing: 0) {
                phaseTracker(userProfile)
                filterChips
                foodList
            }
            .background(AppColors.background)
            .navigationTitle("FODMAP Guide")
            .searchable(text: $searchText, prompt: "Search foods...")
            .task { await loadData() }
            .refreshable { await loadData() }
        }
    }

    // MARK: - Phase Tracker

    private func phaseTracker(_ profile: UserProfile?) -> some View {
        let currentPhase = profile?.fodmapPhase ?? .elimination
        let startDate = profile?.fodmapPhaseStartDate ?? Date()

        return VStack(spacing: AppSpacing.sm) {
            HStack {
                Text("Your FODMAP Phase")
                    .font(AppTypography.headline)
                    .foregroundColor(AppColors.text)
                Spacer()
            }

            HStack(spacing: 0) {
                ForEach(FodmapPhase.allCases, id: \.self) { phase in
                    phaseSegment(phase, isActive: currentPhase == phase, isCurrent: currentPhase == phase)
                }
            }
            .clipShape(RoundedRectangle(cornerRadius: AppRadius.md))

            HStack(spacing: AppSpacing.sm) {
                Image(systemName: phaseIcon(currentPhase))
                    .foregroundColor(AppColors.primary)
                VStack(alignment: .leading, spacing: 2) {
                    Text(phaseTitle(currentPhase))
                        .font(AppTypography.subhead)
                        .foregroundColor(AppColors.text)
                    Text(phaseDescription(currentPhase))
                        .font(AppTypography.caption1)
                        .foregroundColor(AppColors.textSecondary)
                }
                Spacer()
                Text(daysSinceStart(startDate))
                    .font(AppTypography.caption1)
                    .foregroundColor(AppColors.primary)
                    .padding(.horizontal, 8)
                    .padding(.vertical, 4)
                    .background(AppColors.primaryContainer)
                    .cornerRadius(AppRadius.sm)
            }

            phaseTips(currentPhase)
        }
        .padding(AppSpacing.md)
        .cardStyle()
        .padding(.horizontal, AppSpacing.md)
        .padding(.top, AppSpacing.sm)
    }

    private func phaseSegment(_ phase: FodmapPhase, isActive: Bool, isCurrent: Bool) -> some View {
        let isReached = phaseOrder(phase) <= phaseOrder(userProfile?.fodmapPhase ?? .elimination)
        return VStack(spacing: 2) {
            Text(phase.rawValue.capitalized)
                .font(AppTypography.caption2)
                .foregroundColor(isReached ? .white : AppColors.textTertiary)
        }
        .frame(maxWidth: .infinity)
        .padding(.vertical, AppSpacing.sm)
        .background(isReached ? AppColors.primary : AppColors.surface)
    }

    private func phaseOrder(_ phase: FodmapPhase) -> Int {
        switch phase {
        case .elimination: return 0
        case .reintroduction: return 1
        case .maintenance: return 2
        }
    }

    private func phaseIcon(_ phase: FodmapPhase) -> String {
        switch phase {
        case .elimination: return "xmark.circle"
        case .reintroduction: return "arrow.triangle.2.circlepath"
        case .maintenance: return "checkmark.circle"
        }
    }

    private func phaseTitle(_ phase: FodmapPhase) -> String {
        switch phase {
        case .elimination: return "Elimination Phase"
        case .reintroduction: return "Reintroduction Phase"
        case .maintenance: return "Maintenance Phase"
        }
    }

    private func phaseDescription(_ phase: FodmapPhase) -> String {
        switch phase {
        case .elimination: return "Avoid high FODMAP foods for 2-6 weeks"
        case .reintroduction: return "Test one FODMAP group at a time"
        case .maintenance: return "Personalized diet based on your tolerances"
        }
    }

    private func daysSinceStart(_ date: Date) -> String {
        let days = Calendar.current.dateComponents([.day], from: date, to: Date()).day ?? 0
        if days == 0 { return "Started today" }
        if days == 1 { return "Day 1" }
        return "Day \(days)"
    }

    private func tipsForPhase(_ phase: FodmapPhase) -> [String] {
        switch phase {
        case .elimination:
            return [
                "Focus on low FODMAP foods (marked green below)",
                "Keep a detailed food diary",
                "Symptoms should improve within 2-6 weeks"
            ]
        case .reintroduction:
            return [
                "Test one FODMAP group every 3 days",
                "Keep portions small at first",
                "Log any symptoms that appear"
            ]
        case .maintenance:
            return [
                "Eat a varied diet within your tolerances",
                "You may tolerate some high FODMAP foods in small amounts",
                "Continue logging to track patterns"
            ]
        }
    }

    private func phaseTips(_ phase: FodmapPhase) -> some View {
        let tips = tipsForPhase(phase)

        return VStack(alignment: .leading, spacing: AppSpacing.xs) {
            Text("Tips")
                .font(AppTypography.caption1)
                .fontWeight(.semibold)
                .foregroundColor(AppColors.primary)
            ForEach(tips, id: \.self) { tip in
                HStack(alignment: .top, spacing: AppSpacing.xs) {
                    Image(systemName: "checkmark.circle.fill")
                        .font(.caption2)
                        .foregroundColor(AppColors.primary)
                        .padding(.top, 2)
                    Text(tip)
                        .font(AppTypography.caption1)
                        .foregroundColor(AppColors.textSecondary)
                }
            }
        }
        .padding(AppSpacing.sm)
        .background(AppColors.primaryContainer)
        .cornerRadius(AppRadius.sm)
    }

    // MARK: - Filter Chips

    private var filterChips: some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: AppSpacing.sm) {
                filterChip(label: "All", level: nil)
                filterChip(label: "Low", level: .low)
                filterChip(label: "Moderate", level: .moderate)
                filterChip(label: "High", level: .high)
            }
            .padding(.horizontal, AppSpacing.md)
            .padding(.vertical, AppSpacing.sm)
        }
    }

    private func filterChip(label: String, level: FodmapLevel?) -> some View {
        Button {
            selectedFilter = level
        } label: {
            Text(label)
                .font(AppTypography.subhead)
                .foregroundColor(selectedFilter == level ? .white : AppColors.text)
                .padding(.horizontal, AppSpacing.md)
                .padding(.vertical, AppSpacing.sm)
                .background(selectedFilter == level ? colorForLevel(level) : AppColors.surface)
                .cornerRadius(AppRadius.full)
        }
        .buttonStyle(.plain)
    }

    private func colorForLevel(_ level: FodmapLevel?) -> Color {
        switch level {
        case .low: return AppColors.fodmapLow
        case .moderate: return AppColors.fodmapModerate
        case .high: return AppColors.fodmapHigh
        case .unknown, nil: return AppColors.primary
        }
    }

    // MARK: - Food List

    private var foodList: some View {
        Group {
            if isLoading {
                ProgressView()
                    .tint(AppColors.primary)
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
            } else if filteredFoods.isEmpty {
                emptyState
            } else {
                List {
                    ForEach(filteredFoods, id: \.name) { food in
                        foodRow(food)
                            .listRowBackground(AppColors.surfaceElevated)
                    }
                    Section {
                        Text("This is an educational wellness tool. It is not intended to diagnose, treat, or cure any medical condition. This is not medical advice. Consult a dietitian for personalized FODMAP guidance.")
                            .font(AppTypography.caption1)
                            .foregroundColor(AppColors.textTertiary)
                            .multilineTextAlignment(.center)
                            .frame(maxWidth: .infinity)
                            .listRowBackground(Color.clear)
                    }
                }
                .listStyle(.plain)
            }
        }
    }

    private var filteredFoods: [FodmapFood] {
        var result = foods
        if let filter = selectedFilter {
            result = result.filter { $0.fodmapLevel == filter }
        }
        if !searchText.isEmpty {
            result = result.filter { $0.name.localizedCaseInsensitiveContains(searchText) }
        }
        return result
    }

    private func foodRow(_ food: FodmapFood) -> some View {
        HStack(spacing: AppSpacing.md) {
            Circle()
                .fill(colorForLevel(food.fodmapLevel))
                .frame(width: 12, height: 12)

            VStack(alignment: .leading, spacing: 2) {
                Text(food.name)
                    .font(AppTypography.headline)
                    .foregroundColor(AppColors.text)
                HStack(spacing: AppSpacing.xs) {
                    Text(food.category.capitalized)
                        .font(AppTypography.caption1)
                        .foregroundColor(AppColors.textSecondary)
                    if !food.fodmapCategories.isEmpty {
                        Text(food.fodmapCategories.joined(separator: ", "))
                            .font(AppTypography.caption2)
                            .foregroundColor(AppColors.textTertiary)
                    }
                }
            }

            Spacer()

            VStack(alignment: .trailing, spacing: 2) {
                Text(food.fodmapLevel.rawValue.uppercased())
                    .font(AppTypography.caption2)
                    .fontWeight(.semibold)
                    .foregroundColor(colorForLevel(food.fodmapLevel))
                if !food.lowFodmapServing.isEmpty {
                    Text("Safe: \(food.lowFodmapServing)")
                        .font(AppTypography.caption2)
                        .foregroundColor(AppColors.textTertiary)
                }
            }
        }
        .padding(.vertical, AppSpacing.xs)
    }

    private var emptyState: some View {
        VStack(spacing: AppSpacing.md) {
            Image(systemName: "leaf.circle")
                .font(.system(size: 48))
                .foregroundColor(AppColors.primary.opacity(0.5))
            Text("No foods found")
                .font(AppTypography.headline)
                .foregroundColor(AppColors.textSecondary)
            Text("FODMAP foods will appear here once the database is populated")
                .font(AppTypography.footnote)
                .foregroundColor(AppColors.textTertiary)
                .multilineTextAlignment(.center)
        }
        .padding(AppSpacing.xxl)
        .frame(maxWidth: .infinity, maxHeight: .infinity)
    }

    // MARK: - Data

    private func loadData() async {
        isLoading = true
        async let foodsTask: () = loadFoods()
        async let profileTask: () = loadProfile()
        _ = await (foodsTask, profileTask)
        isLoading = false
    }

    private func loadFoods() async {
        foods = FodmapRepository.shared.foods
        await FodmapRepository.shared.refreshIfNeeded()
        foods = FodmapRepository.shared.foods
    }

    private func loadProfile() async {
        guard let uid = authService.currentUserId else { return }
        userProfile = try? await FirestoreService.shared.getUserProfile(uid: uid)
    }
}
