// swift-tools-version: 6.1

import PackageDescription

let package = Package(
    name: "GutHealthPackage",
    platforms: [.iOS(.v18)],
    products: [
        .library(
            name: "GutHealthFeature",
            targets: ["GutHealthFeature"]
        ),
    ],
    dependencies: [
        .package(url: "https://github.com/firebase/firebase-ios-sdk.git", from: "11.0.0"),
    ],
    targets: [
        .target(
            name: "GutHealthFeature",
            dependencies: [
                .product(name: "FirebaseAuth", package: "firebase-ios-sdk"),
                .product(name: "FirebaseFirestore", package: "firebase-ios-sdk"),
                .product(name: "FirebaseStorage", package: "firebase-ios-sdk"),
                .product(name: "FirebaseFunctions", package: "firebase-ios-sdk"),
                .product(name: "FirebaseMessaging", package: "firebase-ios-sdk"),
                .product(name: "FirebaseAnalytics", package: "firebase-ios-sdk"),
            ]
        ),
        .testTarget(
            name: "GutHealthFeatureTests",
            dependencies: ["GutHealthFeature"]
        ),
    ]
)
