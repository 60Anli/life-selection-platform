-- 姣旇緝绾跨▼鏍囩ず涓庨攣涓殑鏍囩ず鏄惁涓€鑷?
if(redis.call('get', KEYS[1]) ==  ARGV[1]) then
    -- 閲婃斁閿?del key
    return redis.call('del', KEYS[1])
end
return 0