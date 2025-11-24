import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.*;
import ru.yandex.tracker.HttpServer.HttpTaskServer;
import ru.yandex.tracker.Model.*;
import ru.yandex.tracker.Service.InMemoryTaskManager;

import java.io.IOException;
import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class HttpTaskServerTasksTest {

    private HttpTaskServer server;
    private InMemoryTaskManager manager;
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
    void testCreateTask() throws IOException, InterruptedException {
        Task task = new Task("Task 1", "Desc", Status.NEW,
                LocalDateTime.now(), Duration.ofMinutes(20));

        String json = gson.toJson(task);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());

        assertEquals(1, manager.getAllTasks().size());
    }

    @Test
    void testUpdateTask() throws IOException, InterruptedException {
        Task task = new Task("Task 1", "Desc", Status.NEW,
                LocalDateTime.now(), Duration.ofMinutes(20));
        manager.createTask(task);

        task.setStatus(Status.IN_PROGRESS);
        String json = gson.toJson(task);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());

        Task updated = manager.getTask(task.getId());
        assertEquals(Status.IN_PROGRESS, updated.getStatus());
    }

    @Test
    void testGetTaskById() throws IOException, InterruptedException {
        Task task = new Task("Task 1", "Desc", Status.NEW,
                LocalDateTime.now(), Duration.ofMinutes(20));
        manager.createTask(task);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks?id=" + task.getId());
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        Task returned = gson.fromJson(response.body(), Task.class);
        assertEquals(task.getName(), returned.getName());
    }

    @Test
    void testDeleteTaskById() throws IOException, InterruptedException {
        Task task = new Task("Task 1", "Desc", Status.NEW,
                LocalDateTime.now(), Duration.ofMinutes(10));
        manager.createTask(task);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks?id=" + task.getId());
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertEquals(0, manager.getAllTasks().size());
    }
}