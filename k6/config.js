// All traffic goes through the API gateway (port 8762).
export const BASE_URL = {
  gateway: 'http://localhost:8762/api/v1',
};

export const JSON_HEADERS = { 'Content-Type': 'application/json' };

export function authHeaders(token) {
  return { 'Content-Type': 'application/json', Authorization: `Bearer ${token}` };
}

// Reusable load profiles — pass as `stages` in scenario options
export const STAGES = {
  smoke: [
    { duration: '30s', target: 5  },
    { duration: '30s', target: 0  },
  ],
  load: [
    { duration: '30s', target: 10  },
    { duration: '1m',  target: 50  },
    { duration: '2m',  target: 100 },
    { duration: '1m',  target: 50  },
    { duration: '30s', target: 0   },
  ],
  stress: [
    { duration: '30s', target: 50  },
    { duration: '1m',  target: 150 },
    { duration: '2m',  target: 200 },
    { duration: '1m',  target: 100 },
    { duration: '30s', target: 0   },
  ],
  spike: [
    { duration: '10s', target: 5   },
    { duration: '20s', target: 200 },
    { duration: '10s', target: 5   },
    { duration: '30s', target: 0   },
  ],
};

// Global SLO thresholds applied to every run
export const THRESHOLDS = {
  http_req_duration: ['p(95)<500', 'p(99)<1500'],
  http_req_failed:   ['rate<0.05'],
  checks:            ['rate>0.95'],
};
