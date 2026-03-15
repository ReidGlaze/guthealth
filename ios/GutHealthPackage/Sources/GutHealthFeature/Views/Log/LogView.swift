import SwiftUI
import PhotosUI
import UIKit
import StoreKit

public struct LogView: View {
    @State private var showingMealLog = false
    @State private var showingSymptomLog = false
    @State private var showingPoopLog = false
    @State private var successMessage: String?

    public init() {}

    public var body: some View {
        NavigationStack {
            ScrollView {
                VStack(spacing: AppSpacing.lg) {
                    quickLogButtons
                    disclaimerView
                }
                .padding(.horizontal, AppSpacing.md)
                .padding(.top, AppSpacing.sm)
            }
            .background(AppColors.background)
            .navigationTitle("Log")
            .navigationBarTitleDisplayMode(.inline)
            .overlay {
                if let msg = successMessage {
                    successToast(msg)
                }
            }
            .fullScreenCover(isPresented: $showingMealLog) {
                MealLogSheet(successMessage: $successMessage)
            }
            .fullScreenCover(isPresented: $showingSymptomLog) {
                SymptomLogSheet(successMessage: $successMessage)
            }
            .fullScreenCover(isPresented: $showingPoopLog) {
                PoopLogSheet(successMessage: $successMessage)
            }
        }
    }

    // MARK: - Quick Log Buttons

    private var quickLogButtons: some View {
        VStack(spacing: AppSpacing.md) {
            Text("What would you like to log?")
                .font(AppTypography.title3)
                .foregroundColor(AppColors.text)
                .frame(maxWidth: .infinity, alignment: .leading)

            logButton(title: "Meal", subtitle: "Snap a photo or manually enter foods", icon: "camera.fill", color: AppColors.primary) {
                showingMealLog = true
            }

            logButton(title: "Symptom", subtitle: "Track bloating, pain, gas, and more", icon: "waveform.path.ecg", color: AppColors.warning) {
                showingSymptomLog = true
            }

            logButton(title: "Poop", subtitle: "Bristol Stool Chart classification", icon: "drop.fill", color: AppColors.fodmapModerate) {
                showingPoopLog = true
            }
        }
    }

    private func logButton(title: String, subtitle: String, icon: String, color: Color, action: @escaping () -> Void) -> some View {
        Button(action: action) {
            HStack(spacing: AppSpacing.md) {
                ZStack {
                    Circle()
                        .fill(color.opacity(0.15))
                        .frame(width: 56, height: 56)
                    Image(systemName: icon)
                        .font(.title2)
                        .foregroundColor(color)
                }

                VStack(alignment: .leading, spacing: 4) {
                    Text(title)
                        .font(AppTypography.headline)
                        .foregroundColor(AppColors.text)
                    Text(subtitle)
                        .font(AppTypography.footnote)
                        .foregroundColor(AppColors.textSecondary)
                }

                Spacer()

                Image(systemName: "chevron.right")
                    .foregroundColor(AppColors.textTertiary)
            }
            .padding(AppSpacing.md)
            .cardStyle()
        }
        .buttonStyle(.plain)
    }

    // MARK: - Disclaimer

    private var disclaimerView: some View {
        VStack(spacing: AppSpacing.xs) {
            Text("FODMAP data based on Monash University research. Bristol Stool Chart: Lewis & Heaton, 1997. Always consult your doctor before making health decisions.")
                .font(AppTypography.caption2)
                .foregroundColor(AppColors.textTertiary)
                .multilineTextAlignment(.center)
            Text("This is an educational wellness tool. It is not intended to diagnose, treat, or cure any medical condition. This is not medical advice.")
                .font(AppTypography.caption2)
                .foregroundColor(AppColors.textTertiary)
                .multilineTextAlignment(.center)
        }
        .padding(AppSpacing.md)
    }

    // MARK: - Success Toast

    private func successToast(_ message: String) -> some View {
        VStack {
            Spacer()
            Text(message)
                .font(AppTypography.headline)
                .foregroundColor(.white)
                .padding(.horizontal, AppSpacing.lg)
                .padding(.vertical, AppSpacing.md)
                .background(AppColors.success)
                .cornerRadius(AppRadius.xl)
                .shadow(radius: 8)
                .padding(.bottom, AppSpacing.xxl)
        }
        .transition(.move(edge: .bottom).combined(with: .opacity))
        .animation(.spring, value: successMessage)
    }
}

// MARK: - Meal Log Sheet

struct MealLogSheet: View {
    @Environment(\.dismiss) private var dismiss
    @Binding var successMessage: String?

    @State private var selectedMealType: MealType = Self.mealTypeForCurrentTime()
    @State private var selectedDate = Date()
    @State private var foods: [MealFood] = []
    @State private var newFoodName = ""
    @State private var notes = ""
    @State private var capturedImage: UIImage?
    @State private var showingCamera = false
    @State private var selectedPhotoItem: PhotosPickerItem?
    @State private var isAnalyzing = false
    @State private var analysisComplete = false
    @State private var errorMessage: String?

    private let authService = AuthService.shared

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(spacing: AppSpacing.lg) {
                    mealTypePicker
                    datePicker
                    photoSection
                    manualFoodEntry
                    foodsList
                    notesField

                    if analysisComplete {
                        disclaimerBanner
                    }

                    saveButton

                    mealDisclaimerView
                }
                .padding(AppSpacing.md)
            }
            .background(AppColors.background)
            .navigationTitle("Log Meal")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Cancel") { dismiss() }
                }
            }
            .sheet(isPresented: $showingCamera) {
                CameraPicker(image: $capturedImage)
            }
            .onChange(of: selectedPhotoItem) { _, newItem in
                Task {
                    if let newItem, let data = try? await newItem.loadTransferable(type: Data.self) {
                        capturedImage = UIImage(data: data)
                    }
                    selectedPhotoItem = nil
                }
            }
            .onChange(of: capturedImage) { _, newImage in
                if newImage != nil {
                    Task { await analyzePhoto() }
                }
            }
            .alert("Error", isPresented: .init(get: { errorMessage != nil }, set: { if !$0 { errorMessage = nil } })) {
                Button("OK", role: .cancel) {}
            } message: {
                Text(errorMessage ?? "")
            }
        }
    }

    private var mealTypePicker: some View {
        Picker("Meal Type", selection: $selectedMealType) {
            ForEach(MealType.allCases, id: \.self) { type in
                Text(type.rawValue.capitalized).tag(type)
            }
        }
        .pickerStyle(.segmented)
    }

    private var datePicker: some View {
        DatePicker("When did you eat this?", selection: $selectedDate, in: ...Date(), displayedComponents: [.date, .hourAndMinute])
            .datePickerStyle(.compact)
    }

    private var photoSection: some View {
        VStack(alignment: .leading, spacing: AppSpacing.sm) {
            Text("Food Photo")
                .font(AppTypography.headline)
                .foregroundColor(AppColors.text)

            if let image = capturedImage {
                Image(uiImage: image)
                    .resizable()
                    .scaledToFill()
                    .frame(height: 200)
                    .clipShape(RoundedRectangle(cornerRadius: AppRadius.md))

                HStack(spacing: AppSpacing.md) {
                    if UIImagePickerController.isSourceTypeAvailable(.camera) {
                        Button {
                            showingCamera = true
                        } label: {
                            Label("Retake", systemImage: "camera")
                                .font(AppTypography.subhead)
                        }
                    }

                    PhotosPicker(selection: $selectedPhotoItem, matching: .images) {
                        Label("Library", systemImage: "photo.on.rectangle")
                            .font(AppTypography.subhead)
                    }

                    if isAnalyzing {
                        HStack(spacing: AppSpacing.xs) {
                            ProgressView()
                                .tint(AppColors.primary)
                            Text("Analyzing...")
                                .font(AppTypography.subhead)
                        }
                    }
                }
                .foregroundColor(AppColors.primary)
            } else {
                HStack(spacing: AppSpacing.md) {
                    if UIImagePickerController.isSourceTypeAvailable(.camera) {
                        Button {
                            showingCamera = true
                        } label: {
                            VStack(spacing: AppSpacing.sm) {
                                Image(systemName: "camera.fill")
                                    .font(.system(size: 28))
                                    .foregroundColor(AppColors.primary)
                                Text("Camera")
                                    .font(AppTypography.headline)
                                    .foregroundColor(AppColors.primary)
                            }
                            .frame(maxWidth: .infinity)
                            .padding(AppSpacing.lg)
                            .background(AppColors.primaryContainer)
                            .cornerRadius(AppRadius.lg)
                        }
                        .buttonStyle(.plain)
                    }

                    PhotosPicker(selection: $selectedPhotoItem, matching: .images) {
                        VStack(spacing: AppSpacing.sm) {
                            Image(systemName: "photo.on.rectangle")
                                .font(.system(size: 28))
                                .foregroundColor(AppColors.primary)
                            Text("Library")
                                .font(AppTypography.headline)
                                .foregroundColor(AppColors.primary)
                        }
                        .frame(maxWidth: .infinity)
                        .padding(AppSpacing.lg)
                        .background(AppColors.primaryContainer)
                        .cornerRadius(AppRadius.lg)
                    }
                    .buttonStyle(.plain)
                }

                Text("AI will identify foods and FODMAP levels")
                    .font(AppTypography.caption1)
                    .foregroundColor(AppColors.textSecondary)
            }
        }
    }

    private var manualFoodEntry: some View {
        VStack(alignment: .leading, spacing: AppSpacing.sm) {
            Text("Add Food Manually")
                .font(AppTypography.headline)
                .foregroundColor(AppColors.text)

            HStack {
                TextField("e.g., Grilled chicken", text: $newFoodName)
                    .textFieldStyle(.roundedBorder)

                Button {
                    addFood()
                } label: {
                    Image(systemName: "plus.circle.fill")
                        .font(.title2)
                        .foregroundColor(AppColors.primary)
                }
                .disabled(newFoodName.trimmingCharacters(in: .whitespaces).isEmpty)
            }
        }
    }

    @ViewBuilder
    private var foodsList: some View {
        if !foods.isEmpty {
            VStack(alignment: .leading, spacing: AppSpacing.sm) {
                Text("Foods (\(foods.count))")
                    .font(AppTypography.headline)
                    .foregroundColor(AppColors.text)

                ForEach(Array(foods.enumerated()), id: \.offset) { index, food in
                    HStack(spacing: AppSpacing.sm) {
                        Circle()
                            .fill(fodmapColor(food.fodmapLevel))
                            .frame(width: 10, height: 10)

                        Text(food.name)
                            .font(AppTypography.body)
                            .foregroundColor(AppColors.text)

                        Spacer()

                        Text(food.fodmapLevel.rawValue.uppercased())
                            .font(AppTypography.caption2)
                            .fontWeight(.semibold)
                            .foregroundColor(fodmapColor(food.fodmapLevel))
                            .padding(.horizontal, 8)
                            .padding(.vertical, 3)
                            .background(fodmapColor(food.fodmapLevel).opacity(0.15))
                            .cornerRadius(AppRadius.sm)

                        Button {
                            foods.remove(at: index)
                        } label: {
                            Image(systemName: "xmark.circle.fill")
                                .foregroundColor(AppColors.textTertiary)
                        }
                    }
                    .padding(AppSpacing.sm)
                    .background(AppColors.surface)
                    .cornerRadius(AppRadius.sm)

                    // Serving size warning — use lowFodmapServing from AI response,
                    // fall back to local FODMAP database for manually-added foods.
                    let safeServing = food.lowFodmapServing.isEmpty
                        ? (FodmapRepository.shared.lookup(food.name)?.lowFodmapServing ?? "")
                        : food.lowFodmapServing
                    if !safeServing.isEmpty,
                       !food.servingSize.isEmpty,
                       food.servingSize.lowercased() != safeServing.lowercased() {
                        HStack(spacing: AppSpacing.xs) {
                            Image(systemName: "exclamationmark.triangle.fill")
                                .font(AppTypography.caption1)
                                .foregroundColor(AppColors.warning)
                            Text("Estimated: \(food.servingSize) | Safe: \(safeServing)")
                                .font(AppTypography.caption2)
                                .foregroundColor(AppColors.warning)
                        }
                        .padding(.horizontal, AppSpacing.sm)
                        .padding(.vertical, AppSpacing.xs)
                        .background(AppColors.warning.opacity(0.1))
                        .cornerRadius(AppRadius.sm)
                    }
                }
            }
        }
    }

    private var notesField: some View {
        TextField("Notes (optional)", text: $notes, axis: .vertical)
            .textFieldStyle(.roundedBorder)
            .lineLimit(3)
    }

    private var disclaimerBanner: some View {
        HStack(spacing: AppSpacing.sm) {
            Image(systemName: "info.circle")
                .foregroundColor(AppColors.warning)
            Text("This is not medical advice.")
                .font(AppTypography.caption2)
                .foregroundColor(AppColors.textSecondary)
        }
        .padding(AppSpacing.sm)
        .background(AppColors.warning.opacity(0.1))
        .cornerRadius(AppRadius.sm)
    }

    private var mealDisclaimerView: some View {
        Text("This is an educational wellness tool. It is not intended to diagnose, treat, or cure any medical condition. This is not medical advice.")
            .font(AppTypography.caption2)
            .foregroundColor(AppColors.textTertiary)
            .multilineTextAlignment(.center)
            .padding(AppSpacing.md)
    }

    private var saveButton: some View {
        Button("Save Meal") {
            Task { await saveMeal() }
        }
        .buttonStyle(PrimaryButtonStyle(isEnabled: !foods.isEmpty))
        .disabled(foods.isEmpty)
    }

    private func addFood() {
        let name = newFoodName.trimmingCharacters(in: .whitespaces)
        guard !name.isEmpty else { return }
        if let match = FodmapRepository.shared.lookup(name) {
            foods.append(MealFood(
                name: name,
                fodmapLevel: match.fodmapLevel,
                fodmapCategories: match.fodmapCategories,
                servingSize: match.servingSize,
                lowFodmapServing: match.lowFodmapServing,
                triggers: match.fodmapCategories
            ))
        } else {
            foods.append(MealFood(name: name, fodmapLevel: .unknown))
        }
        newFoodName = ""
    }

    private func analyzePhoto() async {
        guard let image = capturedImage,
              let data = image.jpegData(compressionQuality: 0.7) else { return }

        isAnalyzing = true
        do {
            let base64 = data.base64EncodedString()
            let result = try await FunctionsService.shared.analyzeFoodPhoto(imageBase64: base64, mealType: selectedMealType.rawValue)
            foods = result
            analysisComplete = true
            UINotificationFeedbackGenerator().notificationOccurred(.success)
        } catch {
            errorMessage = "Unable to analyze photo. You can still add foods manually."
            UINotificationFeedbackGenerator().notificationOccurred(.error)
        }
        isAnalyzing = false
    }

    private func saveMeal() async {
        guard let uid = authService.currentUserId else { return }
        var meal = Meal(createdAt: selectedDate, mealType: selectedMealType, foods: foods, notes: notes)
        if analysisComplete {
            meal.aiAnalysis = AIAnalysis(rawResponse: "AI analyzed", analyzedAt: Date())
        }
        guard let mealId = try? await FirestoreService.shared.saveMeal(uid: uid, meal: meal) else { return }

        // Upload photo to Storage in background (non-blocking)
        if let image = capturedImage {
            Task.detached {
                do {
                    let photoUrl = try await StorageService.shared.uploadMealPhoto(uid: uid, mealId: mealId, image: image)
                    try await FirestoreService.shared.updateMealPhotoUrl(uid: uid, mealId: mealId, photoUrl: photoUrl)
                } catch {
                    print("Failed to upload meal photo: \(error)")
                }
            }
        }

        UINotificationFeedbackGenerator().notificationOccurred(.success)
        dismiss()
        showSuccess("Meal logged!")
    }

    private static func mealTypeForCurrentTime() -> MealType {
        let hour = Calendar.current.component(.hour, from: Date())
        switch hour {
        case 5..<11: return .breakfast
        case 11..<15: return .lunch
        case 15..<21: return .dinner
        default: return .snack
        }
    }

    private func fodmapColor(_ level: FodmapLevel) -> Color {
        switch level {
        case .low: return AppColors.fodmapLow
        case .moderate: return AppColors.fodmapModerate
        case .high: return AppColors.fodmapHigh
        case .unknown: return AppColors.textTertiary
        }
    }

    private func showSuccess(_ message: String) {
        successMessage = message
        Task {
            try? await Task.sleep(for: .seconds(2))
            successMessage = nil
        }
    }
}

// MARK: - Symptom Log Sheet

struct SymptomLogSheet: View {
    @Environment(\.dismiss) private var dismiss
    @Binding var successMessage: String?

    @State private var selectedType: SymptomType = .bloating
    @State private var selectedDate = Date()
    @State private var severity: Double = 5
    @State private var selectedLocation: SymptomLocation?
    @State private var notes = ""
    private let authService = AuthService.shared

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(spacing: AppSpacing.lg) {
                    symptomTypePicker
                    symptomDatePicker
                    severitySlider
                    locationPicker
                    notesField
                    saveButton
                    symptomDisclaimerView
                }
                .padding(AppSpacing.md)
            }
            .background(AppColors.background)
            .navigationTitle("Log Symptom")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Cancel") { dismiss() }
                }
            }
        }
    }

    private var symptomTypePicker: some View {
        VStack(alignment: .leading, spacing: AppSpacing.sm) {
            Text("Symptom Type")
                .font(AppTypography.headline)
                .foregroundColor(AppColors.text)

            LazyVGrid(columns: Array(repeating: GridItem(.flexible(), spacing: AppSpacing.sm), count: 4), spacing: AppSpacing.sm) {
                ForEach(SymptomType.allCases, id: \.self) { type in
                    Button {
                        selectedType = type
                    } label: {
                        VStack(spacing: 4) {
                            Image(systemName: type.icon)
                                .font(.title3)
                            Text(type.rawValue.capitalized)
                                .font(AppTypography.caption2)
                                .lineLimit(1)
                                .minimumScaleFactor(0.8)
                        }
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, AppSpacing.sm)
                        .background(selectedType == type ? AppColors.primary.opacity(0.15) : AppColors.surface)
                        .foregroundColor(selectedType == type ? AppColors.primary : AppColors.textSecondary)
                        .cornerRadius(AppRadius.md)
                    }
                    .buttonStyle(.plain)
                }
            }
        }
    }

    private var symptomDatePicker: some View {
        DatePicker("When did this start?", selection: $selectedDate, in: ...Date(), displayedComponents: [.date, .hourAndMinute])
            .datePickerStyle(.compact)
    }

    private var severitySlider: some View {
        VStack(alignment: .leading, spacing: AppSpacing.sm) {
            HStack {
                Text("Severity")
                    .font(AppTypography.headline)
                    .foregroundColor(AppColors.text)
                Spacer()
                Text("\(Int(severity))/10")
                    .font(AppTypography.title3)
                    .foregroundColor(severityColor)
            }

            Slider(value: $severity, in: 1...10, step: 1)
                .tint(severityColor)
                .sensoryFeedback(.selection, trigger: severity)

            HStack {
                Text("Mild")
                    .font(AppTypography.caption2)
                    .foregroundColor(AppColors.success)
                Spacer()
                Text("Severe")
                    .font(AppTypography.caption2)
                    .foregroundColor(AppColors.error)
            }
        }
    }

    private var severityColor: Color {
        if severity <= 3 { return AppColors.success }
        if severity <= 6 { return AppColors.warning }
        return AppColors.error
    }

    private var locationPicker: some View {
        VStack(alignment: .leading, spacing: AppSpacing.sm) {
            Text("Location (optional)")
                .font(AppTypography.headline)
                .foregroundColor(AppColors.text)

            HStack(spacing: AppSpacing.sm) {
                ForEach(SymptomLocation.allCases, id: \.self) { location in
                    Button {
                        selectedLocation = selectedLocation == location ? nil : location
                    } label: {
                        Text(location.rawValue.capitalized)
                            .font(AppTypography.subhead)
                            .padding(.horizontal, AppSpacing.md)
                            .padding(.vertical, AppSpacing.sm)
                            .background(selectedLocation == location ? AppColors.primary.opacity(0.15) : AppColors.surface)
                            .foregroundColor(selectedLocation == location ? AppColors.primary : AppColors.textSecondary)
                            .cornerRadius(AppRadius.full)
                    }
                    .buttonStyle(.plain)
                }
            }
        }
    }

    private var notesField: some View {
        VStack(alignment: .leading, spacing: AppSpacing.sm) {
            Text("Notes (optional)")
                .font(AppTypography.headline)
                .foregroundColor(AppColors.text)

            TextField("Any additional details...", text: $notes, axis: .vertical)
                .textFieldStyle(.roundedBorder)
                .lineLimit(3)
        }
    }

    private var saveButton: some View {
        Button("Save Symptom") {
            Task { await saveSymptom() }
        }
        .buttonStyle(PrimaryButtonStyle())
    }

    private var symptomDisclaimerView: some View {
        Text("This is an educational wellness tool. It is not intended to diagnose, treat, or cure any medical condition. This is not medical advice.")
            .font(AppTypography.caption2)
            .foregroundColor(AppColors.textTertiary)
            .multilineTextAlignment(.center)
            .padding(AppSpacing.md)
    }

    private func saveSymptom() async {
        guard let uid = authService.currentUserId else { return }
        let symptom = Symptom(
            createdAt: selectedDate,
            type: selectedType,
            severity: Int(severity),
            location: selectedLocation,
            notes: notes
        )
        _ = try? await FirestoreService.shared.saveSymptom(uid: uid, symptom: symptom)
        UINotificationFeedbackGenerator().notificationOccurred(.success)
        dismiss()
        successMessage = "Symptom logged!"
        Task {
            try? await Task.sleep(for: .seconds(2))
            successMessage = nil
        }
    }
}

// MARK: - Poop Log Sheet

struct PoopLogSheet: View {
    @Environment(\.dismiss) private var dismiss
    @Binding var successMessage: String?

    @State private var selectedBristolType: Int? = nil
    @State private var selectedDate = Date()
    @State private var selectedColor: StoolColor? = nil
    @State private var selectedUrgency: Urgency? = nil
    @State private var capturedImage: UIImage?
    @State private var showingCamera = false
    @State private var selectedPhotoItem: PhotosPickerItem?
    @State private var isClassifying = false
    @State private var notes = ""
    @State private var errorMessage: String?

    private let authService = AuthService.shared

    private let bristolEmojis: [Int: String] = [
        1: "🫘", 2: "🌰", 3: "🥖", 4: "🍌", 5: "🫧", 6: "☁️", 7: "💧"
    ]

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(spacing: AppSpacing.lg) {
                    photoSection
                    poopDatePicker
                    bristolChartPicker
                    colorPicker
                    redBlackStoolWarningBanner
                    urgencyPicker
                    notesField
                    saveButton
                    poopDisclaimerView
                }
                .padding(AppSpacing.md)
            }
            .background(AppColors.background)
            .navigationTitle("Log Poop")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Cancel") { dismiss() }
                }
            }
            .sheet(isPresented: $showingCamera) {
                CameraPicker(image: $capturedImage)
            }
            .onChange(of: selectedPhotoItem) { _, newItem in
                Task {
                    if let newItem, let data = try? await newItem.loadTransferable(type: Data.self) {
                        capturedImage = UIImage(data: data)
                    }
                    selectedPhotoItem = nil
                }
            }
            .onChange(of: capturedImage) { _, newImage in
                if newImage != nil {
                    Task { await classifyPhoto() }
                }
            }
            .alert("Error", isPresented: .init(get: { errorMessage != nil }, set: { if !$0 { errorMessage = nil } })) {
                Button("OK", role: .cancel) {}
            } message: {
                Text(errorMessage ?? "")
            }
        }
    }

    private var bristolChartPicker: some View {
        VStack(alignment: .leading, spacing: AppSpacing.sm) {
            Text("Bristol Stool Type")
                .font(AppTypography.headline)
                .foregroundColor(AppColors.text)

            ForEach(1...7, id: \.self) { type in
                Button {
                    selectedBristolType = type
                } label: {
                    HStack(spacing: AppSpacing.md) {
                        Text(bristolEmojis[type] ?? "")
                            .font(.title2)
                            .frame(width: 36)

                        VStack(alignment: .leading, spacing: 2) {
                            Text("Type \(type)")
                                .font(AppTypography.headline)
                                .foregroundColor(selectedBristolType == type ? AppColors.text : AppColors.textSecondary)
                            Text(PoopLog.bristolDescriptions[type] ?? "")
                                .font(AppTypography.caption1)
                                .foregroundColor(AppColors.textTertiary)
                        }

                        Spacer()

                        if selectedBristolType == type {
                            Image(systemName: "checkmark.circle.fill")
                                .foregroundColor(bristolColor(type))
                        }
                    }
                    .padding(AppSpacing.sm)
                    .background(selectedBristolType == type ? bristolColor(type).opacity(0.1) : AppColors.surface)
                    .cornerRadius(AppRadius.md)
                    .overlay(
                        RoundedRectangle(cornerRadius: AppRadius.md)
                            .stroke(selectedBristolType == type ? bristolColor(type).opacity(0.4) : Color.clear, lineWidth: 1.5)
                    )
                }
                .buttonStyle(.plain)
            }
        }
    }

    private var poopDatePicker: some View {
        DatePicker("When did this happen?", selection: $selectedDate, in: ...Date(), displayedComponents: [.date, .hourAndMinute])
            .datePickerStyle(.compact)
    }

    private func bristolColor(_ type: Int) -> Color {
        guard type >= 1 && type <= 7 else { return AppColors.textTertiary }
        return AppColors.bristolColors[type - 1]
    }

    private var colorPicker: some View {
        VStack(alignment: .leading, spacing: AppSpacing.sm) {
            Text("Color")
                .font(AppTypography.headline)
                .foregroundColor(AppColors.text)

            LazyVGrid(columns: Array(repeating: GridItem(.flexible(), spacing: AppSpacing.sm), count: 4), spacing: AppSpacing.sm) {
                ForEach(StoolColor.allCases, id: \.self) { color in
                    Button {
                        selectedColor = color
                    } label: {
                        VStack(spacing: 4) {
                            Circle()
                                .fill(stoolDisplayColor(color))
                                .frame(width: 28, height: 28)
                                .overlay {
                                    if selectedColor == color {
                                        Circle()
                                            .stroke(Color.white, lineWidth: 2)
                                            .frame(width: 20, height: 20)
                                    }
                                }
                            Text(color.rawValue.capitalized)
                                .font(AppTypography.caption2)
                                .foregroundColor(selectedColor == color ? AppColors.text : AppColors.textSecondary)
                        }
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, AppSpacing.sm)
                        .background(selectedColor == color ? AppColors.primaryContainer : AppColors.surface)
                        .cornerRadius(AppRadius.md)
                        .overlay(
                            RoundedRectangle(cornerRadius: AppRadius.md)
                                .stroke(selectedColor == color ? AppColors.primary.opacity(0.5) : Color.clear, lineWidth: 1.5)
                        )
                    }
                    .buttonStyle(.plain)
                }
            }
        }
    }

    private func stoolDisplayColor(_ color: StoolColor) -> Color {
        switch color {
        case .brown: return Color(hex: "8B4513")
        case .dark: return Color(hex: "3E2723")
        case .light: return Color(hex: "D2B48C")
        case .green: return Color(hex: "4CAF50")
        case .yellow: return Color(hex: "FFC107")
        case .red: return Color(hex: "F44336")
        case .black: return Color(hex: "212121")
        }
    }

    @ViewBuilder
    private var redBlackStoolWarningBanner: some View {
        if selectedColor == .red || selectedColor == .black {
            HStack(alignment: .top, spacing: AppSpacing.sm) {
                Image(systemName: "exclamationmark.triangle.fill")
                    .font(.system(size: 18))
                    .foregroundColor(Color(hex: "D84315"))

                VStack(alignment: .leading, spacing: AppSpacing.xs) {
                    Text("Red or black stool can indicate bleeding.")
                        .font(AppTypography.subhead)
                        .fontWeight(.semibold)
                        .foregroundColor(Color(hex: "B71C1C"))

                    Text("If you haven't eaten foods that could cause this color (beets, iron supplements, etc.), please seek medical attention.")
                        .font(AppTypography.footnote)
                        .foregroundColor(Color(hex: "C62828"))
                }
            }
            .padding(AppSpacing.md)
            .frame(maxWidth: .infinity, alignment: .leading)
            .background(Color(hex: "FFEBEE"))
            .overlay(
                RoundedRectangle(cornerRadius: AppRadius.md)
                    .stroke(Color(hex: "EF9A9A"), lineWidth: 1.5)
            )
            .cornerRadius(AppRadius.md)
        }
    }

    private var urgencyPicker: some View {
        VStack(alignment: .leading, spacing: AppSpacing.sm) {
            Text("Urgency")
                .font(AppTypography.headline)
                .foregroundColor(AppColors.text)

            HStack(spacing: AppSpacing.sm) {
                ForEach(Urgency.allCases, id: \.self) { urgency in
                    Button {
                        selectedUrgency = selectedUrgency == urgency ? nil : urgency
                    } label: {
                        let isSelected = selectedUrgency == urgency
                        HStack(spacing: AppSpacing.xs) {
                            Image(systemName: urgencyIcon(urgency))
                                .font(.caption)
                            Text(urgency.rawValue.capitalized)
                                .font(AppTypography.subhead)
                        }
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, AppSpacing.sm)
                        .background(isSelected ? urgencyColor(urgency).opacity(0.15) : AppColors.surface)
                        .foregroundColor(isSelected ? urgencyColor(urgency) : AppColors.textSecondary)
                        .cornerRadius(AppRadius.md)
                    }
                    .buttonStyle(.plain)
                }
            }
        }
    }

    private func urgencyIcon(_ urgency: Urgency) -> String {
        switch urgency {
        case .normal: return "checkmark.circle"
        case .urgent: return "exclamationmark.circle"
        case .emergency: return "exclamationmark.triangle.fill"
        }
    }

    private func urgencyColor(_ urgency: Urgency) -> Color {
        switch urgency {
        case .normal: return AppColors.success
        case .urgent: return AppColors.warning
        case .emergency: return AppColors.error
        }
    }

    private var photoSection: some View {
        VStack(alignment: .leading, spacing: AppSpacing.sm) {
            Text("Poop Photo (Optional)")
                .font(AppTypography.headline)
                .foregroundColor(AppColors.text)

            if let image = capturedImage {
                Image(uiImage: image)
                    .resizable()
                    .scaledToFill()
                    .frame(height: 150)
                    .clipShape(RoundedRectangle(cornerRadius: AppRadius.md))

                HStack(spacing: AppSpacing.md) {
                    if UIImagePickerController.isSourceTypeAvailable(.camera) {
                        Button {
                            showingCamera = true
                        } label: {
                            Label("Retake", systemImage: "camera")
                                .font(AppTypography.subhead)
                        }
                    }

                    PhotosPicker(selection: $selectedPhotoItem, matching: .images) {
                        Label("Library", systemImage: "photo.on.rectangle")
                            .font(AppTypography.subhead)
                    }
                }
                .foregroundColor(AppColors.primary)

                if isClassifying {
                    HStack(spacing: AppSpacing.sm) {
                        ProgressView()
                            .tint(AppColors.primary)
                        Text("Classifying with AI...")
                            .font(AppTypography.subhead)
                            .foregroundColor(AppColors.textSecondary)
                    }
                    .padding(AppSpacing.sm)
                }

                HStack(spacing: AppSpacing.sm) {
                    Image(systemName: "info.circle")
                        .foregroundColor(AppColors.warning)
                    Text("This is not medical advice.")
                        .font(AppTypography.caption2)
                        .foregroundColor(AppColors.textSecondary)
                }
                .padding(AppSpacing.sm)
                .background(AppColors.warning.opacity(0.1))
                .cornerRadius(AppRadius.sm)
            } else {
                HStack(spacing: AppSpacing.md) {
                    if UIImagePickerController.isSourceTypeAvailable(.camera) {
                        Button {
                            showingCamera = true
                        } label: {
                            VStack(spacing: AppSpacing.sm) {
                                Image(systemName: "camera.fill")
                                    .font(.system(size: 28))
                                    .foregroundColor(AppColors.primary)
                                Text("Camera")
                                    .font(AppTypography.headline)
                                    .foregroundColor(AppColors.primary)
                            }
                            .frame(maxWidth: .infinity)
                            .padding(AppSpacing.lg)
                            .background(AppColors.primaryContainer)
                            .cornerRadius(AppRadius.lg)
                        }
                        .buttonStyle(.plain)
                    }

                    PhotosPicker(selection: $selectedPhotoItem, matching: .images) {
                        VStack(spacing: AppSpacing.sm) {
                            Image(systemName: "photo.on.rectangle")
                                .font(.system(size: 28))
                                .foregroundColor(AppColors.primary)
                            Text("Library")
                                .font(AppTypography.headline)
                                .foregroundColor(AppColors.primary)
                        }
                        .frame(maxWidth: .infinity)
                        .padding(AppSpacing.lg)
                        .background(AppColors.primaryContainer)
                        .cornerRadius(AppRadius.lg)
                    }
                    .buttonStyle(.plain)
                }

                Text("AI will classify using the Bristol Stool Chart")
                    .font(AppTypography.caption1)
                    .foregroundColor(AppColors.textSecondary)
            }
        }
    }

    private var notesField: some View {
        VStack(alignment: .leading, spacing: AppSpacing.sm) {
            Text("Notes (optional)")
                .font(AppTypography.headline)
                .foregroundColor(AppColors.text)

            TextField("Any additional details...", text: $notes, axis: .vertical)
                .textFieldStyle(.roundedBorder)
                .lineLimit(3)
        }
    }

    private var poopDisclaimerView: some View {
        Text("This is an educational wellness tool. It is not intended to diagnose, treat, or cure any medical condition. This is not medical advice.")
            .font(AppTypography.caption2)
            .foregroundColor(AppColors.textTertiary)
            .multilineTextAlignment(.center)
            .padding(AppSpacing.md)
    }

    private var saveButton: some View {
        Button("Save Poop Log") {
            Task { await savePoopLog() }
        }
        .buttonStyle(PrimaryButtonStyle())
        .disabled(selectedBristolType == nil || selectedColor == nil)
        .opacity(selectedBristolType == nil || selectedColor == nil ? 0.5 : 1.0)
    }

    private func classifyPhoto() async {
        guard let image = capturedImage,
              let data = image.jpegData(compressionQuality: 0.7) else { return }

        isClassifying = true
        do {
            let base64 = data.base64EncodedString()
            let classification = try await FunctionsService.shared.classifyPoopPhoto(imageBase64: base64)
            selectedBristolType = classification.bristolType
            if let stoolColor = StoolColor(rawValue: classification.color) {
                selectedColor = stoolColor
            }
            if !classification.observations.isEmpty {
                notes = classification.observations
            }
            UINotificationFeedbackGenerator().notificationOccurred(.success)
        } catch {
            errorMessage = "Unable to classify photo. Please select the type manually."
            UINotificationFeedbackGenerator().notificationOccurred(.error)
        }
        isClassifying = false
    }

    private func savePoopLog() async {
        guard let uid = authService.currentUserId,
              let bristolType = selectedBristolType,
              let color = selectedColor else { return }
        let log = PoopLog(
            createdAt: selectedDate,
            bristolType: bristolType,
            color: color,
            urgency: selectedUrgency ?? .normal,
            notes: notes
        )
        guard let logId = try? await FirestoreService.shared.savePoopLog(uid: uid, log: log) else { return }

        // Upload photo to Storage in background (non-blocking)
        if let image = capturedImage {
            Task.detached {
                do {
                    let photoUrl = try await StorageService.shared.uploadPoopPhoto(uid: uid, logId: logId, image: image)
                    try await FirestoreService.shared.updatePoopLogPhotoUrl(uid: uid, logId: logId, photoUrl: photoUrl)
                } catch {
                    print("Failed to upload poop photo: \(error)")
                }
            }
        }

        UINotificationFeedbackGenerator().notificationOccurred(.success)
        dismiss()
        successMessage = "Poop log saved!"
        Task {
            try? await Task.sleep(for: .seconds(2))
            successMessage = nil
        }
    }
}

// MARK: - Camera Picker (camera-only; photo library handled by PhotosPicker)

struct CameraPicker: UIViewControllerRepresentable {
    @Binding var image: UIImage?
    @Environment(\.dismiss) private var dismiss

    func makeUIViewController(context: Context) -> UIImagePickerController {
        let picker = UIImagePickerController()
        picker.delegate = context.coordinator
        picker.sourceType = .camera
        return picker
    }

    func updateUIViewController(_ uiViewController: UIImagePickerController, context: Context) {}

    func makeCoordinator() -> Coordinator { Coordinator(self) }

    class Coordinator: NSObject, UIImagePickerControllerDelegate, UINavigationControllerDelegate {
        let parent: CameraPicker

        init(_ parent: CameraPicker) { self.parent = parent }

        func imagePickerController(_ picker: UIImagePickerController, didFinishPickingMediaWithInfo info: [UIImagePickerController.InfoKey: Any]) {
            if let image = info[.originalImage] as? UIImage {
                parent.image = image
            }
            parent.dismiss()
        }

        func imagePickerControllerDidCancel(_ picker: UIImagePickerController) {
            parent.dismiss()
        }
    }
}
