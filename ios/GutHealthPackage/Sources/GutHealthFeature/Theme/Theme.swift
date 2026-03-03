import SwiftUI

// MARK: - Colors (System Adaptive — Light + Dark)

extension Color {
    init(hex: String) {
        let hex = hex.trimmingCharacters(in: CharacterSet.alphanumerics.inverted)
        var int: UInt64 = 0
        Scanner(string: hex).scanHexInt64(&int)
        let a, r, g, b: UInt64
        switch hex.count {
        case 3:
            (a, r, g, b) = (255, (int >> 8) * 17, (int >> 4 & 0xF) * 17, (int & 0xF) * 17)
        case 6:
            (a, r, g, b) = (255, int >> 16, int >> 8 & 0xFF, int & 0xFF)
        case 8:
            (a, r, g, b) = (int >> 24, int >> 16 & 0xFF, int >> 8 & 0xFF, int & 0xFF)
        default:
            (a, r, g, b) = (255, 0, 0, 0)
        }
        self.init(.sRGB, red: Double(r) / 255, green: Double(g) / 255, blue: Double(b) / 255, opacity: Double(a) / 255)
    }
}

public struct AppColors {
    // Primary — teal/green wellness palette
    public static let primary = Color(light: Color(hex: "00897B"), dark: Color(hex: "4DB6AC"))
    public static let primaryContainer = Color(light: Color(hex: "E0F2F1"), dark: Color(hex: "1A3A36"))

    // Backgrounds
    public static let background = Color(light: .white, dark: Color(hex: "0D1117"))
    public static let surface = Color(light: Color(hex: "F5F7FA"), dark: Color(hex: "1C2128"))
    public static let surfaceElevated = Color(light: .white, dark: Color(hex: "252B33"))

    // Text
    public static let text = Color(light: Color(hex: "1A1A1A"), dark: .white)
    public static let textSecondary = Color(light: Color(hex: "6B7280"), dark: Color(hex: "8B949E"))
    public static let textTertiary = Color(light: Color(hex: "9CA3AF"), dark: Color(hex: "636366"))

    // FODMAP levels
    public static let fodmapLow = Color(hex: "66BB6A")
    public static let fodmapModerate = Color(hex: "FFA726")
    public static let fodmapHigh = Color(hex: "E57373")

    // Semantic
    public static let success = Color(hex: "66BB6A")
    public static let warning = Color(hex: "FFA726")
    public static let error = Color(hex: "E57373")

    // UI elements
    public static let border = Color(light: Color(hex: "E5E7EB"), dark: Color(hex: "30363D"))
    public static let inputBackground = Color(light: Color(hex: "F9FAFB"), dark: Color(hex: "161B22"))

    // Bristol chart gradient (type 1-7)
    public static let bristolColors: [Color] = [
        Color(hex: "8B4513"), // 1 - hard
        Color(hex: "A0522D"), // 2
        Color(hex: "CD853F"), // 3
        Color(hex: "66BB6A"), // 4 - ideal
        Color(hex: "FFA726"), // 5
        Color(hex: "FF7043"), // 6
        Color(hex: "E57373"), // 7 - liquid
    ]
}

// MARK: - Adaptive Color Helper

extension Color {
    init(light: Color, dark: Color) {
        self.init(uiColor: UIColor { traitCollection in
            traitCollection.userInterfaceStyle == .dark
                ? UIColor(dark)
                : UIColor(light)
        })
    }
}

// MARK: - Spacing

public struct AppSpacing {
    public static let xs: CGFloat = 4
    public static let sm: CGFloat = 8
    public static let md: CGFloat = 16
    public static let lg: CGFloat = 24
    public static let xl: CGFloat = 32
    public static let xxl: CGFloat = 48
}

// MARK: - Border Radius

public struct AppRadius {
    public static let sm: CGFloat = 8
    public static let md: CGFloat = 12
    public static let lg: CGFloat = 16
    public static let xl: CGFloat = 24
    public static let full: CGFloat = 9999
}

// MARK: - Typography

public struct AppTypography {
    public static let largeTitle = Font.system(size: 34, weight: .bold)
    public static let title1 = Font.system(size: 28, weight: .bold)
    public static let title2 = Font.system(size: 22, weight: .bold)
    public static let title3 = Font.system(size: 20, weight: .semibold)
    public static let headline = Font.system(size: 17, weight: .semibold)
    public static let body = Font.system(size: 17, weight: .regular)
    public static let callout = Font.system(size: 16, weight: .regular)
    public static let subhead = Font.system(size: 15, weight: .regular)
    public static let footnote = Font.system(size: 13, weight: .regular)
    public static let caption1 = Font.system(size: 12, weight: .regular)
    public static let caption2 = Font.system(size: 11, weight: .regular)
}

// MARK: - Button Styles

public struct PrimaryButtonStyle: ButtonStyle {
    public var isEnabled: Bool = true

    public init(isEnabled: Bool = true) {
        self.isEnabled = isEnabled
    }

    public func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .font(AppTypography.headline)
            .foregroundColor(.white)
            .frame(maxWidth: .infinity)
            .padding(.vertical, AppSpacing.md)
            .background(AppColors.primary)
            .cornerRadius(AppRadius.lg)
            .opacity(isEnabled ? (configuration.isPressed ? 0.8 : 1.0) : 0.5)
    }
}

public struct SecondaryButtonStyle: ButtonStyle {
    public init() {}

    public func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .font(AppTypography.headline)
            .foregroundColor(AppColors.primary)
            .frame(maxWidth: .infinity)
            .padding(.vertical, AppSpacing.md)
            .background(AppColors.surface)
            .cornerRadius(AppRadius.lg)
            .overlay(
                RoundedRectangle(cornerRadius: AppRadius.lg)
                    .stroke(AppColors.primary, lineWidth: 1.5)
            )
            .opacity(configuration.isPressed ? 0.8 : 1.0)
    }
}

// MARK: - Card Modifier

public struct CardStyle: ViewModifier {
    public init() {}

    public func body(content: Content) -> some View {
        content
            .background(AppColors.surfaceElevated)
            .cornerRadius(AppRadius.lg)
            .shadow(color: .black.opacity(0.05), radius: 8, x: 0, y: 2)
    }
}

extension View {
    public func cardStyle() -> some View {
        modifier(CardStyle())
    }
}
