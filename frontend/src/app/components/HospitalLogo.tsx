import Link from "next/link";
import { HOSPITAL_CONFIG } from "@/lib/hospitalConfig";

interface HospitalLogoProps {
  size?: "sm" | "md";
  href?: string;
  subtitle?: string;
}

const DEFAULT_SUBTITLE: Record<"sm" | "md", string> = {
  sm: HOSPITAL_CONFIG.address,
  md: HOSPITAL_CONFIG.tagline,
};

export function HospitalLogo({ size = "md", href, subtitle }: HospitalLogoProps) {
  const box = size === "sm" ? "w-8 h-8 text-lg" : "w-10 h-10 text-xl";
  const label = size === "sm" ? "text-sm" : "";

  const inner = (
    <>
      <div className={`${box} bg-blue-700 rounded-lg flex items-center justify-center text-white font-black`}>
        +
      </div>
      <div>
        <p className={`font-bold text-gray-900 ${label}`}>Salud RedNorte</p>
        <p className="text-xs text-gray-500">{subtitle ?? DEFAULT_SUBTITLE[size]}</p>
      </div>
    </>
  );

  if (href) {
    return <Link href={href} className="flex items-center gap-3">{inner}</Link>;
  }
  return <div className="flex items-center gap-3">{inner}</div>;
}
