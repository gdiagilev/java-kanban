package ru.yandex.tracker.HttpServer;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;

public abstract class BaseHttpHandler {

    protected void sendText(HttpExchange exchange, String text) throws IOException {
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        byte[] bytes = text.getBytes();
        exchange.sendResponseHeaders(200, bytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
    }

    protected void sendCreated(HttpExchange exchange, String text) throws IOException {
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        byte[] bytes = text.getBytes();
        exchange.sendResponseHeaders(201, bytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
    }

    protected void sendNotFound(HttpExchange exchange) throws IOException {
        exchange.sendResponseHeaders(404, -1);
        exchange.close();
    }

    protected void sendServerError(HttpExchange exchange, String text) throws IOException {
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        byte[] bytes = text.getBytes();
        exchange.sendResponseHeaders(500, bytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
    }

    protected String readBody(HttpExchange exchange) throws IOException {
        return new String(exchange.getRequestBody().readAllBytes());
    }

    protected Integer getIdFromQuery(String query) {
        if (query == null || !query.contains("id=")) return null;
        try {
            return Integer.parseInt(query.split("=")[1]);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}