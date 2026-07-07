"use client";
import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import Alert from "@/app/components/Alert";
import { getSession, clearSession } from "@/lib/session";
import { BFF_URL } from "@/lib/bff";
import type { UserProfile } from "@/features/users/users.types";
import { DashboardNav } from "./components/DashboardNav";
import { ProfileTab } from "./components/ProfileTab";
import { AppointmentsTab } from "./components/AppointmentsTab";
import { WaitlistTab } from "./components/WaitlistTab";

type Tab = "profile" | "appointments" | "waitlist";

const TAB_LABELS: Record<Tab, string> = {
  profile: "Mi perfil",
  appointments: "Mis citas",
  waitlist: "Lista de espera",
};

const STAFF_ROLES = new Set(["DOCTOR", "ADMIN", "NURSE", "RECEPTIONIST"]);

export default function Dashboard() {
  const [user, setUser] = useState<UserProfile | null>(null);
  const [session, setSession] = useState({
    userId: "",
    role: "",
    token: "",
    email: "",
  });
  const [activeTab, setActiveTab] = useState<Tab>("profile");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const router = useRouter();

  useEffect(() => {
    const { token, userId, email, role } = getSession();
    if (!token || !userId) {
      router.push("/");
      return;
    }
    setSession({ userId, role, token, email });

    fetch(`${BFF_URL}/api/users/${userId}`, {
      headers: { Authorization: `Bearer ${token}` },
    })
      .then((res) => {
        if (!res.ok) throw new Error("No se pudo cargar el perfil");
        return res.json() as Promise<UserProfile>;
      })
      .then(setUser)
      .catch((e: Error) => setError(e.message))
      .finally(() => setLoading(false));
  }, [router]);

  const isStaff = STAFF_ROLES.has(session.role);

  return (
    <div className="min-h-screen bg-gray-50 flex flex-col">
      <DashboardNav
        email={session.email}
        role={session.role}
        onLogout={() => {
          clearSession();
          router.push("/");
        }}
      />

      <div className="max-w-4xl mx-auto px-6 py-8 w-full">
        <div className="flex gap-1 bg-white border border-gray-100 rounded-xl p-1 mb-8 shadow-sm w-fit">
          {(["profile", "appointments", "waitlist"] as Tab[]).map((tab) => (
            <button
              key={tab}
              onClick={() => setActiveTab(tab)}
              className={`px-5 py-2 rounded-lg text-sm font-semibold transition-all ${
                activeTab === tab
                  ? "bg-blue-700 text-white shadow"
                  : "text-gray-500 hover:text-gray-700"
              }`}
            >
              {TAB_LABELS[tab]}
            </button>
          ))}
        </div>

        {error && <Alert variant="error" message={error} className="mb-4" />}
        {loading && activeTab === "profile" && (
          <div className="bg-white rounded-2xl p-12 text-center text-gray-400 text-sm">
            Cargando perfil...
          </div>
        )}

        {activeTab === "profile" && user && <ProfileTab user={user} />}

        {activeTab === "appointments" && session.userId && (
          <AppointmentsTab
            sessionUserId={session.userId}
            sessionRole={session.role}
            sessionToken={session.token}
            isStaff={isStaff}
          />
        )}

        {activeTab === "waitlist" && session.userId && (
          <WaitlistTab
            sessionUserId={session.userId}
            sessionRole={session.role}
            sessionToken={session.token}
            isStaff={isStaff}
          />
        )}
      </div>
    </div>
  );
}
