package com.FA24SE088.OnlineForum.handler;

import com.corundumstudio.socketio.AckCallback;
import com.corundumstudio.socketio.protocol.AckArgs;
import com.corundumstudio.socketio.protocol.JsonSupport;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public class CustomJsonSupportHandler implements JsonSupport {
    private final ObjectMapper objectMapper;

    public CustomJsonSupportHandler() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Override
    public AckArgs readAckArgs(ByteBufInputStream byteBufInputStream, AckCallback<?> ackCallback) throws IOException {
        return objectMapper.readValue((InputStream) byteBufInputStream, AckArgs.class);
    }

    @Override
    public <T> T readValue(String s, ByteBufInputStream byteBufInputStream, Class<T> aClass) throws IOException {
        return objectMapper.readValue((InputStream) byteBufInputStream, aClass);
    }

    @Override
    public void writeValue(ByteBufOutputStream byteBufOutputStream, Object o) throws IOException {
        objectMapper.writeValue((OutputStream) byteBufOutputStream, o);
    }

    @Override
    public void addEventMapping(String namespace, String eventName, Class<?>... eventClass) {
    }

    @Override
    public void removeEventMapping(String namespace, String eventName) {
    }

    @Override
    public List<byte[]> getArrays() {
        return List.of();
    }
}
