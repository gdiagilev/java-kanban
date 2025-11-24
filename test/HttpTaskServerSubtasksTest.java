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
public class HttpTaskServerSubtasksTest {

    private HttpTaskServer server;
    private InMemoryTaskManager manager;
    private Gson gson;
    private Epic epic;

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
        manager.getAllEpicTasks().forEach(e -> manager.deleteEpicTask(e.getId()));
        manager.getAllSubtasks().forEach(s -> manager.deleteSubtask(s.getId()));
        epic = new Epic("Epic", "Desc");
        manager.createEpicTask(epic);
        server.start();
    }

    @AfterEach
    void tearDown() {
        server.stop(0);
    }

    @Test
    void testCreateSubtask() throws IOException, InterruptedException {
        Subtask sub = new Subtask(epic.getId(), "S1", "D", Status.NEW,
                LocalDateTime.now(), Duration.ofMinutes(10));

        String json = gson.toJson(sub);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(json)).build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());

        assertEquals(1, manager.getAllSubtasks().size());
    }

    @Test
    void testGetSubtaskById() throws IOException, InterruptedException {
        Subtask sub = new Subtask(epic.getId(), "S1", "D", Status.NEW,
                LocalDateTime.now(), Duration.ofMinutes(10));
        manager.createSubtask(sub);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks?id=" + sub.getId());

        HttpResponse<String> response = client.send(
                HttpRequest.newBuilder().uri(url).GET().build(),
                HttpResponse.BodyHandlers.ofString()
        );

        assertEquals(200, response.statusCode());
        Subtask returned = gson.fromJson(response.body(), Subtask.class);

        assertEquals(sub.getName(), returned.getName());
    }

    @Test
    void testGetEpicSubtasks() throws IOException, InterruptedException {
        Subtask s1 = new Subtask(epic.getId(), "S1", "D", Status.NEW,
                LocalDateTime.now(), Duration.ofMinutes(10));
        Subtask s2 = new Subtask(epic.getId(), "S2", "D", Status.NEW,
                LocalDateTime.now().plusMinutes(15), Duration.ofMinutes(10));

        manager.createSubtask(s1);
        manager.createSubtask(s2);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks/epic?id=" + epic.getId());

        HttpResponse<String> response = client.send(
                HttpRequest.newBuilder().uri(url).GET().build(),
                HttpResponse.BodyHandlers.ofString()
        );

        assertEquals(200, response.statusCode());

        Subtask[] arr = gson.fromJson(response.body(), Subtask[].class);
        assertEquals(2, arr.length);
    }
}
