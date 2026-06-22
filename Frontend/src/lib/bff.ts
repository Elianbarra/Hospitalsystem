/**
 * URL base del BFF.
 * En desarrollo apunta a localhost:3000 (o la var NEXT_PUBLIC_BFF_URL del .env.local).
 * En producción apunta a la URL del BFF desplegado en Vercel.
 */
export const BFF_URL =
  process.env.NEXT_PUBLIC_BFF_URL ?? "http://localhost:3000";
