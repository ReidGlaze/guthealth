import Foundation
@preconcurrency import FirebaseStorage
import UIKit

public actor StorageService {
    public static let shared = StorageService()
    private nonisolated(unsafe) let storage = Storage.storage()

    private init() {}

    public func uploadMealPhoto(uid: String, mealId: String, image: UIImage) async throws -> String {
        guard let data = image.jpegData(compressionQuality: 0.8) else {
            throw StorageError.imageConversionFailed
        }
        nonisolated(unsafe) let ref = storage.reference().child("users/\(uid)/meals/\(mealId).jpg")
        let metadata = StorageMetadata()
        metadata.contentType = "image/jpeg"
        _ = try await ref.putDataAsync(data, metadata: metadata)
        let url = try await ref.downloadURL()
        return url.absoluteString
    }

    public func uploadPoopPhoto(uid: String, logId: String, image: UIImage) async throws -> String {
        guard let data = image.jpegData(compressionQuality: 0.8) else {
            throw StorageError.imageConversionFailed
        }
        nonisolated(unsafe) let ref = storage.reference().child("users/\(uid)/poopLogs/\(logId).jpg")
        let metadata = StorageMetadata()
        metadata.contentType = "image/jpeg"
        _ = try await ref.putDataAsync(data, metadata: metadata)
        let url = try await ref.downloadURL()
        return url.absoluteString
    }

    public func deletePhoto(path: String) async throws {
        nonisolated(unsafe) let ref = storage.reference().child(path)
        try await ref.delete()
    }

    public func downloadImage(from urlString: String) async throws -> UIImage {
        guard let url = URL(string: urlString) else {
            throw StorageError.invalidURL
        }
        let (data, _) = try await URLSession.shared.data(from: url)
        guard let image = UIImage(data: data) else {
            throw StorageError.imageConversionFailed
        }
        return image
    }
}

public enum StorageError: LocalizedError {
    case imageConversionFailed
    case invalidURL

    public var errorDescription: String? {
        switch self {
        case .imageConversionFailed: return "Failed to convert image to JPEG data"
        case .invalidURL: return "Invalid storage URL"
        }
    }
}
