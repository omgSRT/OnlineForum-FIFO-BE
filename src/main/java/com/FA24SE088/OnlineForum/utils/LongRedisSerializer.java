package com.FA24SE088.OnlineForum.utils;

import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

import java.nio.ByteBuffer;

public class LongRedisSerializer implements RedisSerializer<Long> {
    @Override
    public byte[] serialize(Long value) throws SerializationException {
        if (value == null) {
            return new byte[0];
        }
        return ByteBuffer.allocate(Long.BYTES).putLong(value).array();
    }

    @Override
    public Long deserialize(byte[] bytes) throws SerializationException {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        return buffer.getLong();
    }
}
