import { useState } from 'react';
import AlgorithmSelector from './AlgorithmSelector';
import { createClient, errorMessage } from '../api/rateLimiterApi';

function ClientConfigForm({ onCreated }) {
  const [clientId, setClientId] = useState('');
  const [maxRequests, setMaxRequests] = useState(5);
  const [windowSizeInSeconds, setWindowSizeInSeconds] = useState(60);
  const [algorithmType, setAlgorithmType] = useState('FIXED_WINDOW');
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  async function handleSubmit(event) {
    event.preventDefault();
    setError('');
    setSubmitting(true);

    const result = await createClient({
      clientId: clientId.trim(),
      maxRequests: Number(maxRequests),
      windowSizeInSeconds: Number(windowSizeInSeconds),
      algorithmType,
    });

    setSubmitting(false);

    if (!result.ok) {
      setError(errorMessage(result));
      return;
    }

    setClientId('');
    onCreated(result.body);
  }

  return (
    <form className="card" onSubmit={handleSubmit}>
      <h2>Register a client</h2>

      <label>
        Client ID
        <input
          type="text"
          value={clientId}
          onChange={(event) => setClientId(event.target.value)}
          placeholder="e.g. client_101"
          required
        />
      </label>

      <label>
        Max requests
        <input
          type="number"
          min="1"
          value={maxRequests}
          onChange={(event) => setMaxRequests(event.target.value)}
          required
        />
      </label>

      <label>
        Window (seconds)
        <input
          type="number"
          min="1"
          value={windowSizeInSeconds}
          onChange={(event) => setWindowSizeInSeconds(event.target.value)}
          required
        />
      </label>

      <label>
        Algorithm
        <AlgorithmSelector value={algorithmType} onChange={setAlgorithmType} />
      </label>

      {error && <p className="error">{error}</p>}

      <button type="submit" disabled={submitting}>
        {submitting ? 'Saving...' : 'Register client'}
      </button>
    </form>
  );
}

export default ClientConfigForm;