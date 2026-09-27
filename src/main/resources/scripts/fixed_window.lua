local key = KEYS[1]
local limit = tonumber(ARGV[1])
local windowSizeInSeconds = tonumber(ARGV[2])

local current = redis.call('GET', key)

if current == false then
    redis.call('SET', key, 1, 'EX', windowSizeInSeconds)
    return 1
end

current = tonumber(current)

if current < limit then
    redis.call('INCR', key)
    return 1
else
    return 0
end

