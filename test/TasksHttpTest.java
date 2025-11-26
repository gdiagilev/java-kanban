import org.junit.jupiter.api.Test;
import ru.yandex.tracker.Model.Status;
import ru.yandex.tracker.Model.Task;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TasksHttpTest extends BaseTestServer {

    private final HttpClient client = HttpClient.newHttpClient();

    @Test
    void testCreateAndGetTask() throws Exception {
        Task task = new Task("Task1", "Desc", Status.NEW, LocalDateTime.now(), Duration.ofMinutes(10));
        String json = gson.toJson(task);

        HttpRequest post = HttpRequest.newBuilder(URI.create("http://localhost:8080/tasks"))
                .POST(HttpRequest.BodyPublishers.ofString(json)).build();
        HttpResponse<String> postResp = client.send(post, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, postResp.statusCode());

        HttpRequest get = HttpRequest.newBuilder(URI.create("http://localhost:8080/tasks?id=1")).GET().build();
        HttpResponse<String> getResp = client.send(get, HttpResponse.BodyHandlers.ofString());
        Task result = gson.fromJson(getResp.body(), Task.class);
        assertEquals("Task1", result.getName());
    }

    @Test
    void testDeleteAllTasks() throws Exception {
        Task task = new Task("Task", "Desc", Status.NEW, LocalDateTime.now(), Duration.ofMinutes(10));
        manager.createTask(task);

        HttpRequest delete = HttpRequest.newBuilder(URI.create("http://localhost:8080/tasks")).DELETE().build();
        HttpResponse<String> delResp = client.send(delete, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, delResp.statusCode());
        assertTrue(manager.getAllTasks().isEmpty());
    }
}