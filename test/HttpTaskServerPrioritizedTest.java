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

public class HttpTaskServerPrioritizedTest extends BaseHttpTest {

    @Test
    void testGetPrioritizedTasks() throws Exception {
        Task t1 = new Task("Task 1", "Desc", Status.NEW, LocalDateTime.now(), Duration.ofMinutes(30));
        Task t2 = new Task("Task 2", "Desc", Status.NEW, LocalDateTime.now().plusHours(1), Duration.ofMinutes(30));

        manager.createTask(t1);
        manager.createTask(t2);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/prioritized");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        Task[] prioritized = gson.fromJson(response.body(), Task[].class);
        assertEquals(2, prioritized.length);
        assertEquals(t1.getId(), prioritized[0].getId());
        assertEquals(t2.getId(), prioritized[1].getId());
    }
}