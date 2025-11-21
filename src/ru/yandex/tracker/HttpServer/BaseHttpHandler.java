package ru.yandex.tracker.HttpServer;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class BaseHttpHandler {
    protected void sendText(HttpExchange h, String text) throws IOException {
        byte[] resp = text.getBytes(StandardCharsets.UTF_8);
        h.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        h.sendResponseHeaders(200, resp.length);
        h.getResponseBody().write(resp);
        h.close();
    }

    protected void sendCreated(HttpExchange h, String text) throws IOException {
        byte[] resp = text == null ? new byte[0] : text.getBytes(StandardCharsets.UTF_8);
        h.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        h.sendResponseHeaders(201, resp.length);
        if (resp.length > 0) h.getResponseBody().write(resp);
        h.close();
    }

    protected void sendNotFound(HttpExchange h) throws IOException {
        String msg = "{\"error\":\"Not Found\"}";
        byte[] resp = msg.getBytes(StandardCharsets.UTF_8);
        h.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        h.sendResponseHeaders(404, resp.length);
        h.getResponseBody().write(resp);
        h.close();
    }

    protected void sendHasInteractions(HttpExchange h, String message) throws IOException {
        // 406 Not Acceptable — конфликт по времени
        byte[] resp = message == null ? new byte[0] : message.getBytes(StandardCharsets.UTF_8);
        h.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        h.sendResponseHeaders(406, resp.length);
        if (resp.length > 0) h.getResponseBody().write(resp);
        h.close();
    }

    protected void sendServerError(HttpExchange h, String message) throws IOException {
        byte[] resp = message == null ? new byte[0] : message.getBytes(StandardCharsets.UTF_8);
        h.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        h.sendResponseHeaders(500, resp.length);
        if (resp.length > 0) h.getResponseBody().write(resp);
        h.close();
    }

    protected Integer getIdFromQuery(String rawQuery) {
        if (rawQuery == null || rawQuery.isBlank()) return null;
        // query format: id=123
        String[] parts = rawQuery.split("&");
        for (String p : parts) {
            String[] kv = p.split("=");
            if (kv.length == 2 && kv[0].equals("id")) {
                try {
                    return Integer.parseInt(kv[1]);
                } catch (NumberFormatException e) {
                    return null;
                }
            }
        }
        return null;
    }
}