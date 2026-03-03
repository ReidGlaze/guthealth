import type { Metadata } from "next";
import Link from "next/link";
import "./globals.css";

export const metadata: Metadata = {
  title: "AI Gut Health & IBS Tracker — FODMAP Food Diary & Poop Log",
  description:
    "Discover your trigger foods with AI. Track meals, symptoms, and bowel movements. Our correlation engine finds the foods causing your IBS symptoms.",
};

function Footer() {
  return (
    <footer className="border-t border-gray-200 bg-gray-50">
      <div className="mx-auto max-w-5xl px-6 py-10">
        <div className="flex flex-col items-center gap-6 sm:flex-row sm:justify-between">
          <div>
            <Link href="/" className="text-lg font-bold text-teal-600">
              AI Gut Health
            </Link>
            <p className="mt-1 text-sm text-gray-500">
              FODMAP Food Diary & Poop Log
            </p>
          </div>
          <nav className="flex flex-wrap justify-center gap-x-8 gap-y-2">
            <Link
              href="/privacy"
              className="text-sm text-gray-500 hover:text-teal-600"
            >
              Privacy Policy
            </Link>
            <Link
              href="/terms"
              className="text-sm text-gray-500 hover:text-teal-600"
            >
              Terms of Service
            </Link>
            <Link
              href="/support"
              className="text-sm text-gray-500 hover:text-teal-600"
            >
              Support
            </Link>
          </nav>
        </div>
        <div className="mt-8 border-t border-gray-200 pt-6 text-center text-xs text-gray-400">
          &copy; {new Date().getFullYear()} Twin Tip Solutions LLC. All rights
          reserved.
        </div>
      </div>
    </footer>
  );
}

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="en">
      <body className="flex min-h-screen flex-col bg-white text-gray-900 antialiased">
        {children}
        <Footer />
      </body>
    </html>
  );
}
