import http from 'k6/http';
import { check, sleep } from 'k6';
import { BASE_URL, authHeaders } from '../config.js';

// ── Users scenario ────────────────────────────────────────────────────────────
export function usersScenario(data) {
  const headers = authHeaders(data.token);

  // List users (paginated)
  const listRes = http.get(`${BASE_URL.gateway}/users?page=0&size=20`, { headers });
  check(listRes, {
    'users | findAll 200':          (r) => r.status === 200,
    'users | findAll has content':  (r) => {
      try { return Array.isArray(JSON.parse(r.body).content); } catch { return false; }
    },
  });
  sleep(0.5);

  // Find own user profile by ID
  const findRes = http.get(`${BASE_URL.gateway}/users/${data.userId}`, { headers });
  check(findRes, {
    'users | findById 200': (r) => r.status === 200,
    'users | findById has id': (r) => {
      try { return !!JSON.parse(r.body).id; } catch { return false; }
    },
  });
  sleep(0.5);

  // Update own profile (non-destructive — keeps same values)
  const updateRes = http.put(
    `${BASE_URL.gateway}/users/${data.userId}`,
    JSON.stringify({
      id:         data.userId,
      firstName:  'StressTest',
      lastName:   'User',
      email:      `${data.username}@stress.test`,
      authUserId: data.authUserId,
      avatarUrl:  null,
    }),
    { headers },
  );
  check(updateRes, { 'users | update 200': (r) => r.status === 200 });
  sleep(1);
}
