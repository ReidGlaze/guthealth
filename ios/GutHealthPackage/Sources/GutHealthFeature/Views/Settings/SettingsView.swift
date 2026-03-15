import SwiftUI

public struct SettingsView: View {
    @Environment(\.dismiss) private var dismiss

    private let authService = AuthService.shared

    @State private var morningReminder = true
    @State private var morningTime = defaultTime(hour: 8, minute: 0)
    @State private var lunchReminder = true
    @State private var lunchTime = defaultTime(hour: 12, minute: 0)
    @State private var dinnerReminder = true
    @State private var dinnerTime = defaultTime(hour: 18, minute: 0)
    @State private var notificationsEnabled = false
    @State private var isLoading = true
    @State private var showDeleteConfirmation = false
    @State private var showSignOutConfirmation = false
    @State private var showDisclaimer = false
    @State private var isSaving = false

    public init() {}

    public var body: some View {
        NavigationStack {
            List {
                reminderTimesSection
                notificationsSection
                appInfoSection
                disclaimerSection
                accountSection
            }
            .navigationTitle("Settings")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .topBarLeading) {
                    Button("Done") { dismiss() }
                        .foregroundColor(AppColors.primary)
                }
            }
            .task { await loadProfile() }
            .alert("Delete All Data?", isPresented: $showDeleteConfirmation) {
                Button("Cancel", role: .cancel) {}
                Button("Delete Everything", role: .destructive) {
                    Task { await deleteAllData() }
                }
            } message: {
                Text("This will permanently delete all your meals, symptoms, poop logs, and insights. This cannot be undone.")
            }
            .alert("Sign Out?", isPresented: $showSignOutConfirmation) {
                Button("Cancel", role: .cancel) {}
                Button("Sign Out", role: .destructive) {
                    signOut()
                }
            } message: {
                Text("You will need to sign in again to access your data.")
            }
            .sheet(isPresented: $showDisclaimer) {
                disclaimerSheet
            }
        }
    }

    // MARK: - Reminder Times

    private var reminderTimesSection: some View {
        Section {
            reminderRow(
                label: "Morning",
                icon: "sunrise.fill",
                isOn: $morningReminder,
                time: $morningTime
            )
            reminderRow(
                label: "Lunch",
                icon: "sun.max.fill",
                isOn: $lunchReminder,
                time: $lunchTime
            )
            reminderRow(
                label: "Dinner",
                icon: "sunset.fill",
                isOn: $dinnerReminder,
                time: $dinnerTime
            )
        } header: {
            Text("Reminder Times")
        } footer: {
            Text("Get reminders to log your meals at these times.")
        }
    }

    private func reminderRow(label: String, icon: String, isOn: Binding<Bool>, time: Binding<Date>) -> some View {
        VStack(spacing: 0) {
            HStack {
                Image(systemName: icon)
                    .foregroundColor(AppColors.primary)
                    .frame(width: 24)
                Text(label)
                Spacer()
                Toggle("", isOn: isOn)
                    .labelsHidden()
                    .tint(AppColors.primary)
                    .onChange(of: isOn.wrappedValue) { _, _ in
                        Task { await saveReminderTimes() }
                    }
            }
            if isOn.wrappedValue {
                DatePicker("", selection: time, displayedComponents: .hourAndMinute)
                    .labelsHidden()
                    .onChange(of: time.wrappedValue) { _, _ in
                        Task { await saveReminderTimes() }
                    }
            }
        }
    }

    // MARK: - Notifications

    private var notificationsSection: some View {
        Section {
            Toggle(isOn: $notificationsEnabled) {
                HStack {
                    Image(systemName: "bell.fill")
                        .foregroundColor(AppColors.primary)
                        .frame(width: 24)
                    Text("Enable Notifications")
                }
            }
            .tint(AppColors.primary)
            .onChange(of: notificationsEnabled) { _, enabled in
                Task { await toggleNotifications(enabled) }
            }
        } header: {
            Text("Notifications")
        }
    }

    // MARK: - App Info

    private var appInfoSection: some View {
        Section {
            HStack {
                Text("Version")
                Spacer()
                Text(appVersion)
                    .foregroundColor(AppColors.textSecondary)
            }
            Button {
                showDisclaimer = true
            } label: {
                HStack {
                    Image(systemName: "info.circle")
                        .foregroundColor(AppColors.primary)
                        .frame(width: 24)
                    Text("About AI Gut Health")
                        .foregroundColor(AppColors.text)
                }
            }
        } header: {
            Text("App Info")
        }
    }

    // MARK: - Disclaimer

    private var disclaimerSection: some View {
        Section {
            VStack(alignment: .leading, spacing: AppSpacing.sm) {
                HStack {
                    Image(systemName: "exclamationmark.shield.fill")
                        .foregroundColor(AppColors.warning)
                    Text("Medical Disclaimer")
                        .font(AppTypography.headline)
                }
                Text("AI Gut Health & IBS Tracker is an educational wellness tool based on peer-reviewed research from Monash University (FODMAP data) and the Bristol Stool Chart (Lewis & Heaton, 1997). It is not intended to provide medical advice, diagnosis, or treatment recommendations. Always consult a qualified healthcare professional before making dietary changes or medical decisions. The AI-generated content in this app is for informational purposes only.")
                    .font(AppTypography.footnote)
                    .foregroundColor(AppColors.textSecondary)
            }
            .padding(.vertical, AppSpacing.xs)
        }
    }

    // MARK: - Account

    private var accountSection: some View {
        Section {
            Button {
                showSignOutConfirmation = true
            } label: {
                HStack {
                    Image(systemName: "rectangle.portrait.and.arrow.right")
                        .foregroundColor(AppColors.warning)
                        .frame(width: 24)
                    Text("Sign Out")
                        .foregroundColor(AppColors.warning)
                }
            }

            Button {
                showDeleteConfirmation = true
            } label: {
                HStack {
                    Image(systemName: "trash.fill")
                        .foregroundColor(AppColors.error)
                        .frame(width: 24)
                    Text("Delete All Data")
                        .foregroundColor(AppColors.error)
                }
            }
        } header: {
            Text("Account")
        } footer: {
            Text("Deleting data will permanently remove all your logged meals, symptoms, poop logs, and AI insights.")
        }
    }

    // MARK: - Disclaimer Sheet

    private var disclaimerSheet: some View {
        NavigationStack {
            ScrollView {
                VStack(alignment: .leading, spacing: AppSpacing.lg) {
                    Text("About AI Gut Health & IBS Tracker")
                        .font(AppTypography.title2)
                        .foregroundColor(AppColors.text)

                    Text("AI Gut Health & IBS Tracker is an educational wellness tool developed by Twin Tip Solutions LLC. It helps you track your diet, symptoms, and digestive health patterns using AI-powered food analysis and correlation detection.")
                        .font(AppTypography.body)
                        .foregroundColor(AppColors.textSecondary)

                    Divider()

                    Text("Medical Disclaimer")
                        .font(AppTypography.title3)
                        .foregroundColor(AppColors.text)

                    Text("""
                    This application is designed as an educational wellness tool and is NOT intended to:

                    - Provide medical advice or diagnosis
                    - Replace consultation with healthcare professionals
                    - Serve as a substitute for professional medical treatment
                    - Make claims about curing or treating any medical condition

                    The AI-generated content, including food analysis, symptom correlations, and weekly insights, is for informational and educational purposes only. Results may not be accurate and should not be used as the sole basis for dietary or health decisions.

                    Always consult with a qualified healthcare professional, such as a registered dietitian or gastroenterologist, before starting an elimination diet or making significant dietary changes.

                    If you are experiencing severe symptoms, please seek immediate medical attention.
                    """)
                        .font(AppTypography.footnote)
                        .foregroundColor(AppColors.textSecondary)

                    Divider()

                    Text("Sources & References")
                        .font(AppTypography.title3)
                        .foregroundColor(AppColors.text)

                    VStack(alignment: .leading, spacing: AppSpacing.sm) {
                        Text("The health information in this app is based on the following peer-reviewed and institutional sources:")
                            .font(AppTypography.footnote)
                            .foregroundColor(AppColors.textSecondary)

                        sourceLink(
                            title: "FODMAP Diet Data",
                            detail: "Monash University — pioneers of the low-FODMAP diet and the gold standard for FODMAP food classifications and safe serving sizes.",
                            url: "https://www.monashfodmap.com"
                        )

                        sourceLink(
                            title: "Bristol Stool Chart",
                            detail: "Lewis SJ, Heaton KW. \"Stool Form Scale as a Useful Guide to Intestinal Transit Time.\" Scandinavian Journal of Gastroenterology, 1997;32(9):920-924.",
                            url: "https://pubmed.ncbi.nlm.nih.gov/9299672/"
                        )

                        sourceLink(
                            title: "IBS & Digestive Health",
                            detail: "International Foundation for Gastrointestinal Disorders (IFFGD) — patient education on IBS symptoms, diagnosis, and management.",
                            url: "https://iffgd.org"
                        )

                        sourceLink(
                            title: "Low-FODMAP Diet Evidence",
                            detail: "Halmos EP, et al. \"A Diet Low in FODMAPs Reduces Symptoms of Irritable Bowel Syndrome.\" Gastroenterology, 2014;146(1):67-75.",
                            url: "https://pubmed.ncbi.nlm.nih.gov/24076059/"
                        )
                    }

                    Divider()

                    Text("Privacy")
                        .font(AppTypography.title3)
                        .foregroundColor(AppColors.text)

                    Text("This app uses anonymous authentication. We do not collect personally identifiable information. Your health data is stored securely in Firebase and can be deleted at any time from Settings.")
                        .font(AppTypography.footnote)
                        .foregroundColor(AppColors.textSecondary)

                    HStack(spacing: AppSpacing.lg) {
                        if let privacyURL = URL(string: "https://guthealth.twintipsolutions.com/privacy") {
                            Link("Privacy Policy", destination: privacyURL)
                                .font(AppTypography.subhead)
                                .foregroundColor(AppColors.primary)
                        }
                        if let termsURL = URL(string: "https://guthealth.twintipsolutions.com/terms") {
                            Link("Terms of Service", destination: termsURL)
                                .font(AppTypography.subhead)
                                .foregroundColor(AppColors.primary)
                        }
                    }
                    .padding(.top, AppSpacing.xs)
                }
                .padding(AppSpacing.lg)
            }
            .background(AppColors.background)
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) {
                    Button("Close") { showDisclaimer = false }
                        .foregroundColor(AppColors.primary)
                }
            }
        }
    }

    // MARK: - Source Link Helper

    private func sourceLink(title: String, detail: String, url: String) -> some View {
        VStack(alignment: .leading, spacing: 4) {
            Text(title)
                .font(AppTypography.subhead)
                .fontWeight(.semibold)
                .foregroundColor(AppColors.text)
            Text(detail)
                .font(AppTypography.caption1)
                .foregroundColor(AppColors.textSecondary)
            if let linkURL = URL(string: url) {
                Link(url, destination: linkURL)
                    .font(AppTypography.caption1)
                    .foregroundColor(AppColors.primary)
            }
        }
        .padding(AppSpacing.sm)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(AppColors.primaryContainer)
        .cornerRadius(AppRadius.sm)
    }

    // MARK: - Data Loading

    private func loadProfile() async {
        guard let uid = authService.currentUserId else {
            isLoading = false
            return
        }
        do {
            if let profile = try await FirestoreService.shared.getUserProfile(uid: uid) {
                notificationsEnabled = profile.preferences.notificationsEnabled

                let times = profile.preferences.reminderTimes
                morningReminder = times.contains { $0.hasPrefix("08") || $0.hasPrefix("07") || $0.hasPrefix("06") || $0.hasPrefix("09") }
                lunchReminder = times.contains { $0.hasPrefix("12") || $0.hasPrefix("11") || $0.hasPrefix("13") }
                dinnerReminder = times.contains { $0.hasPrefix("18") || $0.hasPrefix("17") || $0.hasPrefix("19") || $0.hasPrefix("20") }

                // Parse actual times
                for time in times {
                    if let date = parseTime(time) {
                        let hour = Calendar.current.component(.hour, from: date)
                        if hour < 11 { morningTime = date; morningReminder = true }
                        else if hour < 15 { lunchTime = date; lunchReminder = true }
                        else { dinnerTime = date; dinnerReminder = true }
                    }
                }
            }
        } catch {
            // Use defaults on error
        }
        isLoading = false
    }

    // MARK: - Save Actions

    private func saveReminderTimes() async {
        guard let uid = authService.currentUserId else { return }
        var times: [String] = []
        if morningReminder { times.append(formatTime(morningTime)) }
        if lunchReminder { times.append(formatTime(lunchTime)) }
        if dinnerReminder { times.append(formatTime(dinnerTime)) }

        try? await FirestoreService.shared.updateUserProfile(uid: uid, fields: [
            "preferences.reminderTimes": times
        ])
    }

    private func toggleNotifications(_ enabled: Bool) async {
        if enabled {
            let granted = await NotificationHelper.requestPermissionAndRegister()
            if !granted {
                notificationsEnabled = false
                return
            }
        }
        guard let uid = authService.currentUserId else { return }
        var fields: [String: Any] = [
            "preferences.notificationsEnabled": enabled,
            "preferences.timezone": TimeZone.current.identifier
        ]
        // When enabling, also save current reminder times so they're set
        if enabled {
            var times: [String] = []
            if morningReminder { times.append(formatTime(morningTime)) }
            if lunchReminder { times.append(formatTime(lunchTime)) }
            if dinnerReminder { times.append(formatTime(dinnerTime)) }
            fields["preferences.reminderTimes"] = times
        }
        try? await FirestoreService.shared.updateUserProfile(uid: uid, fields: fields)
    }

    private func signOut() {
        try? authService.signOut()
    }

    private func deleteAllData() async {
        guard let uid = authService.currentUserId else { return }
        try? await FirestoreService.shared.deleteUserData(uid: uid)
        try? await authService.deleteUser()
    }

    // MARK: - Helpers

    private var appVersion: String {
        let version = Bundle.main.infoDictionary?["CFBundleShortVersionString"] as? String ?? "1.0"
        let build = Bundle.main.infoDictionary?["CFBundleVersion"] as? String ?? "1"
        return "\(version) (\(build))"
    }

    private static func defaultTime(hour: Int, minute: Int) -> Date {
        var comps = DateComponents()
        comps.hour = hour
        comps.minute = minute
        return Calendar.current.date(from: comps) ?? Date()
    }

    private func formatTime(_ date: Date) -> String {
        let formatter = DateFormatter()
        formatter.dateFormat = "HH:mm"
        return formatter.string(from: date)
    }

    private func parseTime(_ timeString: String) -> Date? {
        let formatter = DateFormatter()
        formatter.dateFormat = "HH:mm"
        return formatter.date(from: timeString)
    }
}
