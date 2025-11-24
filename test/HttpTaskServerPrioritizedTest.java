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
public class HttpTaskServerPrioritizedTest {

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
    void testGetPrioritizedTasks() throws IOException, InterruptedException {
        Task t1 = new Task("T1", "Desc", Status.NEW,
                LocalDateTime.now(), Duration.ofMinutes(30));
        Task t2 = new Task("T2", "Desc", Status.NEW,
                LocalDateTime.now().plusHours(1), Duration.ofMinutes(30));

        manager.createTask(t1);
        manager.createTask(t2);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/prioritized");

        HttpResponse<String> response = client.send(
                HttpRequest.newBuilder().uri(url).GET().build(),
                HttpResponse.BodyHandlers.ofString()
        );

        assertEquals(200, response.statusCode());

        Task[] arr = gson.fromJson(response.body(), Task[].class);
        assertEquals(2, arr.length);
        assertEquals("T1", arr[0].getName());
    }
}