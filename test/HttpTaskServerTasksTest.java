import org.junit.jupiter.api.Test;
import ru.yandex.tracker.Model.Status;
import ru.yandex.tracker.Model.Task;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HttpTaskServerTasksTest extends BaseHttpTest {

    @Test
    void testAddTask() throws Exception {
        Task task = new Task("Test Task", "Desc", Status.NEW, LocalDateTime.now(), Duration.ofMinutes(10));
        String json = gson.toJson(task);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(json)).build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());

        List<Task> tasks = manager.getAllTasks();
        assertEquals(1, tasks.size());
        assertEquals("Test Task", tasks.get(0).getName());
    }

    @Test
    void testGetTaskById() throws Exception {
        Task task = new Task("Task 1", "Desc", Status.NEW, LocalDateTime.now(), Duration.ofMinutes(15));
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
    void testDeleteTaskById() throws Exception {
        Task task = new Task("Task 1", "Desc", Status.NEW, LocalDateTime.now(), Duration.ofMinutes(15));
        manager.createTask(task);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks?id=" + task.getId());
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertTrue(manager.getAllTasks().isEmpty());
    }
}