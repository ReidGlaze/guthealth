import Link from "next/link";

export function Header() {
  return (
    <header className="absolute left-0 right-0 top-0 z-50">
      <div className="mx-auto flex max-w-6xl items-center justify-between px-6 py-5">
        <Link href="/" className="text-lg font-bold text-white">
          AI Gut Health
        </Link>
        <nav className="hidden items-center gap-6 sm:flex">
          <Link
            href="/support"
            className="text-sm text-white/80 transition hover:text-white"
          >
            Support
          </Link>
          <Link
            href="/privacy"
            className="text-sm text-white/80 transition hover:text-white"
          >
            Privacy
          </Link>
          <Link
            href="/terms"
            className="text-sm text-white/80 transition hover:text-white"
          >
            Terms
          </Link>
        </nav>
        <nav className="flex items-center gap-4 sm:hidden">
          <Link
            href="/support"
            className="text-xs text-white/80 transition hover:text-white"
          >
            Support
          </Link>
          <Link
            href="/privacy"
            className="text-xs text-white/80 transition hover:text-white"
          >
            Privacy
          </Link>
        </nav>
      </div>
    </header>
  );
}

export function SubpageHeader() {
  return (
    <header className="border-b border-gray-200 bg-white">
      <div className="mx-auto flex max-w-5xl items-center justify-between px-6 py-4">
        <Link href="/" className="text-lg font-bold text-teal-600">
          AI Gut Health
        </Link>
        <nav className="flex items-center gap-6">
          <Link
            href="/privacy"
            className="text-sm text-gray-600 hover:text-teal-600"
          >
            Privacy
          </Link>
          <Link
            href="/terms"
            className="text-sm text-gray-600 hover:text-teal-600"
          >
            Terms
          </Link>
          <Link
            href="/support"
            className="text-sm text-gray-600 hover:text-teal-600"
          >
            Support
          </Link>
        </nav>
      </div>
    </header>
  );
}
