/**
 * URL base del BFF.
 * Se configura via NEXT_PUBLIC_BFF_URL en .env.local.
 * Fallback apunta al puerto del BFF (8090), no al de Next.js (3001).
 */
export const BFF_URL =
  process.env.NEXT_PUBLIC_BFF_URL ?? "http://localhost:8090";
