local key = KEYS[1]
local limit = tonumber(ARGV[1])
local windowSizeInSeconds = tonumber(ARGV[2])

local now = redis.call('TIME')
local currentTime = tonumber(now[1]) + (tonumber(now[2]) / 1000000)

local windowStart = currentTime - windowSizeInSeconds

redis.call('ZREMRANGEBYSCORE', key, 0, windowStart)

local count = redis.call('ZCARD', key)

if count < limit then
    redis.call('ZADD', key, currentTime, currentTime)
    redis.call('EXPIRE', key, windowSizeInSeconds)
    return 1
else
    return 0
end