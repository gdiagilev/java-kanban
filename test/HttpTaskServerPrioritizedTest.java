package ru.yandex.tracker.HttpServer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.*;
import ru.yandex.tracker.Model.Status;
import ru.yandex.tracker.Model.Task;
import ru.yandex.tracker.Service.InMemoryTaskManager;
import ru.yandex.tracker.Service.TaskManager;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class HttpTaskServerPrioritizedTest {

    private HttpTaskServer server;
    private TaskManager manager;
    private Gson gson;

    @BeforeAll
    void init() throws IOException {
        manager = new InMemoryTaskManager();
        gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new ru.yandex.tracker.HttpServer.LocalDateTimeAdapter())
                .registerTypeAdapter(Duration.class, new ru.yandex.tracker.HttpServer.DurationAdapter())
                .serializeNulls()
                .create();
        server = new HttpTaskServer(manager, gson);
    }

    @BeforeEach
    void setUp() {
        manager.getAllTasks().forEach(t -> manager.deleteTask(t.getId()));
        server.start();
    }

    @AfterEach
    void tearDown() {
        server.stop(0);
    }

    @Test
    void testPrioritizedTasksOrder() throws IOException, InterruptedException {
        Task t1 = new Task("Task 1", "Desc 1", Status.NEW, LocalDateTime.now().plusMinutes(20), Duration.ofMinutes(10));
        Task t2 = new Task("Task 2", "Desc 2", Status.NEW, LocalDateTime.now().plusMinutes(5), Duration.ofMinutes(15));
        manager.createTask(t1);
        manager.createTask(t2);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/prioritized");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        Task[] tasks = gson.fromJson(response.body(), Task[].class);
        assertEquals("Task 2", tasks[0].getName());
        assertEquals("Task 1", tasks[1].getName());
    }
}