import http from 'k6/http';
import { check, sleep } from 'k6';
import { BASE_URL, authHeaders } from '../config.js';

// ── Projects scenario ─────────────────────────────────────────────────────────
export function projectsScenario(data) {
  const headers = authHeaders(data.token);

  // List projects (paginated)
  const listRes = http.get(`${BASE_URL.gateway}/projects?page=0&size=20`, { headers });
  check(listRes, {
    'projects | findAll 200':         (r) => r.status === 200,
    'projects | findAll has content': (r) => {
      try { return Array.isArray(JSON.parse(r.body).content); } catch { return false; }
    },
  });
  sleep(0.5);

  // Find the seeded project
  const findRes = http.get(`${BASE_URL.gateway}/projects/${data.projectId}`, { headers });
  check(findRes, { 'projects | findById 200': (r) => r.status === 200 });
  sleep(0.5);

  // Create a new project (each VU creates its own, then deletes it)
  const createRes = http.post(
    `${BASE_URL.gateway}/projects`,
    JSON.stringify({
      title:             'k6 Stress Project',
      description:       'Created during k6 stress test',
      projectStatusType: 'PLANNING',
      startDate:         '2026-01-01T00:00:00',
      endDate:           '2026-12-31T23:59:59',
      userId:            null,
    }),
    { headers },
  );
  check(createRes, { 'projects | create 200': (r) => r.status === 200 });
  sleep(0.5);

  let newProjectId = null;
  try { newProjectId = JSON.parse(createRes.body).data.id; } catch { /* ignored */ }

  // Update the seeded project
  const updateRes = http.put(
    `${BASE_URL.gateway}/projects/${data.projectId}`,
    JSON.stringify({
      title:             'k6 Updated Project',
      description:       'Updated during k6 stress test',
      projectStatusType: 'IN_PROGRESS',
      startDate:         '2026-01-01T00:00:00',
      endDate:           '2026-12-31T23:59:59',
      userId:            null,
    }),
    { headers },
  );
  check(updateRes, { 'projects | update 200': (r) => r.status === 200 });
  sleep(0.5);

  sleep(1);
}
