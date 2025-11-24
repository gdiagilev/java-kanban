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

public class HttpTaskServerHistoryTest extends BaseHttpTest {

    @Test
    void testHistoryAfterAccessingTask() throws Exception {
        Task task = new Task("Task 1", "Desc", Status.NEW, LocalDateTime.now(), Duration.ofMinutes(10));
        manager.createTask(task);

        HttpClient client = HttpClient.newHttpClient();
        URI urlTask = URI.create("http://localhost:8080/tasks?id=" + task.getId());
        client.send(HttpRequest.newBuilder().uri(urlTask).GET().build(), HttpResponse.BodyHandlers.ofString());

        URI urlHistory = URI.create("http://localhost:8080/history");
        HttpRequest request = HttpRequest.newBuilder().uri(urlHistory).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        Task[] history = gson.fromJson(response.body(), Task[].class);
        assertEquals(1, history.length);
        assertEquals(task.getName(), history[0].getName());
    }
}