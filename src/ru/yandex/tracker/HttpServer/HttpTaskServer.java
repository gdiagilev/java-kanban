package ru.yandex.tracker.HttpServer;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpServer;
import ru.yandex.tracker.Service.TaskManager;

import java.io.IOException;
import java.net.InetSocketAddress;

public class HttpTaskServer {

    private final HttpServer server;
    private final TaskManager manager;
    private final Gson gson;

    public HttpTaskServer(TaskManager manager, Gson gson) throws IOException {
        this.manager = manager;
        this.gson = gson;

        server = HttpServer.create(new InetSocketAddress(8080), 0);

        // Регистрируем обработчики
        server.createContext("/tasks", new TaskHandler(gson, manager));
        server.createContext("/epics", new EpicHandler(gson, manager));
        server.createContext("/subtasks", new SubtaskHandler(gson, manager));
        server.createContext("/history", new HistoryHandler(gson, manager));
        server.createContext("/prioritized", new PrioritizedHandler(gson, manager));
    }

    public void start() {
        server.start();
        System.out.println("HTTP Task Server started at port 8080");
    }

    public void stop(int delay) {
        server.stop(delay);
        System.out.println("HTTP Task Server stopped");
    }
}