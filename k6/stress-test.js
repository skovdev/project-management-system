// k6 stress test — project-management-system
//
// Usage:
//   k6 run --env USERNAME=you --env PASSWORD=secret k6/stress-test.js
//   k6 run --env USERNAME=you --env PASSWORD=secret --env PROFILE=smoke  k6/stress-test.js
//   k6 run --env USERNAME=you --env PASSWORD=secret --env PROFILE=stress k6/stress-test.js
//   k6 run --env USERNAME=you --env PASSWORD=secret --env PROFILE=spike  k6/stress-test.js
//
// Prerequisites: docker-compose up (all services running)

import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate } from 'k6/metrics';

import { BASE_URL, authHeaders, STAGES, THRESHOLDS } from './config.js';
import { signIn } from './helpers/auth.js';
import { usersScenario }         from './scenarios/users.js';
import { projectsScenario }      from './scenarios/projects.js';
import { tasksScenario }         from './scenarios/tasks.js';
import { notificationsScenario } from './scenarios/notifications.js';

// ── Custom metrics ────────────────────────────────────────────────────────────
const setupErrorRate = new Rate('setup_errors');

// ── Load profile ──────────────────────────────────────────────────────────────
const profile = __ENV.PROFILE || 'load';
const stages  = STAGES[profile] || STAGES.load;

// ── k6 options ────────────────────────────────────────────────────────────────
export const options = {
  scenarios: {
    users_scenario: {
      executor:  'ramping-vus',
      exec:      'runUsers',
      stages,
      startTime: '0s',
    },
    projects_scenario: {
      executor:  'ramping-vus',
      exec:      'runProjects',
      stages,
      startTime: '5s',
    },
    tasks_scenario: {
      executor:  'ramping-vus',
      exec:      'runTasks',
      stages,
      startTime: '10s',
    },
    notifications_scenario: {
      executor:  'ramping-vus',
      exec:      'runNotifications',
      stages,
      startTime: '15s',
    },
  },
  thresholds: THRESHOLDS,
};

// ── Setup: sign in + seed test data ──────────────────────────────────────────
// Runs once before all VUs start. Return value is passed to every exec function.
export function setup() {
  const username = __ENV.USERNAME;
  const password = __ENV.PASSWORD;

  if (!username || !password) {
    console.error('Setup: USERNAME and PASSWORD env vars are required');
    return null;
  }

  const auth = signIn(username, password);
  if (!auth) {
    console.error('Setup: signIn failed — check your credentials');
    setupErrorRate.add(1);
    return null;
  }
  const { token, authUserId } = auth;
  const headers = authHeaders(token);

  // Resolve user service ID from /users endpoint
  const usersRes = http.get(`${BASE_URL.gateway}/users?page=0&size=100`, { headers });
  let userId = null;
  try {
    const users = JSON.parse(usersRes.body).content || [];
    const me    = users.find((u) => u.authUserId === authUserId);
    userId      = me ? me.id : null;
  } catch { /* ignored */ }

  // Create a shared project for read/update scenarios
  const projectRes = http.post(
    `${BASE_URL.gateway}/projects`,
    JSON.stringify({
      title:             'k6 Seed Project',
      description:       'Shared project for k6 stress tests',
      projectStatusType: 'PLANNING',
      startDate:         '2026-01-01T00:00:00',
      endDate:           '2026-12-31T23:59:59',
      userId:            null,
    }),
    { headers },
  );
  let projectId = null;
  try { projectId = JSON.parse(projectRes.body).data.id; } catch { /* ignored */ }

  if (!projectId) {
    console.error(`Setup: project creation failed — ${projectRes.status} ${projectRes.body}`);
    setupErrorRate.add(1);
  }

  // Create a shared task for read/update scenarios
  let taskId = null;
  if (projectId) {
    const taskRes = http.post(
      `${BASE_URL.gateway}/tasks`,
      JSON.stringify({
        title:            'k6 Seed Task',
        description:      'Shared task for k6 stress tests',
        taskStatusType:   'TODO',
        taskPriorityType: 'MEDIUM',
        active:           true,
        projectId,
        userId:           null,
        acceptanceCriteria: null,
      }),
      { headers },
    );
    try { taskId = JSON.parse(taskRes.body).data.id; } catch { /* ignored */ }

    if (!taskId) {
      console.error(`Setup: task creation failed — ${taskRes.status} ${taskRes.body}`);
      setupErrorRate.add(1);
    }
  }

  console.log(`Setup complete — user=${username} project=${projectId} task=${taskId}`);

  return { token, authUserId, userId, username, projectId, taskId };
}

// ── Teardown: remove seeded project + task ────────────────────────────────────
export function teardown(data) {
  if (!data) return;
  const headers = authHeaders(data.token);

  if (data.taskId) {
    http.del(`${BASE_URL.gateway}/tasks/${data.taskId}`, null, { headers });
  }
  if (data.projectId) {
    http.del(`${BASE_URL.gateway}/projects/${data.projectId}`, null, { headers });
  }
  console.log('Teardown complete — seeded project and task removed');
}

// ── Scenario executors ────────────────────────────────────────────────────────
export function runUsers(data)         { if (data) usersScenario(data);         }
export function runProjects(data)      { if (data) projectsScenario(data);      }
export function runTasks(data)         { if (data) tasksScenario(data);         }
export function runNotifications(data) { if (data) notificationsScenario(data); }
