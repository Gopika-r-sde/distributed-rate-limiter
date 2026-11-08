const WINDOW_SECONDS = 60;
const BUCKET_SECONDS = 5;
const BUCKET_COUNT = WINDOW_SECONDS / BUCKET_SECONDS;
const CHART_HEIGHT = 160;

function buildBuckets(logs, now) {
  const windowStart = now - WINDOW_SECONDS * 1000;
  const bucketMs = BUCKET_SECONDS * 1000;
  const buckets = Array.from({ length: BUCKET_COUNT }, () => ({ allowed: 0, denied: 0 }));

  for (const log of logs) {
    const time = new Date(log.timestamp).getTime();
    if (time < windowStart) continue;

    const index = Math.min(BUCKET_COUNT - 1, Math.floor((time - windowStart) / bucketMs));
    if (log.allowed) {
      buckets[index].allowed += 1;
    } else {
      buckets[index].denied += 1;
    }
  }

  return buckets;
}

function LiveRequestChart({ logs, now }) {
  if (now === null) {
    return <p className="muted">Select a client to see its live traffic.</p>;
  }

  const buckets = buildBuckets(logs, now);
  const totalAllowed = buckets.reduce((sum, bucket) => sum + bucket.allowed, 0);
  const totalDenied = buckets.reduce((sum, bucket) => sum + bucket.denied, 0);
  const maxInBucket = Math.max(1, ...buckets.map((bucket) => bucket.allowed + bucket.denied));
  const scale = CHART_HEIGHT / maxInBucket;

  return (
    <div>
      <p className="legend">
        <span className="dot allowed" /> Allowed: {totalAllowed}
        <span className="dot denied" /> Denied: {totalDenied}
        <span className="muted">(last {WINDOW_SECONDS} seconds)</span>
      </p>

      <div className="chart" style={{ height: CHART_HEIGHT }}>
        {buckets.map((bucket, index) => (
          <div
            key={index}
            className="bar-slot"
            title={`${bucket.allowed} allowed, ${bucket.denied} denied`}
          >
            <div className="bar denied" style={{ height: bucket.denied * scale }} />
            <div className="bar allowed" style={{ height: bucket.allowed * scale }} />
          </div>
        ))}
      </div>

      <div className="chart-axis">
        <span>{WINDOW_SECONDS}s ago</span>
        <span>now</span>
      </div>
    </div>
  );
}

export default LiveRequestChart;