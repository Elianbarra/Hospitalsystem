const KEYS = ["token", "userId", "email", "role"] as const;

function read(key: string): string {
  return localStorage.getItem(key) ?? sessionStorage.getItem(key) ?? "";
}

export function getSession() {
  return {
    token: read("token") || null,
    userId: read("userId") || null,
    email: read("email"),
    role: read("role"),
  };
}

export function clearSession() {
  KEYS.forEach((k) => {
    localStorage.removeItem(k);
    sessionStorage.removeItem(k);
  });
}
