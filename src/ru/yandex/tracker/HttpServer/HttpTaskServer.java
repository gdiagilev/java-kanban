package ru.yandex.tracker.HttpServer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpServer;
import ru.yandex.tracker.Service.TaskManager;

import java.io.IOException;
import java.net.InetSocketAddress;

public class HttpTaskServer {

    private static final int PORT = 8080;
    private final HttpServer server;
    private final Gson gson;

    public HttpTaskServer(TaskManager manager) throws IOException {
        gson = new GsonBuilder().serializeNulls().create();
        server = HttpServer.create(new InetSocketAddress(PORT), 0);

        // создаём обработчики с переданным менеджером
        server.createContext("/tasks", new TaskHandler(manager, gson));
        server.createContext("/epics", new EpicHandler(manager, gson));
        server.createContext("/subtasks", new SubtaskHandler(manager, gson));
        server.createContext("/history", new HistoryHandler(manager, gson));
        server.createContext("/prioritized", new PrioritizedHandler(manager, gson));
    }

    public void start() {
        server.start();
        System.out.println("HTTP Task Server started on port " + PORT);
    }

    public void stop(int delaySeconds) {
        server.stop(delaySeconds);
        System.out.println("HTTP Task Server stopped");
    }

    public static Gson getGson() {
        return new GsonBuilder().serializeNulls().create();
    }

    // main для обычного запуска
    public static void main(String[] args) throws IOException {
        // используем реальный менеджер из Managers
        TaskManager defaultManager = ru.yandex.tracker.Service.Managers.getDefault();
        HttpTaskServer httpServer = new HttpTaskServer(defaultManager);
        httpServer.start();
    }
}