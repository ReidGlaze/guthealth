import type { Metadata } from "next";
import { SubpageHeader } from "../components";

export const metadata: Metadata = {
  title: "Terms of Service — AI Gut Health & IBS Tracker",
};

export default function TermsOfService() {
  return (
    <>
    <SubpageHeader />
    <article className="prose prose-gray mx-auto max-w-3xl px-6 py-12">
      <h1>Terms of Service</h1>
      <p className="text-sm text-gray-500">Last updated: February 28, 2026</p>

      <p>
        These Terms of Service (&quot;Terms&quot;) govern your use of the AI Gut Health
        &amp; IBS Tracker mobile application (the &quot;App&quot;), operated by Twin Tip
        Solutions LLC (&quot;we,&quot; &quot;us,&quot; or &quot;our&quot;). By using the App, you agree to
        these Terms.
      </p>

      <h2>1. Educational Wellness Tool</h2>
      <p>
        The App is an <strong>educational wellness tool</strong> designed to help
        you track dietary intake, digestive symptoms, and bowel movements. The
        App provides general informational content related to gut health and the
        Low FODMAP diet.
      </p>
      <p>
        <strong>
          The App is NOT a medical device. It is NOT intended to diagnose, treat,
          cure, or prevent any disease or medical condition.
        </strong>
      </p>

      <h2>2. Not Medical Advice</h2>
      <p>
        All AI-generated content in the App, including food analyses, symptom
        correlations, gut health scores, daily summaries, and weekly insight
        reports, is provided for <strong>informational and educational purposes
        only</strong>. This content does not constitute medical advice,
        professional diagnosis, or treatment recommendations.
      </p>
      <p>
        You should always consult a qualified healthcare professional before
        making any decisions about your health, diet, or medical treatment. Never
        disregard professional medical advice or delay seeking it because of
        information provided by the App.
      </p>

      <h2>3. No Liability for Health Outcomes</h2>
      <p>
        We are not responsible or liable for any health outcomes, adverse
        effects, or consequences that may result from your use of the App or
        reliance on its content. You use the App and its information entirely at
        your own risk.
      </p>

      <h2>4. Age Requirements</h2>
      <p>
        You must be at least 13 years of age to use the App. If you are under
        18, you should use the App only with the involvement of a parent or
        guardian.
      </p>

      <h2>5. Anonymous Accounts</h2>
      <p>
        The App uses anonymous authentication. No email address, name, or
        password is associated with your account. Your account is tied to your
        device.
      </p>
      <p>
        <strong>
          If you uninstall the App, factory reset your device, or lose your
          device, your anonymous account and all associated data cannot be
          recovered.
        </strong>{" "}
        We are unable to provide account recovery services for anonymous
        accounts.
      </p>

      <h2>6. Acceptable Use</h2>
      <p>You agree not to:</p>
      <ul>
        <li>
          Use the App for any unlawful purpose or in violation of any applicable
          laws or regulations.
        </li>
        <li>
          Attempt to reverse engineer, decompile, or disassemble the App.
        </li>
        <li>
          Interfere with or disrupt the App&apos;s servers, networks, or
          infrastructure.
        </li>
        <li>
          Upload malicious content, including harmful images or data designed to
          exploit the AI analysis features.
        </li>
        <li>
          Use the App to collect or harvest data about other users.
        </li>
        <li>
          Misrepresent the App&apos;s AI-generated content as professional
          medical advice.
        </li>
      </ul>

      <h2>7. Intellectual Property</h2>
      <p>
        The App, its design, features, and content (excluding user-submitted
        data) are owned by Twin Tip Solutions LLC and are protected by
        applicable intellectual property laws. You may not copy, modify,
        distribute, or create derivative works based on the App without our
        prior written consent.
      </p>

      <h2>8. AI-Generated Content</h2>
      <p>
        The App uses artificial intelligence (Google Gemini) to analyze food
        photos, classify bowel movements, identify potential food-symptom
        correlations, and generate health insights. AI-generated content may
        contain errors or inaccuracies. We do not guarantee the accuracy,
        completeness, or reliability of any AI-generated content.
      </p>

      <h2>9. Third-Party Services</h2>
      <p>
        The App relies on third-party services including Google Firebase and
        Google Gemini API. We are not responsible for any interruptions,
        errors, or data issues caused by these third-party services.
      </p>

      <h2>10. Availability and Changes</h2>
      <p>
        We reserve the right to modify, suspend, or discontinue the App at any
        time without notice. We may also update these Terms from time to time.
        Continued use of the App after changes to these Terms constitutes
        acceptance of the updated Terms.
      </p>

      <h2>11. Termination</h2>
      <p>
        We reserve the right to terminate or suspend your access to the App at
        any time, without notice, for conduct that we believe violates these
        Terms or is harmful to other users, us, or third parties.
      </p>
      <p>
        You may stop using the App at any time. You can delete your data through
        the App&apos;s settings before uninstalling.
      </p>

      <h2>12. Disclaimer of Warranties</h2>
      <p>
        The App is provided &quot;as is&quot; and &quot;as available&quot; without warranties of
        any kind, either express or implied, including but not limited to
        implied warranties of merchantability, fitness for a particular purpose,
        and non-infringement.
      </p>

      <h2>13. Limitation of Liability</h2>
      <p>
        To the maximum extent permitted by applicable law, Twin Tip Solutions
        LLC shall not be liable for any indirect, incidental, special,
        consequential, or punitive damages, or any loss of profits or revenues,
        whether incurred directly or indirectly, or any loss of data, use,
        goodwill, or other intangible losses resulting from your use of the App.
      </p>

      <h2>14. Governing Law</h2>
      <p>
        These Terms shall be governed by and construed in accordance with the
        laws of the State of Utah, United States, without regard to its conflict
        of law provisions. Any disputes arising from these Terms or your use of
        the App shall be resolved in the courts of the State of Utah.
      </p>

      <h2>15. Contact Us</h2>
      <p>
        If you have questions about these Terms, please contact us at:
      </p>
      <p>
        <strong>Twin Tip Solutions LLC</strong>
        <br />
        Email:{" "}
        <a href="mailto:support@twintipsolutions.com">
          support@twintipsolutions.com
        </a>
      </p>
    </article>
    </>
  );
}
