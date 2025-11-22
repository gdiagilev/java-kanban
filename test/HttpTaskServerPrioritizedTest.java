package ru.yandex.tracker.HttpServer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.*;
import ru.yandex.tracker.Model.Task;
import ru.yandex.tracker.Model.Status;
import ru.yandex.tracker.Service.InMemoryTaskManager;
import ru.yandex.tracker.Service.TaskManager;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HttpTaskServerPrioritizedTest {

    private HttpTaskServer server;
    private TaskManager manager;
    private Gson gson;
    private HttpClient client;
    private final String baseUrl = "http://localhost:8080/prioritized";

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
    void testPrioritizedTasksOrder() throws IOException, InterruptedException {
        Task t1 = new Task("Task 1", "Desc", Status.NEW, LocalDateTime.now(), Duration.ofMinutes(30));
        Task t2 = new Task("Task 2", "Desc", Status.NEW, LocalDateTime.now().plusMinutes(40), Duration.ofMinutes(20));
        manager.createTask(t2);
        manager.createTask(t1);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertTrue(response.body().indexOf("Task 1") < response.body().indexOf("Task 2"), "Задачи должны быть отсортированы по startTime");
    }
}