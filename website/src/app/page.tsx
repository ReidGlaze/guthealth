import { Header } from "./components";

const features = [
  {
    title: "AI Food Scanner",
    description:
      "Snap a photo of your meal and our AI instantly identifies every food and its FODMAP level — no manual searching required.",
    icon: (
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" className="h-7 w-7">
        <path strokeLinecap="round" strokeLinejoin="round" d="M6.827 6.175A2.31 2.31 0 0 1 5.186 7.23c-.38.054-.757.112-1.134.175C2.999 7.58 2.25 8.507 2.25 9.574V18a2.25 2.25 0 0 0 2.25 2.25h15A2.25 2.25 0 0 0 21.75 18V9.574c0-1.067-.75-1.994-1.802-2.169a47.865 47.865 0 0 0-1.134-.175 2.31 2.31 0 0 1-1.64-1.055l-.822-1.316a2.192 2.192 0 0 0-1.736-1.039 48.774 48.774 0 0 0-5.232 0 2.192 2.192 0 0 0-1.736 1.039l-.821 1.316Z" />
        <path strokeLinecap="round" strokeLinejoin="round" d="M16.5 12.75a4.5 4.5 0 1 1-9 0 4.5 4.5 0 0 1 9 0ZM18.75 10.5h.008v.008h-.008V10.5Z" />
      </svg>
    ),
  },
  {
    title: "Symptom Tracker",
    description:
      "Log bloating, gas, pain, heartburn, nausea, and more with severity ratings. Build a clear picture of your daily symptoms.",
    icon: (
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" className="h-7 w-7">
        <path strokeLinecap="round" strokeLinejoin="round" d="M11.35 3.836c-.065.21-.1.433-.1.664 0 .414.336.75.75.75h4.5a.75.75 0 0 0 .75-.75 2.25 2.25 0 0 0-.1-.664m-5.8 0A2.251 2.251 0 0 1 13.5 2.25H15a2.25 2.25 0 0 1 2.15 1.586m-5.8 0c-.376.023-.75.05-1.124.08C9.095 4.01 8.25 4.973 8.25 6.108V8.25m8.9-4.414c.376.023.75.05 1.124.08 1.131.094 1.976 1.057 1.976 2.192V16.5A2.25 2.25 0 0 1 18 18.75h-2.25m-7.5-10.5H4.875c-.621 0-1.125.504-1.125 1.125v11.25c0 .621.504 1.125 1.125 1.125h9.75c.621 0 1.125-.504 1.125-1.125V18.75m-7.5-10.5h6.375c.621 0 1.125.504 1.125 1.125v9.375m-8.25-3 1.5 1.5 3-3.75" />
      </svg>
    ),
  },
  {
    title: "Bristol Stool Logger",
    description:
      "Track bowel movements with the Bristol Stool Chart. Optionally snap a photo for AI-powered classification.",
    icon: (
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" className="h-7 w-7">
        <path strokeLinecap="round" strokeLinejoin="round" d="M3.75 3v11.25A2.25 2.25 0 0 0 6 16.5h2.25M3.75 3h-1.5m1.5 0h16.5m0 0h1.5m-1.5 0v11.25A2.25 2.25 0 0 1 18 16.5h-2.25m-7.5 0h7.5m-7.5 0-1 3m8.5-3 1 3m0 0 .5 1.5m-.5-1.5h-9.5m0 0-.5 1.5m.75-9 3-3 2.148 2.148A12.061 12.061 0 0 1 16.5 7.605" />
      </svg>
    ),
  },
  {
    title: "Correlation Engine",
    description:
      "Our AI analyzes 30 days of your food and symptom data to uncover hidden trigger foods and timing patterns.",
    icon: (
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" className="h-7 w-7">
        <path strokeLinecap="round" strokeLinejoin="round" d="m21 21-5.197-5.197m0 0A7.5 7.5 0 1 0 5.196 5.196a7.5 7.5 0 0 0 10.607 10.607Z" />
      </svg>
    ),
  },
  {
    title: "FODMAP Guide",
    description:
      "Navigate the Low FODMAP elimination and reintroduction phases step by step, with a searchable food database.",
    icon: (
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" className="h-7 w-7">
        <path strokeLinecap="round" strokeLinejoin="round" d="M12 6.042A8.967 8.967 0 0 0 6 3.75c-1.052 0-2.062.18-3 .512v14.25A8.987 8.987 0 0 1 6 18c2.305 0 4.408.867 6 2.292m0-14.25a8.966 8.966 0 0 1 6-2.292c1.052 0 2.062.18 3 .512v14.25A8.987 8.987 0 0 0 18 18a8.967 8.967 0 0 0-6 2.292m0-14.25v14.25" />
      </svg>
    ),
  },
  {
    title: "Weekly AI Insights",
    description:
      "Every week, receive a personalized AI-generated gut health report with trends, improvements, and actionable tips.",
    icon: (
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" className="h-7 w-7">
        <path strokeLinecap="round" strokeLinejoin="round" d="M9.813 15.904 9 18.75l-.813-2.846a4.5 4.5 0 0 0-3.09-3.09L2.25 12l2.846-.813a4.5 4.5 0 0 0 3.09-3.09L9 5.25l.813 2.846a4.5 4.5 0 0 0 3.09 3.09L15.75 12l-2.846.813a4.5 4.5 0 0 0-3.09 3.09ZM18.259 8.715 18 9.75l-.259-1.035a3.375 3.375 0 0 0-2.455-2.456L14.25 6l1.036-.259a3.375 3.375 0 0 0 2.455-2.456L18 2.25l.259 1.035a3.375 3.375 0 0 0 2.455 2.456L21.75 6l-1.036.259a3.375 3.375 0 0 0-2.455 2.456ZM16.894 20.567 16.5 21.75l-.394-1.183a2.25 2.25 0 0 0-1.423-1.423L13.5 18.75l1.183-.394a2.25 2.25 0 0 0 1.423-1.423l.394-1.183.394 1.183a2.25 2.25 0 0 0 1.423 1.423l1.183.394-1.183.394a2.25 2.25 0 0 0-1.423 1.423Z" />
      </svg>
    ),
  },
];

const steps = [
  {
    number: "1",
    title: "Log Your Meals",
    description:
      "Snap a photo or manually log what you eat. AI identifies foods and FODMAP levels automatically.",
  },
  {
    number: "2",
    title: "Track Your Symptoms",
    description:
      "Record bloating, pain, gas, and other symptoms as they happen. Rate severity so patterns emerge.",
  },
  {
    number: "3",
    title: "AI Finds Your Triggers",
    description:
      "Our correlation engine analyzes your data and reveals which foods cause your symptoms — backed by your own data.",
  },
];

function AppStoreBadge() {
  return (
    <a
      href="#"
      className="inline-flex items-center gap-2.5 rounded-xl bg-black px-5 py-3 text-white transition hover:bg-gray-800"
    >
      <svg viewBox="0 0 24 24" fill="currentColor" className="h-7 w-7">
        <path d="M18.71 19.5c-.83 1.24-1.71 2.45-3.05 2.47-1.34.03-1.77-.79-3.29-.79-1.53 0-2 .77-3.27.82-1.31.05-2.3-1.32-3.14-2.53C4.25 17 2.94 12.45 4.7 9.39c.87-1.52 2.43-2.48 4.12-2.51 1.28-.02 2.5.87 3.29.87.78 0 2.26-1.07 3.8-.91.65.03 2.47.26 3.64 1.98-.09.06-2.17 1.28-2.15 3.81.03 3.02 2.65 4.03 2.68 4.04-.03.07-.42 1.44-1.38 2.83M13 3.5c.73-.83 1.94-1.46 2.94-1.5.13 1.17-.34 2.35-1.04 3.19-.69.85-1.83 1.51-2.95 1.42-.15-1.15.41-2.35 1.05-3.11Z" />
      </svg>
      <div className="text-left">
        <div className="text-[10px] leading-none">Download on the</div>
        <div className="text-lg font-semibold leading-tight">App Store</div>
      </div>
    </a>
  );
}

function GooglePlayBadge() {
  return (
    <a
      href="#"
      className="inline-flex items-center gap-2.5 rounded-xl bg-black px-5 py-3 text-white transition hover:bg-gray-800"
    >
      <svg viewBox="0 0 24 24" fill="currentColor" className="h-7 w-7">
        <path d="M3.609 1.814 13.792 12 3.61 22.186a.996.996 0 0 1-.61-.92V2.734a1 1 0 0 1 .609-.92Zm10.89 10.893 2.302 2.302-10.937 6.333 8.635-8.635ZM17.8 11.344l2.944 1.7a1 1 0 0 1 0 1.728L17.8 16.47l-2.57-2.57 2.57-2.556ZM5.864 2.658 16.8 8.99l-2.3 2.3-8.636-8.632Z" />
      </svg>
      <div className="text-left">
        <div className="text-[10px] leading-none">GET IT ON</div>
        <div className="text-lg font-semibold leading-tight">Google Play</div>
      </div>
    </a>
  );
}

function PhoneMockup() {
  return (
    <div className="relative mx-auto w-[280px]">
      {/* Phone frame */}
      <div className="rounded-[2.5rem] border-[8px] border-gray-900 bg-gray-900 p-1 shadow-2xl">
        {/* Notch */}
        <div className="absolute left-1/2 top-0 z-10 h-6 w-28 -translate-x-1/2 rounded-b-2xl bg-gray-900" />
        {/* Screen */}
        <div className="relative overflow-hidden rounded-[2rem] bg-white">
          {/* Status bar */}
          <div className="flex items-center justify-between bg-teal-600 px-5 pb-1 pt-7 text-[10px] font-medium text-white">
            <span>9:41</span>
            <div className="flex gap-1">
              <div className="h-2.5 w-2.5 rounded-full border border-white" />
              <div className="h-2.5 w-4 rounded-sm border border-white" />
            </div>
          </div>
          {/* App content mockup */}
          <div className="bg-gradient-to-b from-teal-600 to-teal-500 px-4 pb-5 pt-3 text-white">
            <p className="text-[11px] font-medium opacity-80">Good morning</p>
            <p className="mt-0.5 text-base font-bold">Today&apos;s Dashboard</p>
            {/* Gut score ring */}
            <div className="mx-auto mt-3 flex h-24 w-24 items-center justify-center rounded-full border-[6px] border-white/30">
              <div className="flex h-[72px] w-[72px] items-center justify-center rounded-full border-[5px] border-teal-200">
                <div className="text-center">
                  <div className="text-2xl font-bold leading-none">82</div>
                  <div className="text-[8px] uppercase tracking-wider opacity-80">Gut Score</div>
                </div>
              </div>
            </div>
          </div>
          {/* Cards */}
          <div className="-mt-2 space-y-2 rounded-t-xl bg-gray-50 px-3 pb-4 pt-4">
            <div className="rounded-lg bg-white p-3 shadow-sm">
              <div className="flex items-center justify-between">
                <div className="flex items-center gap-2">
                  <div className="flex h-7 w-7 items-center justify-center rounded-full bg-teal-100 text-xs">
                    🥗
                  </div>
                  <div>
                    <p className="text-[11px] font-semibold text-gray-900">Lunch</p>
                    <p className="text-[9px] text-gray-500">Grilled chicken salad</p>
                  </div>
                </div>
                <span className="rounded-full bg-green-100 px-2 py-0.5 text-[9px] font-medium text-green-700">Low FODMAP</span>
              </div>
            </div>
            <div className="rounded-lg bg-white p-3 shadow-sm">
              <div className="flex items-center justify-between">
                <div className="flex items-center gap-2">
                  <div className="flex h-7 w-7 items-center justify-center rounded-full bg-orange-100 text-xs">
                    😣
                  </div>
                  <div>
                    <p className="text-[11px] font-semibold text-gray-900">Mild Bloating</p>
                    <p className="text-[9px] text-gray-500">Severity: 3/10</p>
                  </div>
                </div>
                <span className="text-[9px] text-gray-400">2:30 PM</span>
              </div>
            </div>
            <div className="rounded-lg bg-white p-3 shadow-sm">
              <div className="flex items-center justify-between">
                <div className="flex items-center gap-2">
                  <div className="flex h-7 w-7 items-center justify-center rounded-full bg-amber-100 text-xs">
                    🔍
                  </div>
                  <div>
                    <p className="text-[11px] font-semibold text-gray-900">Trigger Found</p>
                    <p className="text-[9px] text-gray-500">Garlic linked to bloating</p>
                  </div>
                </div>
                <span className="rounded-full bg-amber-100 px-2 py-0.5 text-[9px] font-medium text-amber-700">87%</span>
              </div>
            </div>
          </div>
          {/* Tab bar */}
          <div className="flex items-center justify-around border-t border-gray-200 bg-white px-2 py-2">
            <div className="flex flex-col items-center">
              <div className="h-3.5 w-3.5 rounded-sm bg-teal-500" />
              <span className="mt-0.5 text-[8px] font-medium text-teal-600">Home</span>
            </div>
            <div className="flex flex-col items-center">
              <div className="h-3.5 w-3.5 rounded-full border-2 border-gray-300" />
              <span className="mt-0.5 text-[8px] text-gray-400">Log</span>
            </div>
            <div className="flex flex-col items-center">
              <div className="h-3.5 w-3.5 rounded-sm border-2 border-gray-300" />
              <span className="mt-0.5 text-[8px] text-gray-400">FODMAP</span>
            </div>
            <div className="flex flex-col items-center">
              <div className="h-3.5 w-3.5 rounded-full border-2 border-gray-300" />
              <span className="mt-0.5 text-[8px] text-gray-400">Insights</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

export default function Home() {
  return (
    <>
      <Header />
      {/* ===== HERO ===== */}
      <section className="relative overflow-hidden bg-gradient-to-br from-teal-600 via-teal-500 to-teal-700 px-6 pb-24 pt-20 text-center text-white">
        {/* Decorative blobs */}
        <div className="pointer-events-none absolute -left-32 -top-32 h-96 w-96 rounded-full bg-white/5" />
        <div className="pointer-events-none absolute -bottom-48 -right-24 h-[500px] w-[500px] rounded-full bg-white/5" />

        <div className="relative mx-auto max-w-5xl">
          <p className="text-sm font-semibold uppercase tracking-widest text-teal-200">
            FODMAP Food Diary & Poop Log
          </p>
          <h1 className="mt-4 text-4xl font-extrabold tracking-tight sm:text-5xl lg:text-6xl">
            Discover Your Trigger Foods
            <br className="hidden sm:block" /> with AI
          </h1>
          <p className="mx-auto mt-5 max-w-2xl text-lg text-teal-100">
            Track meals, symptoms, and bowel movements. Our AI correlation
            engine analyzes your data to reveal which foods cause your IBS
            symptoms — so you can finally feel better.
          </p>

          <div className="mt-10 flex flex-col items-center justify-center gap-4 sm:flex-row">
            <AppStoreBadge />
            <GooglePlayBadge />
          </div>

          {/* App mockup */}
          <div className="mt-16">
            <PhoneMockup />
          </div>
        </div>
      </section>

      {/* ===== SOCIAL PROOF BAR ===== */}
      <section className="border-b border-gray-100 bg-white px-6 py-8">
        <div className="mx-auto flex max-w-4xl flex-wrap items-center justify-center gap-x-12 gap-y-4 text-center text-sm text-gray-500">
          <span>
            <strong className="text-gray-900">100%</strong> Free
          </span>
          <span className="hidden h-4 w-px bg-gray-300 sm:block" />
          <span>
            <strong className="text-gray-900">No Account</strong> Required
          </span>
          <span className="hidden h-4 w-px bg-gray-300 sm:block" />
          <span>
            <strong className="text-gray-900">Anonymous</strong> & Private
          </span>
          <span className="hidden h-4 w-px bg-gray-300 sm:block" />
          <span>
            <strong className="text-gray-900">AI-Powered</strong> Insights
          </span>
        </div>
      </section>

      {/* ===== FEATURES ===== */}
      <section className="px-6 py-20">
        <div className="mx-auto max-w-5xl">
          <p className="text-center text-sm font-semibold uppercase tracking-widest text-teal-600">
            Features
          </p>
          <h2 className="mt-2 text-center text-3xl font-bold text-gray-900 sm:text-4xl">
            Everything you need to understand your gut
          </h2>
          <p className="mx-auto mt-4 max-w-2xl text-center text-gray-500">
            Six powerful tools that work together to give you a complete picture
            of your digestive health.
          </p>

          <div className="mt-14 grid gap-8 sm:grid-cols-2 lg:grid-cols-3">
            {features.map((feature) => (
              <div
                key={feature.title}
                className="group rounded-2xl border border-gray-200 p-6 transition hover:border-teal-200 hover:shadow-lg"
              >
                <div className="flex h-12 w-12 items-center justify-center rounded-xl bg-teal-50 text-teal-600 transition group-hover:bg-teal-100">
                  {feature.icon}
                </div>
                <h3 className="mt-4 text-lg font-semibold text-gray-900">
                  {feature.title}
                </h3>
                <p className="mt-2 text-sm leading-relaxed text-gray-500">
                  {feature.description}
                </p>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* ===== HOW IT WORKS ===== */}
      <section className="bg-gray-50 px-6 py-20">
        <div className="mx-auto max-w-4xl">
          <p className="text-center text-sm font-semibold uppercase tracking-widest text-teal-600">
            How It Works
          </p>
          <h2 className="mt-2 text-center text-3xl font-bold text-gray-900 sm:text-4xl">
            Three steps to feeling better
          </h2>

          <div className="mt-14 grid gap-10 sm:grid-cols-3">
            {steps.map((step) => (
              <div key={step.number} className="text-center">
                <div className="mx-auto flex h-14 w-14 items-center justify-center rounded-full bg-teal-600 text-xl font-bold text-white shadow-lg shadow-teal-200">
                  {step.number}
                </div>
                <h3 className="mt-5 text-lg font-semibold text-gray-900">
                  {step.title}
                </h3>
                <p className="mt-2 text-sm leading-relaxed text-gray-500">
                  {step.description}
                </p>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* ===== TESTIMONIAL / VALUE PROP ===== */}
      <section className="px-6 py-20">
        <div className="mx-auto max-w-3xl text-center">
          <h2 className="text-3xl font-bold text-gray-900 sm:text-4xl">
            Stop guessing. Start knowing.
          </h2>
          <p className="mt-4 text-lg leading-relaxed text-gray-500">
            IBS affects 1 in 7 people worldwide, but most never identify their
            trigger foods. AI Gut Health & IBS Tracker combines daily logging
            with AI-powered analysis to help you discover patterns that are
            impossible to see on your own.
          </p>
          <div className="mx-auto mt-8 grid max-w-xl gap-6 sm:grid-cols-3">
            <div className="rounded-xl bg-teal-50 p-5">
              <div className="text-3xl font-bold text-teal-600">0-100</div>
              <div className="mt-1 text-sm text-gray-600">Daily Gut Score</div>
            </div>
            <div className="rounded-xl bg-teal-50 p-5">
              <div className="text-3xl font-bold text-teal-600">30 days</div>
              <div className="mt-1 text-sm text-gray-600">
                Pattern Analysis
              </div>
            </div>
            <div className="rounded-xl bg-teal-50 p-5">
              <div className="text-3xl font-bold text-teal-600">Weekly</div>
              <div className="mt-1 text-sm text-gray-600">AI Reports</div>
            </div>
          </div>
        </div>
      </section>

      {/* ===== CTA / DOWNLOAD ===== */}
      <section className="bg-gradient-to-br from-teal-600 to-teal-700 px-6 py-20 text-center text-white">
        <div className="mx-auto max-w-2xl">
          <h2 className="text-3xl font-bold sm:text-4xl">
            Take control of your gut health
          </h2>
          <p className="mt-4 text-lg text-teal-100">
            Free to download. No account needed. No ads. Just a better gut.
          </p>
          <div className="mt-10 flex flex-col items-center justify-center gap-4 sm:flex-row">
            <AppStoreBadge />
            <GooglePlayBadge />
          </div>
        </div>
      </section>

      {/* ===== DISCLAIMER ===== */}
      <section className="mx-auto max-w-3xl px-6 py-10 text-center text-xs leading-relaxed text-gray-400">
        <p>
          AI Gut Health & IBS Tracker is an educational wellness tool. It is not
          intended to diagnose, treat, cure, or prevent any disease. All
          AI-generated content is for informational purposes only. Always consult
          a qualified healthcare professional for medical advice.
        </p>
      </section>
    </>
  );
}
