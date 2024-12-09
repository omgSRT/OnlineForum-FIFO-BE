package com.FA24SE088.OnlineForum.exception;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.listener.ExceptionListenerAdapter;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class CustomExceptionListener extends ExceptionListenerAdapter {
    @Override
    public void onEventException(Exception e, List<Object> data, SocketIOClient client) {
        StringBuilder errorMsg = new StringBuilder();

        errorMsg.append("An exception occurred while handling an event.\n");

        if (client != null) {
            errorMsg.append("Client Information:\n");
            errorMsg.append(" - Client ID: ").append(client.getSessionId()).append("\n");
            errorMsg.append(" - Client Address: ").append(client.getRemoteAddress()).append("\n");
        }

        if (data != null && !data.isEmpty()) {
            errorMsg.append("Event Data: ").append(data.toString()).append("\n");
        } else {
            errorMsg.append("No event data provided.\n");
        }

        errorMsg.append("Exception Message: ").append(e.getMessage()).append("\n");

        errorMsg.append("Stack Trace:\n");
        String stackTrace = String.join("\n", Arrays.stream(e.getStackTrace())
                .map(StackTraceElement::toString)
                .toArray(String[]::new));

        errorMsg.append(stackTrace).append("\n");

        System.err.println(errorMsg);
    }

    @Override
    public void onDisconnectException(Exception e, SocketIOClient client) {
        StringBuilder errorMsg = new StringBuilder();
        errorMsg.append("Disconnect exception occurred.\n");

        // Include client details
        if (client != null) {
            errorMsg.append("Client ID: ").append(client.getSessionId()).append("\n");
            errorMsg.append("Client Address: ").append(client.getRemoteAddress()).append("\n");
        }

        // Log the exception message and stack trace
        errorMsg.append("Exception Message: ").append(e.getMessage()).append("\n");
        errorMsg.append("Stack Trace: \n");
        for (StackTraceElement element : e.getStackTrace()) {
            errorMsg.append(element.toString()).append("\n");
        }

        System.err.println(errorMsg);
    }

    @Override
    public void onConnectException(Exception e, SocketIOClient client) {
        StringBuilder errorMsg = new StringBuilder();
        errorMsg.append("Connect exception occurred.\n");

        if (client != null) {
            errorMsg.append("Client ID: ").append(client.getSessionId()).append("\n");
            errorMsg.append("Client Address: ").append(client.getRemoteAddress()).append("\n");
        }

        errorMsg.append("Exception Message: ").append(e.getMessage()).append("\n");
        errorMsg.append("Stack Trace: \n");
        for (StackTraceElement element : e.getStackTrace()) {
            errorMsg.append(element.toString()).append("\n");
        }

        System.err.println(errorMsg.toString());
    }

    @Override
    public void onPingException(Exception e, SocketIOClient client) {
        StringBuilder errorMsg = new StringBuilder();
        errorMsg.append("Ping exception occurred.\n");

        if (client != null) {
            errorMsg.append("Client ID: ").append(client.getSessionId()).append("\n");
            errorMsg.append("Client Address: ").append(client.getRemoteAddress()).append("\n");
        }

        errorMsg.append("Exception Message: ").append(e.getMessage()).append("\n");
        errorMsg.append("Stack Trace: \n");
        for (StackTraceElement element : e.getStackTrace()) {
            errorMsg.append(element.toString()).append("\n");
        }

        System.err.println(errorMsg);
    }

    @Override
    public boolean exceptionCaught(ChannelHandlerContext ctx, Throwable e) throws Exception {
        StringBuilder errorMsg = new StringBuilder();
        errorMsg.append("Unhandled exception occurred in Channel Handler.\n");

        if (ctx != null) {
            errorMsg.append("Channel ID: ").append(ctx.channel().id().asShortText()).append("\n");
            errorMsg.append("Channel Remote Address: ").append(ctx.channel().remoteAddress()).append("\n");
        }

        errorMsg.append("Exception Message: ").append(e.getMessage()).append("\n");
        errorMsg.append("Stack Trace: \n");
        for (StackTraceElement element : e.getStackTrace()) {
            errorMsg.append(element.toString()).append("\n");
        }

        System.err.println(errorMsg);

        return true;
    }

    @Override
    public void onAuthException(Throwable throwable, SocketIOClient socketIOClient) {
        StringBuilder errorMsg = new StringBuilder();
        errorMsg.append("Authentication exception occurred.\n");

        if (socketIOClient != null) {
            errorMsg.append("Client ID: ").append(socketIOClient.getSessionId()).append("\n");
            errorMsg.append("Client Address: ").append(socketIOClient.getRemoteAddress()).append("\n");
        }

        errorMsg.append("Exception Message: ").append(throwable.getMessage()).append("\n");

        errorMsg.append("Stack Trace: \n");
        for (StackTraceElement element : throwable.getStackTrace()) {
            errorMsg.append(element.toString()).append("\n");
        }

        System.err.println(errorMsg.toString());
    }
}
