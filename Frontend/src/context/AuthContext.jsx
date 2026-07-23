/* ============================================================
   AdStudio · Auth context
   Holds the signed-in user, JWT and the eligibility list.
   On login it calls the dummy backend on :8181; if that is
   unreachable it signs in locally so the UI is fully usable.
   The eligibility list (GET /eligibilityList) decides which
   portals are unlocked vs. show the red "Not authorized" box.
   ============================================================ */

import React, { createContext, useContext, useState, useEffect, useCallback } from "react";
import { API_BASE, ENDPOINTS } from "../api/endpoints";
import { getToken, setToken, clearToken } from "../api/apiClient";
import { MOCK_ELIGIBILITY } from "../data/mockData";

const AuthContext = createContext(null);

const USER_KEY = "adstudio_user";

function deriveRoleFromEmail(email) {
  const e = (email || "").toLowerCase();
  if (e.includes("publisher") || e.includes("pub")) return "Publisher";
  if (e.includes("planner") || e.includes("media")) return "Media Planner";
  if (e.includes("creative")) return "Creative Manager";
  if (e.includes("finance")) return "Finance Executive";
  if (e.includes("advertiser") || e.includes("brand")) return "Advertiser / Brand Manager";
  if (e.includes("admin")) return "Ad Operations Admin";
  return "Ad Operations Admin";
}

function deriveNameFromEmail(email) {
  const local = (email || "user").split("@")[0];
  return local
    .split(/[._-]+/)
    .filter(Boolean)
    .map((w) => w.charAt(0).toUpperCase() + w.slice(1))
    .join(" ");
}

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    try {
      const raw = localStorage.getItem(USER_KEY);
      return raw ? JSON.parse(raw) : null;
    } catch {
      return null;
    }
  });
  const [eligibility, setEligibility] = useState([]);
  const [eligibilityLoading, setEligibilityLoading] = useState(false);
  const [eligibilityIsMock, setEligibilityIsMock] = useState(false);

  const isAuthenticated = !!getToken() && !!user;

  /* ---- fetch the eligibility list from :8181 (mock fallback) ---- */
  const loadEligibility = useCallback(async () => {
    setEligibilityLoading(true);
    try {
      const res = await fetch(`${API_BASE}/${ENDPOINTS.eligibilityList}`, {
        headers: getToken() ? { Authorization: `Bearer ${getToken()}` } : {},
      });
      if (!res.ok) throw new Error("no backend");
      const json = await res.json();
      const list = Array.isArray(json) ? json : json.data;
      setEligibility(list || []);
      setEligibilityIsMock(false);
    } catch {
      // No backend on :8181 -> use the mock eligibility list.
      setEligibility(MOCK_ELIGIBILITY);
      setEligibilityIsMock(true);
    } finally {
      setEligibilityLoading(false);
    }
  }, []);

  // Load eligibility once we have a session.
  useEffect(() => {
    if (isAuthenticated) loadEligibility();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [isAuthenticated]);

  /* ---- login ---- */
  const login = useCallback(async (email, password) => {
    try {
      const res = await fetch(`${API_BASE}/${ENDPOINTS.login}`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ email, password }),
      });
      if (!res.ok) throw new Error("backend login failed");
      const json = await res.json();
      const payload = json.data || json;
      setToken(payload.token || "live-session");
      const u = payload.user || {
        name: deriveNameFromEmail(email),
        email,
        role: payload.role || deriveRoleFromEmail(email),
      };
      localStorage.setItem(USER_KEY, JSON.stringify(u));
      setUser(u);
      return { ok: true };
    } catch {
      // Dummy/offline login so the UI is usable without a backend.
      const u = {
        name: deriveNameFromEmail(email) || MOCK_USER.name,
        email: email || MOCK_USER.email,
        role: deriveRoleFromEmail(email),
        accountId: MOCK_USER.accountId,
      };
      setToken("demo-session-token");
      localStorage.setItem(USER_KEY, JSON.stringify(u));
      setUser(u);
      return { ok: true, demo: true };
    }
  }, []);

  const logout = useCallback(() => {
    clearToken();
    localStorage.removeItem(USER_KEY);
    setUser(null);
    setEligibility([]);
  }, []);

  const isPortalAllowed = useCallback(
    (key) => eligibility.includes(key),
    [eligibility]
  );

  const value = {
    user,
    isAuthenticated,
    eligibility,
    eligibilityLoading,
    eligibilityIsMock,
    login,
    logout,
    isPortalAllowed,
    reloadEligibility: loadEligibility,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth must be used within AuthProvider");
  return ctx;
}

export default AuthContext;
