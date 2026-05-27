import Cookies from "js-cookie";

export function setCookie(name, value, days = 7) {
  Cookies.set(name, JSON.stringify(value), { expires: days, sameSite: "Lax" });
}

export function getCookie(name) {
  const value = Cookies.get(name);
  if (!value) return null;

  try {
    return JSON.parse(value);
  } catch {
    return value;
  }
}

export function removeCookie(name) {
  Cookies.remove(name);
}
