local fcntl = require("posix.fcntl")
local unistd = require("posix.unistd")
local poll = require("posix.poll")
local json_null = require("cjson").null
local Channel = require("oc2.channel")

local blob = {}

local chunkSize = 32 * 1024
local readTimeout = 5000
local maxDepth = 32

blob.key = "$blob"
-- Binary not on the payload channel: in events, or past a message's one payload.
blob.bytesKey = "$bytes"
blob.maxOutbound = 512 * 1024
blob.maxInbound = blob.maxOutbound
blob.chunkSize = chunkSize
blob.readTimeout = readTimeout
blob.maxDepth = maxDepth
blob.marker = {}

local alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/"

function blob.encodeBase64(data)
  local out = {}
  for i = 1, #data, 3 do
    local a, b, c = data:byte(i, i + 2)
    local n = (a << 16) | ((b or 0) << 8) | (c or 0)
    local chars = {}
    for shift = 18, 0, -6 do
      local index = (n >> shift & 63) + 1
      chars[#chars + 1] = alphabet:sub(index, index)
    end
    if not b then chars[3] = "=" end
    if not c then chars[4] = "=" end
    out[#out + 1] = table.concat(chars)
  end
  return table.concat(out)
end

function blob.decodeBase64(text)
  text = text:gsub("[^%w%+/]", "") -- padding and line breaks
  local out = {}
  for i = 1, #text, 4 do
    local n, count = 0, 0
    for j = i, math.min(i + 3, #text) do
      n = (n << 6) | (alphabet:find(text:sub(j, j), 1, true) - 1)
      count = count + 1
    end
    n = n << (6 * (4 - count))
    out[#out + 1] = string.char(n >> 16 & 255, n >> 8 & 255, n & 255):sub(1, count - 1)
  end
  return table.concat(out)
end

function blob.wrap(data)
  if type(data) ~= "string" then
    error("a binary payload must be a string, got " .. type(data), 2)
  end
  return setmetatable({ data = data }, blob.marker)
end

function blob.checksum(data)
  local sum = 0
  local length = #data
  local i = 1
  while i <= length do
    local word
    if i + 3 <= length then
      word = string.unpack("<I4", data, i)
    else
      word = string.unpack("<I4", data:sub(i) .. string.rep("\0", 4 - (length - i + 1)))
    end
    sum = (((sum << 1) | (sum >> 31)) + word) & 0xFFFFFFFF
    i = i + 4
  end
  if sum >= 0x80000000 then
    sum = sum - 0x100000000
  end
  return sum
end

function blob.extract(...)
  local packed = table.pack(...)
  local parameters = {}
  local payload
  for i = 1, packed.n do
    local value = packed[i]
    if getmetatable(value) == blob.marker then
      if payload then
        error("a call may carry at most one binary payload", 2)
      end
      payload = value.data
      parameters[i] = { [blob.key] = true }
    elseif value == nil then
      parameters[i] = json_null
    elseif type(value) == "string" and not utf8.len(value) then
      -- Not text, so binary; small enough to go inline, as messages are limited to 4 KiB.
      parameters[i] = { [blob.bytesKey] = blob.encodeBase64(value) }
    else
      parameters[i] = value
    end
  end
  return parameters, payload
end

function blob.substitute(value, payload, depth)
  if type(value) ~= "table" then
    return value
  end

  depth = depth or 0
  if depth > maxDepth then
    error("host reply is nested too deeply", 0)
  end

  if value[blob.key] then
    return payload
  end
  if value[blob.bytesKey] then
    return blob.decodeBase64(value[blob.bytesKey])
  end

  for key, item in pairs(value) do
    value[key] = blob.substitute(item, payload, depth + 1)
  end
  return value
end

local Payload = {}
Payload.__index = Payload

function blob.fromFd(fd)
  return setmetatable({ fd = fd }, Payload)
end

function blob.open(path)
  local fd, status = fcntl.open(path, fcntl.O_RDWR | fcntl.O_CLOEXEC | fcntl.O_NONBLOCK)
  if not fd then
    return nil, status
  end
  return blob.fromFd(fd)
end

function Payload:close()
  if self.fd then
    unistd.close(self.fd)
    self.fd = nil -- closing twice would close whatever reused the number
  end
end

function Payload:reset()
  repeat
    local ready = poll.rpoll(self.fd, 0)
    if ready == 1 then
      local data = unistd.read(self.fd, chunkSize)
      if not data or #data == 0 then
        break
      end
    end
  until ready ~= 1
end

function Payload:write(data)
  if #data > blob.maxOutbound then
    error(string.format("binary payload of %d bytes exceeds the host limit of %d bytes",
                        #data, blob.maxOutbound), 2)
  end

  Channel.writeAll(self.fd, data)
end

function Payload:read(length, timeout)
  return Channel.readExactly(self.fd, length, timeout or readTimeout)
end

function blob.resolve(channel, result)
  local reference = result.blob
  if not reference then
    return blob.substitute(result.data)
  end

  if reference.length < 0 or reference.length > blob.maxInbound then
    error("host announced an implausible payload size: " .. tostring(reference.length), 0)
  end

  local data, reason = channel:read(reference.length)
  if not data then
    channel:reset()
    error("could not read binary payload: " .. tostring(reason), 0)
  end
  if blob.checksum(data) ~= reference.checksum then
    channel:reset()
    error("binary payload failed its checksum; the data channel is corrupt or out of sync", 0)
  end

  return blob.substitute(result.data, data)
end

return blob
