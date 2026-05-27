import assert from "node:assert/strict";
import {
  loginRequest,
  logoutRequest,
  setAuthToken,
  signupRequest,
} from "../../src/api/watchlistApi.js";

async function runTest(name, callback) {
  try {
    await callback();
    console.log(`PASS ${name}`);
  } catch (error) {
    console.error(`FAIL ${name}`);
    throw error;
  }
}

await runTest("signupRequest posts the expected registration payload", async () => {
  const calls = [];
  globalThis.fetch = async (url, options) => {
    calls.push({ url, options });
    return {
      ok: true,
      status: 200,
      async text() {
        return JSON.stringify({
          username: "alice",
          roles: ["USER"],
          permissions: ["SHOW_READ", "SHOW_WRITE"],
          token: "signup-token",
          sessionTimeoutMinutes: 15,
        });
      },
    };
  };

  const result = await signupRequest("alice", "Password123", "Password123");

  assert.equal(calls.length, 1);
  assert.equal(calls[0].url, "https://localhost:8443/api/auth/signup");
  assert.equal(calls[0].options.method, "POST");
  assert.deepEqual(JSON.parse(calls[0].options.body), {
    username: "alice",
    password: "Password123",
    confirmPassword: "Password123",
  });
  assert.equal(result.token, "signup-token");
});

await runTest("loginRequest posts credentials and returns the issued token", async () => {
  globalThis.fetch = async (_url, options) => ({
    ok: true,
    status: 200,
    async text() {
      return JSON.stringify({
        username: "admin",
        roles: ["ADMIN"],
        permissions: ["SHOW_READ", "SHOW_WRITE", "USER_MANAGE"],
        token: "login-token",
        sessionTimeoutMinutes: 30,
        echoedMethod: options.method,
      });
    },
  });

  const result = await loginRequest("admin", "Password123");

  assert.equal(result.username, "admin");
  assert.equal(result.token, "login-token");
  assert.deepEqual(result.roles, ["ADMIN"]);
});

await runTest("logoutRequest sends the bearer token in the Authorization header", async () => {
  const calls = [];
  setAuthToken("session-token");

  globalThis.fetch = async (url, options) => {
    calls.push({ url, options });
    return {
      ok: true,
      status: 204,
      async text() {
        return "";
      },
    };
  };

  await logoutRequest();

  assert.equal(calls.length, 1);
  assert.equal(calls[0].url, "https://localhost:8443/api/auth/logout");
  assert.equal(calls[0].options.method, "POST");
  assert.equal(calls[0].options.headers.Authorization, "Bearer session-token");

  setAuthToken("");
});
