import XCTest

final class GutHealthUITests: XCTestCase {
    var app: XCUIApplication!

    override func setUpWithError() throws {
        continueAfterFailure = false
        app = XCUIApplication()
        app.launchArguments = ["--uitesting"]
    }

    // MARK: - Tab Navigation

    @MainActor
    func testMainTabsExist() throws {
        app.launch()

        let dashboardTab = app.buttons["Dashboard"]
        guard dashboardTab.waitForExistence(timeout: 10) else { return }

        let logTab = app.buttons["Log"]
        let fodmapTab = app.buttons["FODMAP"]
        let insightsTab = app.buttons["Insights"]

        XCTAssertTrue(dashboardTab.exists, "Dashboard tab should exist")
        XCTAssertTrue(logTab.exists, "Log tab should exist")
        XCTAssertTrue(fodmapTab.exists, "FODMAP tab should exist")
        XCTAssertTrue(insightsTab.exists, "Insights tab should exist")
    }

    // MARK: - Dashboard

    @MainActor
    func testDashboardShowsGutScore() throws {
        app.launch()

        let dashboardTab = app.buttons["Dashboard"]
        guard dashboardTab.waitForExistence(timeout: 10) else { return }

        let gutScoreLabel = app.staticTexts["Gut Score"]
        XCTAssertTrue(gutScoreLabel.waitForExistence(timeout: 5), "Dashboard should show Gut Score label")
    }

    // MARK: - Log Tab

    @MainActor
    func testLogTabShowsLogButtons() throws {
        app.launch()

        let logTab = app.buttons["Log"]
        guard logTab.waitForExistence(timeout: 10) else { return }
        logTab.tap()

        let mealButton = app.staticTexts["Meal"]
        XCTAssertTrue(mealButton.waitForExistence(timeout: 5), "Log tab should show Meal button")

        let symptomButton = app.staticTexts["Symptom"]
        XCTAssertTrue(symptomButton.exists, "Log tab should show Symptom button")

        let poopButton = app.staticTexts["Poop"]
        XCTAssertTrue(poopButton.exists, "Log tab should show Poop button")
    }

    // MARK: - FODMAP Guide

    @MainActor
    func testFODMAPGuideTabLoads() throws {
        app.launch()

        let fodmapTab = app.buttons["FODMAP"]
        guard fodmapTab.waitForExistence(timeout: 10) else { return }
        fodmapTab.tap()

        let navTitle = app.navigationBars["FODMAP Guide"]
        XCTAssertTrue(navTitle.waitForExistence(timeout: 5), "FODMAP Guide navigation title should appear")
    }

    // MARK: - Insights

    @MainActor
    func testInsightsTabShowsAnalyzeButton() throws {
        app.launch()

        let insightsTab = app.buttons["Insights"]
        guard insightsTab.waitForExistence(timeout: 10) else { return }
        insightsTab.tap()

        let analyzeButton = app.buttons["Run Correlation Analysis"]
        XCTAssertTrue(analyzeButton.waitForExistence(timeout: 5), "Insights tab should show analyze button")
    }
}
