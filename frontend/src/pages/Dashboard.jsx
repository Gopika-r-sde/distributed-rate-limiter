import { useCallback, useEffect, useState } from 'react';
import AlgorithmSelector from '../components/AlgorithmSelector';
import ClientConfigForm from '../components/ClientConfigForm';
import LiveRequestChart from '../components/LiveRequestChart';
import {
  checkRateLimit,
  errorMessage,
  getClientLogs,
  getClients,
  updateClient,
} from '../api/rateLimiterApi';

const POLL_INTERVAL_MS = 2000;

function Dashboard() {
  const [clients, setClients] = useState([]);
  const [selectedClientId, setSelectedClientId] = useState('');
  const [logs, setLogs] = useState([]);
  const [lastUpdated, setLastUpdated] = useState(null);
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');
  const [sending, setSending] = useState(false);
  console.log('render -> clients:', clients, 'selected:', selectedClientId);

  const selectedClient = clients.find((client) => client.clientId === selectedClientId) ?? null;

  const refreshClients = useCallback(async () => {
    const result = await getClients();
    console.log('getClients result:', result);
    if (result.ok) {
      setClients(result.body);
    } else {
      setError(errorMessage(result));
    }
  }, []);

  useEffect(() => {
    refreshClients();
  }, [refreshClients]);

  useEffect(() => {
    if (!selectedClientId) return undefined;

    let cancelled = false;

    async function loadLogs() {
      const result = await getClientLogs(selectedClientId);
      if (cancelled || !result.ok) return;
      setLogs(result.body);
      setLastUpdated(Date.now());
    }

    loadLogs();
    const intervalId = setInterval(loadLogs, POLL_INTERVAL_MS);

    return () => {
      cancelled = true;
      clearInterval(intervalId);
    };
  }, [selectedClientId]);

  async function handleCreated(newClient) {
    if (!newClient) return;
    setError('');
    setMessage(`Client ${newClient.clientId} registered.`);
    await refreshClients();
    setSelectedClientId(newClient.clientId);
  }

  async function handleAlgorithmChange(client, algorithmType) {
    setError('');
    const result = await updateClient(client.clientId, {
      clientId: client.clientId,
      maxRequests: client.maxRequests,
      windowSizeInSeconds: client.windowSizeInSeconds,
      algorithmType,
    });

    if (!result.ok) {
      setError(errorMessage(result));
      return;
    }

    setMessage(`Algorithm switched to ${algorithmType}.`);
    await refreshClients();
  }

  async function sendRequests(count) {
    setError('');
    setSending(true);

    let allowed = 0;
    let denied = 0;

    for (let i = 0; i < count; i++) {
      const result = await checkRateLimit(selectedClientId);
      if (result.ok) {
        allowed += 1;
      } else if (result.status === 429) {
        denied += 1;
      } else {
        setError(errorMessage(result));
        break;
      }
    }

    if (allowed + denied > 0) {
      setMessage(`Sent ${allowed + denied} requests: ${allowed} allowed, ${denied} denied.`);
    }
    setSending(false);
  }

  return (
    <div className="page">
      <header>
        <h1>Distributed Rate Limiter</h1>
        <p className="muted">
          Register clients, send test traffic, and watch each algorithm decide.
        </p>
      </header>

      {error && <p className="error">{error}</p>}
      {message && <p className="notice">{message}</p>}

      <div className="layout">
        <ClientConfigForm onCreated={handleCreated} />

        <section className="card">
          <h2>Client</h2>

          <label>
            Choose a client
            <select
              value={selectedClientId}
              onChange={(event) => setSelectedClientId(event.target.value)}
            >
              <option value="" disabled>Select a client</option>
              {clients.map((client) => (
                <option key={client.id} value={client.clientId}>
                  {client.clientId}
                </option>
              ))}
            </select>
          </label>

          {selectedClient && (
            <>
              <p>
                Limit: <strong>{selectedClient.maxRequests}</strong> requests per{' '}
                <strong>{selectedClient.windowSizeInSeconds}</strong> seconds
              </p>

              <label>
                Algorithm
                <AlgorithmSelector
                  value={selectedClient.algorithmType}
                  onChange={(algorithmType) => handleAlgorithmChange(selectedClient, algorithmType)}
                  disabled={sending}
                />
              </label>

              <div className="actions">
                <button onClick={() => sendRequests(1)} disabled={sending}>
                  Send 1 request
                </button>
                <button onClick={() => sendRequests(10)} disabled={sending}>
                  Send burst of 10
                </button>
              </div>
            </>
          )}
        </section>
      </div>

      <section className="card">
        <h2>Live traffic</h2>
        <LiveRequestChart logs={logs} now={lastUpdated} />
        {lastUpdated && (
          <p className="muted">Updates every {POLL_INTERVAL_MS / 1000} seconds.</p>
        )}
      </section>
    </div>
  );
}

export default Dashboard;