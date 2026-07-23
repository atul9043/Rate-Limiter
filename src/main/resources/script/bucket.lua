local key = KEYS[1]
local now = tonumber(ARGV[1])
local max_tokens = tonumber(ARGV[2])
local refill_rate = tonumber(ARGV[3])
local refill_interval = tonumber(ARGV[4])

local bucket = redis.call('HMGET', key, 'tokens', 'last_refill')
local tokens = tonumber(bucket[1])
local last_refill = tonumber(bucket[2])

if tokens == nil then
    last_refill = now
    tokens = max_tokens
end

local elapsed_time = now-last_refill
local refill = math.floor(elapsed_time/refill_interval)*refill_rate
if refill>0 then
   tokens = math.min(max_tokens, refill+tokens)
   last_refill=now
end

local allowed = 0
if tokens>0 then
    allowed = 1
    tokens = tokens-1
end

redis.call('HMSET', key, 'tokens', tokens, 'last_refill', last_refill)
redis.call('EXPIRE', key, 3600)

return {allowed, tokens}
    


