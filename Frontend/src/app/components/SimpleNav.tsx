import Link from "next/link";
import { HospitalLogo } from "./HospitalLogo";

interface SimpleNavProps {
  logoHref?: string;
  logoSubtitle?: string;
  backHref: string;
  backLabel: string;
  right?: React.ReactNode;
}

export function SimpleNav({
  logoHref = "/",
  logoSubtitle,
  backHref,
  backLabel,
  right,
}: SimpleNavProps) {
  return (
    <nav className="bg-white border-b border-gray-100 px-8 py-4 flex items-center justify-between shadow-sm">
      <HospitalLogo href={logoHref} subtitle={logoSubtitle} />
      <div className="flex items-center gap-4">
        {right}
        <Link href={backHref} className="text-sm text-blue-700 hover:underline font-medium">
          {backLabel}
        </Link>
      </div>
    </nav>
  );
}
