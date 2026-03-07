import { chromium } from 'playwright';

const APP_DATA = {
  description: `AI Gut Health & IBS Tracker helps you understand your digestive patterns by connecting what you eat to how you feel. Whether you're following a low-FODMAP diet or just trying to figure out your food triggers, this app makes tracking simple and insightful.

SNAP & ANALYZE MEALS
Take a photo of your food and our AI instantly identifies ingredients and their FODMAP levels. Get clear green/yellow/red ratings so you know what's safe and what might cause issues. Serving size warnings alert you when portions exceed safe FODMAP thresholds.

TRACK SYMPTOMS & BOWEL MOVEMENTS
Log bloating, gas, pain, nausea, and other symptoms with severity ratings. Track bowel movements using the Bristol Stool Chart with optional photo classification. Every entry is timestamped so patterns are easy to spot.

AI CORRELATION ANALYSIS
Our AI engine analyzes your meals, symptoms, and bowel movements together to find patterns. Get plain-English reports explaining which foods may be triggering your symptoms and why.

FODMAP ELIMINATION GUIDE
Follow a structured elimination and reintroduction protocol to systematically identify your triggers. Search our FODMAP database to check any food before you eat it.

DAILY DASHBOARD
See everything you logged today at a glance. Browse past days to review your history. Track your logging streak to stay consistent.

KEY FEATURES
\u2022 AI food photo analysis with FODMAP ratings
\u2022 Symptom logger with severity tracking
\u2022 Bristol Stool Chart poop tracker
\u2022 AI-powered correlation reports
\u2022 FODMAP elimination & reintroduction guide
\u2022 Searchable FODMAP food database
\u2022 Daily summaries and logging streaks
\u2022 Push notification reminders

This app is an educational wellness tool and does not provide medical advice, diagnosis, or treatment. Always consult a healthcare professional for medical concerns.`,

  promotionalText: `Track meals, symptoms & bowel movements. AI-powered FODMAP analysis helps you discover your personal food triggers and take control of your gut health.`,

  whatsNew: `Initial release of AI Gut Health & IBS Tracker! Snap food photos for instant FODMAP analysis, log symptoms and bowel movements, and get AI-powered correlation reports to discover your triggers.`,

  copyright: `2026 Twin Tip Solutions LLC`,

  supportUrl: `https://guthealth.twintipsolutions.com/support`,

  privacyUrl: `https://guthealth.twintipsolutions.com/privacy`,

  reviewNotes: `This app uses anonymous authentication — no sign-up or login is required. On first launch, the app presents a 6-screen onboarding flow, then goes to the Dashboard. Use the Log tab to take or select a food photo for AI analysis, log symptoms, or log bowel movements. The Insights tab runs AI correlation analysis on logged data. All AI features require an internet connection. No in-app purchases.`,
};

(async () => {
  const browser = await chromium.launch({ headless: false, slowMo: 300 });
  const context = await browser.newContext({ viewport: { width: 1400, height: 900 } });
  const page = await context.newPage();

  await page.goto('https://appstoreconnect.apple.com');

  console.log('\n========================================');
  console.log('1. Log in to App Store Connect');
  console.log('2. Navigate to your app: AI Gut Health & IBS Tracker');
  console.log('3. Go to the app version page (e.g., 1.0 Prepare for Submission)');
  console.log('4. Come back here and press ENTER to continue');
  console.log('========================================\n');

  // Wait for user to log in and navigate
  await new Promise(resolve => {
    process.stdin.once('data', resolve);
  });

  console.log('Starting to fill fields...\n');

  // Helper: clear and fill a textarea/input by label or nearby text
  async function fillField(labelText, value) {
    try {
      // Try by label
      const byLabel = page.getByLabel(labelText, { exact: false });
      if (await byLabel.count() > 0) {
        await byLabel.first().click();
        await byLabel.first().fill(value);
        console.log(`Filled: ${labelText}`);
        return true;
      }
    } catch (e) {}

    try {
      // Try by placeholder
      const byPlaceholder = page.getByPlaceholder(labelText, { exact: false });
      if (await byPlaceholder.count() > 0) {
        await byPlaceholder.first().click();
        await byPlaceholder.first().fill(value);
        console.log(`Filled: ${labelText}`);
        return true;
      }
    } catch (e) {}

    console.log(`Could not find field: ${labelText} - you may need to fill this manually`);
    return false;
  }

  // Try to fill all the text fields
  // App Store Connect uses various label patterns - try multiple approaches

  // Description
  await fillField('Description', APP_DATA.description);

  // Promotional Text
  await fillField('Promotional Text', APP_DATA.promotionalText);

  // What's New
  await fillField("What's New", APP_DATA.whatsNew);

  // Copyright
  await fillField('Copyright', APP_DATA.copyright);

  // Support URL
  await fillField('Support URL', APP_DATA.supportUrl);

  // Marketing URL (optional)
  await fillField('Marketing URL', APP_DATA.supportUrl.replace('/support', ''));

  // Privacy Policy URL
  await fillField('Privacy Policy URL', APP_DATA.privacyUrl);

  // Review Notes
  await fillField('Notes', APP_DATA.reviewNotes);

  console.log('\n========================================');
  console.log('Done filling fields!');
  console.log('Please review everything and fill in any fields I missed.');
  console.log('Press ENTER to close the browser, or Ctrl+C to keep it open.');
  console.log('========================================\n');

  await new Promise(resolve => {
    process.stdin.once('data', resolve);
  });

  await browser.close();
})();
