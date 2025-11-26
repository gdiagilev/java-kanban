import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.tracker.HttpServer.HttpTaskServer;
import ru.yandex.tracker.HttpServer.DurationAdapter;
import ru.yandex.tracker.HttpServer.LocalDateTimeAdapter;
import ru.yandex.tracker.Model.*;
import ru.yandex.tracker.Service.InMemoryTaskManager;
import ru.yandex.tracker.Service.TaskManager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HistoryHttpTest {

    private TaskManager manager;
    private Gson gson;
    private HttpClient client;
    private HttpTaskServer server;

    @BeforeEach
    void setup() throws Exception {
        manager = new InMemoryTaskManager();
        gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .registerTypeAdapter(Duration.class, new DurationAdapter())
                .create();
        client = HttpClient.newHttpClient();

        // Запуск сервера
        server = new HttpTaskServer(manager, gson);
        server.start();
    }

    @AfterEach
    void teardown() {
        server.stop();
    }

    @Test
    void testHistory() throws Exception {
        Task task = new Task("Task1", "Desc1", Status.NEW, LocalDateTime.now(), Duration.ofMinutes(10));
        manager.createTask(task);
        manager.getTask(task.getId());

        HttpRequest request = HttpRequest.newBuilder()
                .uri(new java.net.URI("http://localhost:8080/history"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        Task[] history = gson.fromJson(response.body(), Task[].class);

        assertEquals(1, history.length);
        assertEquals("Task1", history[0].getName());
    }
}
