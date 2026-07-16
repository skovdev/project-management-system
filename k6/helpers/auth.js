import http from 'k6/http';
import { check } from 'k6';
import { BASE_URL, JSON_HEADERS } from '../config.js';

export function signUp(username, password, email, firstName, lastName) {
  const res = http.post(
    `${BASE_URL.gateway}/auth/sign-up`,
    JSON.stringify({ username, password, email, firstName, lastName }),
    { headers: JSON_HEADERS },
  );
  check(res, { 'signUp 200': (r) => r.status === 200 });
  return res;
}

export function signIn(username, password) {
  const res = http.post(
    `${BASE_URL.gateway}/auth/sign-in`,
    JSON.stringify({ username, password }),
    { headers: JSON_HEADERS },
  );
  const ok = check(res, { 'signIn 200': (r) => r.status === 200 });
  if (!ok) {
    console.error(`signIn failed: ${res.status} ${res.body}`);
    return null;
  }
  const body = JSON.parse(res.body);
  return { token: body.data.token, authUserId: body.data.authUserId };
}
