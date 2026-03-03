import SwiftUI

public struct InsightsView: View {
    @State private var correlationReports: [CorrelationReport] = []
    @State private var isRunningAnalysis = false
    @State private var analysisError: String? = nil
    @State private var expandedReportId: String? = nil
    @State private var selectedDaysBack: Int = 7

    private let dayOptions: [(label: String, value: Int)] = [
        ("3 Days", 3),
        ("7 Days", 7),
        ("10 Days", 10)
    ]

    private let authService = AuthService.shared

    public init() {}

    public var body: some View {
        NavigationStack {
            ScrollView {
                VStack(spacing: AppSpacing.lg) {
                    correlationAnalysisSection
                    if let errorMsg = analysisError {
                        errorBanner(errorMsg)
                    }
                    reportsSection
                    disclaimerView
                }
                .padding(.horizontal, AppSpacing.md)
                .padding(.top, AppSpacing.sm)
            }
            .background(AppColors.background)
            .navigationTitle("Insights")
            .task { await loadData() }
            .refreshable { await loadData() }
        }
    }

    // MARK: - Correlation Analysis Section

    private var correlationAnalysisSection: some View {
        VStack(alignment: .leading, spacing: AppSpacing.sm) {
            Text("Correlation Analysis")
                .font(AppTypography.title3)
                .foregroundColor(AppColors.text)

            Text("AI analysis of your meals, symptoms, and poop logs")
                .font(AppTypography.footnote)
                .foregroundColor(AppColors.textSecondary)

            Picker("Time Period", selection: $selectedDaysBack) {
                ForEach(dayOptions, id: \.value) { option in
                    Text(option.label).tag(option.value)
                }
            }
            .pickerStyle(.segmented)
            .onAppear {
                UISegmentedControl.appearance().selectedSegmentTintColor = UIColor(AppColors.primary)
                UISegmentedControl.appearance().setTitleTextAttributes(
                    [.foregroundColor: UIColor.white],
                    for: .selected
                )
                UISegmentedControl.appearance().setTitleTextAttributes(
                    [.foregroundColor: UIColor(AppColors.primary)],
                    for: .normal
                )
            }
            .padding(.top, AppSpacing.xs)

            Button {
                Task { await runAnalysis() }
            } label: {
                HStack(spacing: AppSpacing.sm) {
                    if isRunningAnalysis {
                        ProgressView()
                            .tint(.white)
                    } else {
                        Image(systemName: "brain.head.profile")
                    }
                    Text(isRunningAnalysis ? "Analyzing..." : "Run Correlation Analysis")
                }
            }
            .buttonStyle(PrimaryButtonStyle(isEnabled: !isRunningAnalysis))
            .disabled(isRunningAnalysis)
            .padding(.top, AppSpacing.xs)

            Text("Analyzes your last \(selectedDaysBack) days of meals, symptoms, and poop logs together for patterns.")
                .font(AppTypography.caption1)
                .foregroundColor(AppColors.textTertiary)
                .multilineTextAlignment(.leading)
        }
    }

    // MARK: - Error Banner

    private func errorBanner(_ message: String) -> some View {
        HStack(alignment: .top, spacing: AppSpacing.sm) {
            Image(systemName: "exclamationmark.triangle.fill")
                .font(.system(size: 14))
                .foregroundColor(AppColors.error)
                .padding(.top, 1)
            Text(message)
                .font(AppTypography.caption1)
                .foregroundColor(AppColors.error)
                .fixedSize(horizontal: false, vertical: true)
        }
        .padding(AppSpacing.md)
        .background(AppColors.error.opacity(0.1))
        .cornerRadius(AppRadius.md)
        .overlay(
            RoundedRectangle(cornerRadius: AppRadius.md)
                .stroke(AppColors.error.opacity(0.3), lineWidth: 1)
        )
    }

    // MARK: - Reports Section

    private var reportsSection: some View {
        VStack(alignment: .leading, spacing: AppSpacing.md) {
            Text("Past Correlation Reports")
                .font(AppTypography.title3)
                .foregroundColor(AppColors.text)

            if correlationReports.isEmpty {
                emptyCard(
                    icon: "doc.text.magnifyingglass",
                    message: "No reports yet",
                    detail: "Run a correlation analysis above to get your first AI-powered gut health report. For best results, log meals, symptoms, and poop logs for at least 7 days.\n\nThis is not medical advice."
                )
            } else {
                ForEach(correlationReports) { report in
                    reportCard(report)
                }
            }
        }
    }

    // MARK: - Report Card

    private func reportCard(_ report: CorrelationReport) -> some View {
        let isExpanded = expandedReportId == report.id

        return VStack(alignment: .leading, spacing: AppSpacing.sm) {

            // Header row — tappable to expand/collapse
            Button {
                withAnimation(.easeInOut(duration: 0.2)) {
                    if isExpanded {
                        expandedReportId = nil
                    } else {
                        expandedReportId = report.id
                    }
                }
            } label: {
                HStack(alignment: .center) {
                    VStack(alignment: .leading, spacing: 4) {
                        Text(formatDateRange(start: report.periodStart, end: report.periodEnd))
                            .font(AppTypography.headline)
                            .foregroundColor(AppColors.text)

                        HStack(spacing: AppSpacing.sm) {
                            statPill(value: report.mealsAnalyzed, label: "meals", icon: "fork.knife")
                            statPill(value: report.symptomsAnalyzed, label: "symptoms", icon: "waveform.path.ecg")
                            statPill(value: report.poopLogsAnalyzed, label: "poop", icon: "drop.fill")
                        }
                    }

                    Spacer()

                    Image(systemName: isExpanded ? "chevron.up" : "chevron.down")
                        .font(.caption)
                        .foregroundColor(AppColors.textTertiary)
                }
            }
            .buttonStyle(.plain)

            // Report date
            Text("Generated \(relativeDate(report.createdAt))")
                .font(AppTypography.caption2)
                .foregroundColor(AppColors.textTertiary)

            // Expanded content
            if isExpanded {
                Divider()

                // Full narrative
                if report.aiReport.isEmpty {
                    Text("No report text available.")
                        .font(AppTypography.footnote)
                        .foregroundColor(AppColors.textTertiary)
                        .italic()
                } else {
                    Text(report.aiReport)
                        .font(AppTypography.footnote)
                        .foregroundColor(AppColors.textSecondary)
                        .fixedSize(horizontal: false, vertical: true)
                }

                Divider()

                // Disclaimer
                Text(report.disclaimer.isEmpty ? "This is not medical advice." : report.disclaimer)
                    .font(AppTypography.caption2)
                    .foregroundColor(AppColors.textTertiary)
                    .italic()
                    .frame(maxWidth: .infinity, alignment: .leading)
            }
        }
        .padding(AppSpacing.md)
        .background(AppColors.surfaceElevated)
        .cornerRadius(AppRadius.lg)
        .shadow(color: .black.opacity(0.05), radius: 8, x: 0, y: 2)
    }

    // MARK: - Stat Pill

    private func statPill(value: Int, label: String, icon: String) -> some View {
        HStack(spacing: 3) {
            Image(systemName: icon)
                .font(.system(size: 9))
                .foregroundColor(AppColors.textTertiary)
            Text("\(value) \(label)")
                .font(.system(size: 11))
                .foregroundColor(AppColors.textTertiary)
        }
        .padding(.horizontal, 6)
        .padding(.vertical, 3)
        .background(AppColors.background)
        .cornerRadius(AppRadius.sm)
    }

    // MARK: - Helpers

    private func emptyCard(icon: String, message: String, detail: String) -> some View {
        VStack(spacing: AppSpacing.sm) {
            Image(systemName: icon)
                .font(.system(size: 32))
                .foregroundColor(AppColors.primary.opacity(0.5))
            Text(message)
                .font(AppTypography.headline)
                .foregroundColor(AppColors.textSecondary)
            Text(detail)
                .font(AppTypography.footnote)
                .foregroundColor(AppColors.textTertiary)
                .multilineTextAlignment(.center)
        }
        .frame(maxWidth: .infinity)
        .padding(AppSpacing.lg)
        .cardStyle()
    }

    private var disclaimerView: some View {
        Text("This is an educational wellness tool. It is not intended to diagnose, treat, or cure any medical condition. This is not medical advice.")
            .font(AppTypography.caption2)
            .foregroundColor(AppColors.textTertiary)
            .multilineTextAlignment(.center)
            .padding(AppSpacing.md)
    }

    // MARK: - Date Formatting

    private static let isoFormatter: DateFormatter = {
        let f = DateFormatter(); f.dateFormat = "yyyy-MM-dd"; return f
    }()
    private static let displayFormatter: DateFormatter = {
        let f = DateFormatter(); f.dateFormat = "MMM d"; return f
    }()
    private static let displayYearFormatter: DateFormatter = {
        let f = DateFormatter(); f.dateFormat = "MMM d, yyyy"; return f
    }()
    private static let mediumDateFormatter: DateFormatter = {
        let f = DateFormatter(); f.dateStyle = .medium; f.timeStyle = .none; return f
    }()

    private func formatDateRange(start: String, end: String) -> String {
        if let s = Self.isoFormatter.date(from: start), let e = Self.isoFormatter.date(from: end) {
            let calendar = Calendar.current
            let startYear = calendar.component(.year, from: s)
            let endYear = calendar.component(.year, from: e)
            let currentYear = calendar.component(.year, from: Date())
            if startYear == currentYear && endYear == currentYear {
                return "\(Self.displayFormatter.string(from: s)) - \(Self.displayFormatter.string(from: e))"
            } else {
                return "\(Self.displayYearFormatter.string(from: s)) - \(Self.displayYearFormatter.string(from: e))"
            }
        }
        return "\(start) - \(end)"
    }

    private func relativeDate(_ date: Date) -> String {
        let interval = Date().timeIntervalSince(date)
        if interval < 86400 { return "today" }
        if interval < 172800 { return "yesterday" }
        let days = Int(interval / 86400)
        if days < 7 { return "\(days) days ago" }
        return Self.mediumDateFormatter.string(from: date)
    }

    // MARK: - Data

    private func loadData() async {
        guard let uid = authService.currentUserId else { return }
        await loadReports(uid: uid)
    }

    private func loadReports(uid: String) async {
        let firestoreReports = (try? await FirestoreService.shared.getCorrelationReports(uid: uid)) ?? []
        // Merge: keep any in-memory reports (from just-run analysis) that aren't in Firestore yet
        let firestoreIds = Set(firestoreReports.compactMap(\.id))
        let localOnly = correlationReports.filter { report in
            guard let id = report.id else { return true }
            return !firestoreIds.contains(id)
        }
        correlationReports = localOnly + firestoreReports
    }

    private func runAnalysis() async {
        isRunningAnalysis = true
        analysisError = nil
        do {
            let result = try await FunctionsService.shared.runCorrelationEngine(daysBack: selectedDaysBack)
            if let message = result.message {
                analysisError = message
            } else if let report = result.report {
                correlationReports.insert(report, at: 0)
            }
        } catch {
            analysisError = "Analysis failed: \(error.localizedDescription)"
        }
        isRunningAnalysis = false
    }
}

