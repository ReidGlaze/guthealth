import type { Metadata } from "next";
import { SubpageHeader } from "../components";

export const metadata: Metadata = {
  title: "Support — AI Gut Health & IBS Tracker",
};

const faqs = [
  {
    question: "Is this medical advice?",
    answer:
      "No. AI Gut Health & IBS Tracker is an educational wellness tool. All AI-generated content, including food analyses, correlations, and weekly reports, is for informational purposes only. It is not intended to diagnose, treat, cure, or prevent any disease. Always consult a qualified healthcare professional for medical advice.",
  },
  {
    question: "How does the AI food scanner work?",
    answer:
      "When you take a photo of your meal, the image is sent to Google's Gemini AI, which identifies the foods in the photo and determines their FODMAP levels (low, moderate, or high). The AI also identifies specific FODMAP categories like fructans, lactose, fructose, GOS, mannitol, and sorbitol for each food. The photo is processed temporarily and is not stored permanently on our servers.",
  },
  {
    question: "How does the correlation engine find my trigger foods?",
    answer:
      "The correlation engine uses AI to analyze your meal logs and symptom logs over the past 30 days. It looks for patterns where specific foods are consistently followed by symptoms within a typical digestion window. When it finds a reliable pattern, it reports the trigger food, the associated symptom, a confidence score, and a personalized recommendation.",
  },
  {
    question: "Can I delete my data?",
    answer:
      "Yes. You can delete all your data at any time through the App's settings screen. This permanently removes all your meal logs, symptom logs, poop logs, correlations, daily summaries, and weekly insights from our servers.",
  },
  {
    question: "Is my data private?",
    answer:
      "Yes. The App uses anonymous authentication, meaning we never collect your name, email, phone number, or any other personally identifiable information. Your data is stored under a randomly generated anonymous ID in Google Firebase. We do not sell or share your data with third parties for marketing purposes.",
  },
  {
    question: "What happens if I lose my phone or uninstall the app?",
    answer:
      "Because the App uses anonymous authentication tied to your device, your account cannot be recovered if you uninstall the App, factory reset your device, or switch to a new device. We recommend using the App's data features while you have access.",
  },
  {
    question: "What is the FODMAP diet?",
    answer:
      "FODMAP stands for Fermentable Oligosaccharides, Disaccharides, Monosaccharides, and Polyols. These are short-chain carbohydrates that some people have difficulty digesting, which can cause IBS symptoms. The Low FODMAP diet involves an elimination phase (removing high FODMAP foods), a reintroduction phase (testing foods one at a time), and a maintenance phase (personalizing your long-term diet). The App helps you track your progress through these phases.",
  },
  {
    question: "Is the app free?",
    answer:
      "Yes. AI Gut Health & IBS Tracker is completely free to use with no paywall, subscriptions, or in-app purchases.",
  },
];

export default function Support() {
  return (
    <>
    <SubpageHeader />
    <div className="mx-auto max-w-3xl px-6 py-12">
      <h1 className="text-3xl font-bold text-gray-900">Support</h1>
      <p className="mt-4 text-gray-600">
        Need help with AI Gut Health &amp; IBS Tracker? Check the frequently
        asked questions below or contact us directly.
      </p>

      <section className="mt-10 rounded-xl border border-gray-200 bg-gray-50 p-6">
        <h2 className="text-lg font-semibold text-gray-900">Contact Us</h2>
        <p className="mt-2 text-gray-600">
          For questions, feedback, or issues, email us at:
        </p>
        <p className="mt-2">
          <a
            href="mailto:reid@twintipsolutions.com"
            className="font-medium text-teal-600 hover:text-teal-700"
          >
            reid@twintipsolutions.com
          </a>
        </p>
        <p className="mt-3 text-sm text-gray-500">
          Twin Tip Solutions LLC — We typically respond within 1-2 business days.
        </p>
      </section>

      <section className="mt-12">
        <h2 className="text-2xl font-bold text-gray-900">
          Frequently Asked Questions
        </h2>
        <div className="mt-6 space-y-6">
          {faqs.map((faq) => (
            <div
              key={faq.question}
              className="rounded-lg border border-gray-200 p-5"
            >
              <h3 className="font-semibold text-gray-900">{faq.question}</h3>
              <p className="mt-2 text-sm leading-relaxed text-gray-600">
                {faq.answer}
              </p>
            </div>
          ))}
        </div>
      </section>

      <section className="mt-12 text-center text-xs text-gray-400">
        <p>
          AI Gut Health &amp; IBS Tracker is an educational wellness tool. It is
          not intended to diagnose, treat, cure, or prevent any disease. Always
          consult a qualified healthcare professional for medical advice.
        </p>
      </section>
    </div>
    </>
  );
}
