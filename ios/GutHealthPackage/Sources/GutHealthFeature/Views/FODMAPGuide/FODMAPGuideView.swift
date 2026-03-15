import SwiftUI

public struct FODMAPGuideView: View {
    @State private var searchText = ""
    @State private var foods: [FodmapFood] = []
    @State private var isLoading = false
    @State private var selectedFilter: FodmapLevel? = nil
    @State private var isInfoExpanded = false

    public init() {}

    public var body: some View {
        NavigationStack {
            VStack(spacing: 0) {
                searchBar
                fodmapInfoCard
                filterChips
                foodList
            }
            .background(AppColors.background)
            .navigationTitle("FODMAP Guide")
            .navigationBarTitleDisplayMode(.inline)
            .task { await loadData() }
            .refreshable { await loadData() }
        }
    }

    // MARK: - Search Bar

    private var searchBar: some View {
        HStack {
            Image(systemName: "magnifyingglass")
                .foregroundColor(AppColors.textTertiary)
            TextField("Search foods...", text: $searchText)
                .font(AppTypography.body)
            if !searchText.isEmpty {
                Button {
                    searchText = ""
                } label: {
                    Image(systemName: "xmark.circle.fill")
                        .foregroundColor(AppColors.textTertiary)
                }
            }
        }
        .padding(AppSpacing.sm)
        .background(AppColors.surface)
        .cornerRadius(AppRadius.md)
        .padding(.horizontal, AppSpacing.md)
        .padding(.top, AppSpacing.sm)
    }

    // MARK: - What is FODMAP? Info Card

    private var fodmapInfoCard: some View {
        VStack(alignment: .leading, spacing: 0) {
            HStack {
                Image(systemName: "info.circle.fill")
                    .foregroundColor(AppColors.primary)
                Text("What is FODMAP?")
                    .font(AppTypography.headline)
                    .foregroundColor(AppColors.text)
                Spacer()
                Image(systemName: isInfoExpanded ? "chevron.up" : "chevron.down")
                    .font(.caption)
                    .foregroundColor(AppColors.textTertiary)
            }
            .padding(.vertical, AppSpacing.xs)
            .contentShape(Rectangle())
            .onTapGesture {
                withAnimation(.easeInOut(duration: 0.2)) {
                    isInfoExpanded.toggle()
                }
            }

            if isInfoExpanded {
                VStack(alignment: .leading, spacing: AppSpacing.sm) {
                    Text("FODMAPs (Fermentable Oligosaccharides, Disaccharides, Monosaccharides, and Polyols) are short-chain carbohydrates that can cause bloating, gas, and digestive discomfort, especially for those with IBS.\n\nThis guide helps you identify high and low FODMAP foods. Use it with the app's meal logging and correlation analysis to discover your personal triggers.")
                        .font(AppTypography.footnote)
                        .foregroundColor(AppColors.textSecondary)
                        .fixedSize(horizontal: false, vertical: true)

                    VStack(alignment: .leading, spacing: AppSpacing.xs) {
                        Text("Color Legend")
                            .font(AppTypography.caption1)
                            .fontWeight(.semibold)
                            .foregroundColor(AppColors.text)
                        legendRow(color: AppColors.fodmapLow, label: "Low", detail: "Generally well-tolerated")
                        legendRow(color: AppColors.fodmapModerate, label: "Moderate", detail: "May trigger in larger portions")
                        legendRow(color: AppColors.fodmapHigh, label: "High", detail: "Common trigger for sensitive individuals")
                    }
                    .padding(AppSpacing.sm)
                    .background(AppColors.primaryContainer)
                    .cornerRadius(AppRadius.sm)
                }
                .padding(.top, AppSpacing.sm)
            }
        }
        .padding(AppSpacing.md)
        .cardStyle()
        .padding(.horizontal, AppSpacing.md)
        .padding(.top, AppSpacing.sm)
    }

    private func legendRow(color: Color, label: String, detail: String) -> some View {
        HStack(spacing: AppSpacing.sm) {
            Circle()
                .fill(color)
                .frame(width: 10, height: 10)
            Text(label)
                .font(AppTypography.caption1)
                .fontWeight(.medium)
                .foregroundColor(AppColors.text)
            Text("— \(detail)")
                .font(AppTypography.caption1)
                .foregroundColor(AppColors.textSecondary)
        }
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
                        VStack(spacing: AppSpacing.sm) {
                            Text("FODMAP data based on research by Monash University, the leading authority on the low-FODMAP diet.")
                                .font(AppTypography.caption1)
                                .foregroundColor(AppColors.textTertiary)
                                .multilineTextAlignment(.center)
                            if let url = URL(string: "https://www.monashfodmap.com") {
                                Link("monashfodmap.com", destination: url)
                                    .font(AppTypography.caption1)
                                    .foregroundColor(AppColors.primary)
                            }
                            Text("This is an educational wellness tool. It is not intended to diagnose, treat, or cure any medical condition. Consult a dietitian for personalized FODMAP guidance.")
                                .font(AppTypography.caption1)
                                .foregroundColor(AppColors.textTertiary)
                                .multilineTextAlignment(.center)
                        }
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
                Text(food.fodmapLevel.rawValue.capitalized)
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
        foods = FodmapRepository.shared.foods
        await FodmapRepository.shared.refreshIfNeeded()
        foods = FodmapRepository.shared.foods
        isLoading = false
    }
}
