package ru.yandex.tracker.HttpServer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.*;
import ru.yandex.tracker.Model.Task;
import ru.yandex.tracker.Model.Status;
import ru.yandex.tracker.Service.InMemoryTaskManager;
import ru.yandex.tracker.Service.TaskManager;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;

class HttpTaskServerHistoryTest {

    private HttpTaskServer server;
    private TaskManager manager;
    private Gson gson;
    private HttpClient client;
    private final String baseUrl = "http://localhost:8080/history";

    @BeforeEach
    void setUp() throws IOException {
        manager = new InMemoryTaskManager();
        gson = new GsonBuilder().serializeNulls().create();
        server = new HttpTaskServer(8080, manager, gson);
        server.start();
        client = HttpClient.newHttpClient();
    }

    @AfterEach
    void tearDown() {
        server.stop(0);
    }

    @Test
    void testHistoryAfterAccessingTasks() throws IOException, InterruptedException {
        Task task = new Task("Task 1", "Desc", Status.NEW);
        manager.createTask(task);

        // Получаем задачу через API
        HttpRequest getTaskRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks?id=1"))
                .GET()
                .build();
        client.send(getTaskRequest, HttpResponse.BodyHandlers.ofString());

        // GET /history
        HttpRequest historyRequest = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl))
                .GET()
                .build();
        HttpResponse<String> historyResponse = client.send(historyRequest, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, historyResponse.statusCode());
        assertTrue(historyResponse.body().contains("Task 1"));
    }
}