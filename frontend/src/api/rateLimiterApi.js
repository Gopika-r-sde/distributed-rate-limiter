const API_BASE_URL = 'http://localhost:9090/api';

async function request(path, options = {}) {
  try {
    const response = await fetch(`${API_BASE_URL}${path}`, {
      headers: { 'Content-Type': 'application/json' },
      ...options,
    });

    let body = null;
    try {
      body = await response.json();
    } catch {
      body = null;
    }

    return { ok: response.ok, status: response.status, body };
  } catch {
    return {
      ok: false,
      status: 0,
      body: { message: 'Cannot reach the server. Is the backend running?' },
    };
  }
}

export function getClients() {
  return request('/clients');
}

export function createClient(client) {
  return request('/clients', { method: 'POST', body: JSON.stringify(client) });
}

export function updateClient(clientId, client) {
  return request(`/clients/${encodeURIComponent(clientId)}`, {
    method: 'PUT',
    body: JSON.stringify(client),
  });
}

export function getClientLogs(clientId) {
  return request(`/clients/${encodeURIComponent(clientId)}/logs`);
}

export function checkRateLimit(clientId) {
  return request('/ratelimit/check', {
    method: 'POST',
    body: JSON.stringify({ clientId }),
  });
}

export function errorMessage(result) {
  if (result.body && result.body.message) {
    return result.body.message;
  }

  switch (result.status) {
    case 400: return 'Invalid input. Check the values and try again.';
    case 404: return 'Client not found.';
    case 409: return 'A client with this ID already exists.';
    case 429: return 'Rate limit exceeded.';
    case 0: return 'Cannot reach the server. Is the backend running?';
    default: return `Request failed (status ${result.status}).`;
  }
}