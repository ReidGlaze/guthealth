import Foundation
import AVFoundation
import UIKit

@MainActor @Observable
public final class CameraService: NSObject {
    public var isAuthorized = false
    public var isCameraReady = false
    public var capturedImage: UIImage?
    public var capturedImagePath: String?
    public var error: CameraError?

    private var captureSession: AVCaptureSession?
    private var photoOutput: AVCapturePhotoOutput?
    private var captureCompletion: ((Result<String, Error>) -> Void)?

    public override init() {
        super.init()
        checkAuthorization()
    }

    public func checkAuthorization() {
        isAuthorized = AVCaptureDevice.authorizationStatus(for: .video) == .authorized
    }

    public func requestPermission() async -> Bool {
        let granted = await AVCaptureDevice.requestAccess(for: .video)
        isAuthorized = granted
        return granted
    }

    public func setupCamera() async throws {
        guard isAuthorized else { throw CameraError.notAuthorized }

        let session = AVCaptureSession()
        session.sessionPreset = .photo

        guard let device = AVCaptureDevice.default(.builtInWideAngleCamera, for: .video, position: .back) else {
            throw CameraError.noCameraAvailable
        }

        do {
            let input = try AVCaptureDeviceInput(device: device)
            if session.canAddInput(input) { session.addInput(input) }

            let output = AVCapturePhotoOutput()
            if session.canAddOutput(output) { session.addOutput(output) }

            self.captureSession = session
            self.photoOutput = output
            session.startRunning()
            self.isCameraReady = true
        } catch {
            throw CameraError.setupFailed
        }
    }

    public func capturePhoto() async throws -> String {
        guard let photoOutput else { throw CameraError.cameraNotInitialized }

        return try await withCheckedThrowingContinuation { [weak self] continuation in
            guard let self else {
                continuation.resume(throwing: CameraError.cameraNotInitialized)
                return
            }
            self.captureCompletion = { result in continuation.resume(with: result) }
            let settings = AVCapturePhotoSettings()
            settings.flashMode = .off
            photoOutput.capturePhoto(with: settings, delegate: self)
        }
    }

    public func stopCamera() {
        captureSession?.stopRunning()
        captureSession = nil
        photoOutput = nil
        isCameraReady = false
    }

    public var session: AVCaptureSession? { captureSession }

    private func saveImageToTemporaryFile(_ image: UIImage) -> String? {
        guard let data = image.jpegData(compressionQuality: 0.8) else { return nil }
        let fileURL = FileManager.default.temporaryDirectory.appendingPathComponent("capture_\(UUID().uuidString).jpg")
        do {
            try data.write(to: fileURL)
            return fileURL.path
        } catch {
            return nil
        }
    }
}

extension CameraService: AVCapturePhotoCaptureDelegate {
    nonisolated public func photoOutput(_ output: AVCapturePhotoOutput, didFinishProcessingPhoto photo: AVCapturePhoto, error: Error?) {
        let errorMessage = error?.localizedDescription
        let photoData = photo.fileDataRepresentation()
        Task { @MainActor in
            if let errorMessage {
                captureCompletion?(.failure(CameraError.captureFailed(errorMessage)))
                captureCompletion = nil
                return
            }
            guard let photoData, let image = UIImage(data: photoData) else {
                captureCompletion?(.failure(CameraError.processingFailed))
                captureCompletion = nil
                return
            }
            capturedImage = image
            if let path = saveImageToTemporaryFile(image) {
                capturedImagePath = path
                captureCompletion?(.success(path))
            } else {
                captureCompletion?(.failure(CameraError.saveFailed))
            }
            captureCompletion = nil
        }
    }
}

public enum CameraError: LocalizedError {
    case notAuthorized
    case noCameraAvailable
    case setupFailed
    case cameraNotInitialized
    case captureFailed(String)
    case processingFailed
    case saveFailed

    public var errorDescription: String? {
        switch self {
        case .notAuthorized: return "Camera access not authorized"
        case .noCameraAvailable: return "No camera available"
        case .setupFailed: return "Failed to setup camera"
        case .cameraNotInitialized: return "Camera not initialized"
        case .captureFailed(let msg): return "Failed to capture photo: \(msg)"
        case .processingFailed: return "Failed to process photo data"
        case .saveFailed: return "Failed to save photo"
        }
    }
}
