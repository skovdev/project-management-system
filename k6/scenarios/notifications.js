import http from 'k6/http';
import { check, sleep } from 'k6';
import { BASE_URL, authHeaders } from '../config.js';

// ── Notifications scenario ────────────────────────────────────────────────────
// Notifications are created by Kafka consumers (Saga events), so this scenario
// only exercises read and mark-as-read paths. If the DB has no notifications for
// the test user the findById / markAsRead checks will be skipped gracefully.
export function notificationsScenario(data) {
  const headers = authHeaders(data.token);

  // List all notifications (paginated)
  const listRes = http.get(`${BASE_URL.gateway}/notifications?page=0&size=20`, { headers });
  check(listRes, {
    'notifications | findAll 200':         (r) => r.status === 200,
    'notifications | findAll has content': (r) => {
      try { return Array.isArray(JSON.parse(r.body).content); } catch { return false; }
    },
  });
  sleep(0.5);

  // Unread bell-dropdown endpoint
  const unreadRes = http.get(`${BASE_URL.gateway}/notifications/unread`, { headers });
  check(unreadRes, { 'notifications | findUnread 200': (r) => r.status === 200 });
  sleep(0.5);

  // If there are any notifications, exercise findById + markAsRead
  let notificationId = null;
  try {
    const body = JSON.parse(listRes.body);
    if (body.content && body.content.length > 0) {
      notificationId = body.content[0].id;
    }
  } catch { /* ignored */ }

  if (notificationId) {
    const findRes = http.get(`${BASE_URL.gateway}/notifications/${notificationId}`, { headers });
    check(findRes, { 'notifications | findById 200': (r) => r.status === 200 });
    sleep(0.3);

    const markRes = http.put(
      `${BASE_URL.gateway}/notifications/${notificationId}/read`,
      null,
      { headers },
    );
    check(markRes, { 'notifications | markAsRead 200': (r) => r.status === 200 });
    sleep(0.5);
  }

  sleep(1);
}
