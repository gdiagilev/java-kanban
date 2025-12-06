package ru.yandex.tracker.HttpServer;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.yandex.tracker.Model.Epic;
import ru.yandex.tracker.Service.TaskManager;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public class EpicHandler extends BaseHttpHandler implements HttpHandler {

    private final TaskManager manager;
    private final Gson gson;

    public EpicHandler(Gson gson, TaskManager manager) {
        this.manager = manager;
        this.gson = gson;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        try {
            switch (method) {
                case "POST":
                    String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                    Epic epic = gson.fromJson(body, Epic.class);

                    if (epic.getStartTime() == null) {
                        epic.setStartTime(LocalDateTime.now());
                    }
                    if (epic.getDuration() == null) {
                        epic.setDuration(Duration.ZERO);
                    }

                    manager.createEpicTask(epic);
                    sendText(exchange, gson.toJson(epic), 201); // 201 Created
                    break;
                case "GET":
                    List<Epic> epics = manager.getAllEpicTasks();
                    sendText(exchange, gson.toJson(epics), 200);
                    break;
                default:
                    sendError(exchange, "Метод не поддерживается", 405);
            }
        } catch (Exception e) {
            sendError(exchange, "Ошибка при обработке запроса: " + e.getMessage(), 500);
        }
    }

    private void sendText(HttpExchange exchange, String text, int code) throws IOException {
        byte[] resp = text.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        exchange.sendResponseHeaders(code, resp.length);
        exchange.getResponseBody().write(resp);
        exchange.close();
    }

    private void sendError(HttpExchange exchange, String message, int code) throws IOException {
        byte[] resp = message.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(code, resp.length);
        exchange.getResponseBody().write(resp);
        exchange.close();
    }

}