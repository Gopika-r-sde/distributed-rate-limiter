const ALGORITHMS = [
  { value: 'FIXED_WINDOW', label: 'Fixed Window' },
  { value: 'TOKEN_BUCKET', label: 'Token Bucket' },
  { value: 'SLIDING_WINDOW_LOG', label: 'Sliding Window Log' },
];

function AlgorithmSelector({ value, onChange, disabled = false }) {
  return (
    <select
      value={value}
      onChange={(event) => onChange(event.target.value)}
      disabled={disabled}
    >
      {ALGORITHMS.map((algorithm) => (
        <option key={algorithm.value} value={algorithm.value}>
          {algorithm.label}
        </option>
      ))}
    </select>
  );
}

export default AlgorithmSelector;