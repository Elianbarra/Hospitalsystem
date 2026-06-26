import { Suspense } from "react";
import { LoginForm } from "./components/LoginForm";
import { HospitalLogo } from "./components/HospitalLogo";

const NAV_LINKS = ["Especialidades", "Servicios", "Profesionales", "Sobre nosotros", "Contacto"];

const FEATURES = [
  {
    title: "Historia clínica completa",
    desc: "Consulte sus diagnósticos, tratamientos y antecedentes en cualquier momento.",
  },
  {
    title: "Resultados al instante",
    desc: "Reciba notificaciones cuando estén listos los resultados de sus pruebas.",
  },
  {
    title: "Citas y videoconsulta",
    desc: "Reserve, modifique o atienda su cita por videoconsulta sin salir de casa.",
  },
];

const STATS = [
  ["250.000+", "Pacientes activos"],
  ["180", "Especialistas"],
  ["12", "Centros asociados"],
];

const FOOTER_COLS = [
  ["ATENCIÓN AL PACIENTE", ["Citas y consultas", "Resultados", "Facturación"]],
  ["HOSPITAL", ["Sobre nosotros", "Especialidades", "Profesionales"]],
  ["SOPORTE", ["Centro de ayuda", "Aviso de privacidad", "Accesibilidad"]],
] as const;

function CheckIcon() {
  return (
    <div className="w-5 h-5 bg-blue-100 rounded-full flex items-center justify-center flex-shrink-0 mt-0.5">
      <svg className="w-3 h-3 text-blue-600" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={3}>
        <path strokeLinecap="round" strokeLinejoin="round" d="M5 13l4 4L19 7" />
      </svg>
    </div>
  );
}

export default function Home() {
  return (
    <div className="min-h-screen flex flex-col bg-white">
      {/* Top info bar */}
      <div className="bg-gray-900 text-white text-xs py-2 px-8 flex items-center justify-between">
        <div className="flex items-center gap-2">
          <span className="w-2 h-2 bg-green-400 rounded-full" />
          <span className="text-gray-300">Atendiendo con normalidad</span>
        </div>
        <div className="flex items-center gap-4 text-gray-400">
          <span className="flex items-center gap-1.5">
            <svg className="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 5a2 2 0 012-2h3.28a1 1 0 01.948.684l1.498 4.493a1 1 0 01-.502 1.21l-2.257 1.13a11.042 11.042 0 005.516 5.516l1.13-2.257a1 1 0 011.21-.502l4.493 1.498a1 1 0 01.684.949V19a2 2 0 01-2 2h-1C9.716 21 3 14.284 3 6V5z" />
            </svg>
            900 123 456
          </span>
          <span className="text-gray-600">|</span>
          <span>Urgencias 24h · <strong className="text-white font-bold">112</strong></span>
          <span className="text-gray-600">|</span>
          <span className="flex items-center gap-1.5">
            <button className="text-white font-semibold">ES</button>
            <span className="text-gray-600">|</span>
            <button className="hover:text-white transition-colors">EN</button>
          </span>
        </div>
      </div>

      {/* Navbar */}
      <nav className="bg-white border-b border-gray-100 px-8 py-4 flex items-center justify-between shadow-sm">
        <HospitalLogo />
        <div className="flex items-center gap-8">
          {NAV_LINKS.map((link) => (
            <a key={link} href="#" className="text-blue-700 hover:text-blue-900 text-sm font-medium transition-colors">
              {link}
            </a>
          ))}
        </div>
        <button className="bg-gray-900 text-white px-5 py-2.5 rounded-lg text-sm font-semibold hover:bg-gray-700 transition-colors flex items-center gap-2">
          Pedir cita
          <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17 8l4 4m0 0l-4 4m4-4H3" />
          </svg>
        </button>
      </nav>

      {/* Hero */}
      <main
        className="flex-1"
        style={{ backgroundImage: "radial-gradient(#e2e8f0 1px, transparent 1px)", backgroundSize: "24px 24px" }}
      >
        <div className="max-w-7xl mx-auto px-8 py-20 flex items-center gap-20">
          <div className="flex-1 min-w-0">
            <div className="inline-flex items-center gap-2 bg-blue-50 text-blue-700 text-xs font-bold px-3 py-1.5 rounded-full mb-7 tracking-wide">
              <span className="w-1.5 h-1.5 bg-blue-500 rounded-full" />
              PORTAL DEL PACIENTE
            </div>
            <h1 className="text-5xl font-black text-gray-900 leading-[1.1] mb-6">
              Su salud,{" "}
              <em className="not-italic italic text-blue-700">siempre<br />cerca.</em>
            </h1>
            <p className="text-gray-500 text-base leading-relaxed mb-10 max-w-md">
              Acceda de forma segura a su historia clínica, consulte resultados de pruebas,
              gestione sus citas y comuníquese con su equipo médico desde un único lugar.
            </p>
            <ul className="flex flex-col gap-5">
              {FEATURES.map(({ title, desc }) => (
                <li key={title} className="flex items-start gap-3">
                  <CheckIcon />
                  <div>
                    <p className="text-sm font-semibold text-gray-900">{title}</p>
                    <p className="text-sm text-gray-500">{desc}</p>
                  </div>
                </li>
              ))}
            </ul>
          </div>

          <div className="w-[420px] flex-shrink-0">
            <div className="bg-white rounded-2xl shadow-xl border border-gray-100 p-8">
              <h2 className="text-2xl font-bold text-gray-900 mb-1">Acceder al portal</h2>
              <p className="text-sm text-gray-500 mb-7">Introduzca sus credenciales</p>
              <Suspense>
                <LoginForm />
              </Suspense>
            </div>
          </div>
        </div>

        {/* Stats */}
        <div className="border-t border-gray-100 bg-white/80 backdrop-blur-sm">
          <div className="max-w-7xl mx-auto px-8 py-10">
            <p className="text-xs font-bold text-gray-400 tracking-[0.2em] mb-5">CONFIANZA DESDE 1947</p>
            <div className="flex gap-16">
              {STATS.map(([value, label]) => (
                <div key={label}>
                  <p className="text-3xl font-black text-gray-900">{value}</p>
                  <p className="text-sm text-gray-500 mt-0.5">{label}</p>
                </div>
              ))}
            </div>
          </div>
        </div>
      </main>

      {/* Footer */}
      <footer className="bg-white border-t border-gray-200">
        <div className="max-w-7xl mx-auto px-8 py-10 grid grid-cols-4 gap-10">
          <HospitalLogo size="sm" />
          {FOOTER_COLS.map(([title, links]) => (
            <div key={title}>
              <h4 className="text-xs font-bold text-gray-400 tracking-[0.15em] mb-4">{title}</h4>
              <ul className="flex flex-col gap-2.5">
                {links.map((link) => (
                  <li key={link}>
                    <a href="#" className="text-sm text-gray-600 hover:text-blue-700 transition-colors">{link}</a>
                  </li>
                ))}
              </ul>
            </div>
          ))}
        </div>
      </footer>
    </div>
  );
}
