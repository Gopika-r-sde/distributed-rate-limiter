local key = KEYS[1]
local capacity = tonumber(ARGV[1])
local refillRatePerSecond = tonumber(ARGV[2])

local bucket = redis.call('HMGET', key, 'tokens', 'lastRefillTime')
local tokens = tonumber(bucket[1])
local lastRefillTime = tonumber(bucket[2])

local now = redis.call('TIME')
local currentTime = tonumber(now[1])

if tokens == nil then
    tokens = capacity
    lastRefillTime = currentTime
end

local elapsedSeconds = currentTime - lastRefillTime
local tokensToAdd = elapsedSeconds * refillRatePerSecond
tokens = math.min(capacity, tokens + tokensToAdd)

if tokens >= 1 then
    tokens = tokens - 1
    redis.call('HSET', key, 'tokens', tokens, 'lastRefillTime', currentTime)
    redis.call('EXPIRE', key, 3600)
    return 1
else
    redis.call('HSET', key, 'tokens', tokens, 'lastRefillTime', currentTime)
    redis.call('EXPIRE', key, 3600)
    return 0
end