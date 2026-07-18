import http from 'k6/http';
import { check, sleep } from 'k6';
import { BASE_URL, authHeaders } from '../config.js';

// ── Tasks + Comments scenario ─────────────────────────────────────────────────
export function tasksScenario(data) {
  const headers = authHeaders(data.token);

  // List tasks (paginated)
  const listRes = http.get(`${BASE_URL.gateway}/tasks?page=0&size=20`, { headers });
  check(listRes, {
    'tasks | findAll 200':         (r) => r.status === 200,
    'tasks | findAll has content': (r) => {
      try { return Array.isArray(JSON.parse(r.body).content); } catch { return false; }
    },
  });
  sleep(0.5);

  // Find the seeded task (skip if setup failed to create it)
  if (data.taskId) {
    const findRes = http.get(`${BASE_URL.gateway}/tasks/${data.taskId}`, { headers });
    check(findRes, { 'tasks | findById 200': (r) => r.status === 200 });
    sleep(0.5);
  }

  // Create a new task
  const createRes = http.post(
    `${BASE_URL.gateway}/tasks`,
    JSON.stringify({
      title:            'k6 Stress Task',
      description:      'Created during k6 stress test',
      taskStatusType:   'TODO',
      taskPriorityType: 'MEDIUM',
      active:           true,
      projectId:        data.projectId,
      userId:           null,
      acceptanceCriteria: null,
    }),
    { headers },
  );
  check(createRes, { 'tasks | create 200': (r) => r.status === 200 });
  sleep(0.5);

  let newTaskId = null;
  try { newTaskId = JSON.parse(createRes.body).data.id; } catch { /* ignored */ }

  // Update and comment on the seeded task (skip if setup failed to create it)
  if (data.taskId) {
    const updateRes = http.put(
      `${BASE_URL.gateway}/tasks/${data.taskId}`,
      JSON.stringify({
        title:            'k6 Updated Task',
        description:      'Updated during k6 stress test',
        taskStatusType:   'IN_PROGRESS',
        taskPriorityType: 'HIGH',
        active:           true,
        projectId:        data.projectId,
        userId:           null,
        acceptanceCriteria: null,
      }),
      { headers },
    );
    check(updateRes, { 'tasks | update 200': (r) => r.status === 200 });
    sleep(0.5);

    commentsScenario(data.taskId, headers);
  }

  sleep(1);
}

function commentsScenario(taskId, headers) {
  const base = `${BASE_URL.gateway}/tasks/${taskId}/comments`;

  // List comments
  const listRes = http.get(`${base}?page=0&size=10`, { headers });
  check(listRes, { 'comments | findAll 200': (r) => r.status === 200 });
  sleep(0.3);

  // Create a comment
  const createRes = http.post(
    base,
    JSON.stringify({ content: 'k6 stress test comment' }),
    { headers },
  );
  check(createRes, { 'comments | create 200': (r) => r.status === 200 });
  sleep(0.3);

  let commentId = null;
  try { commentId = JSON.parse(createRes.body).data.id; } catch { /* ignored */ }

  if (commentId) {
    // Update the comment
    const updateRes = http.put(
      `${base}/${commentId}`,
      JSON.stringify({ content: 'k6 updated comment' }),
      { headers },
    );
    check(updateRes, { 'comments | update 200': (r) => r.status === 200 });
    sleep(0.3);

    // Delete the comment
    const deleteRes = http.del(`${base}/${commentId}`, null, { headers });
    check(deleteRes, { 'comments | delete 200': (r) => r.status === 200 });
  }
}
