import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import ru.yandex.tracker.HttpServer.DurationAdapter;
import ru.yandex.tracker.HttpServer.HttpTaskServer;
import ru.yandex.tracker.HttpServer.LocalDateTimeAdapter;
import ru.yandex.tracker.Service.InMemoryTaskManager;

import java.io.IOException;

public abstract class BaseHttpTest {
    protected InMemoryTaskManager manager;
    protected HttpTaskServer server;
    protected Gson gson;

    @BeforeEach
    void setUp() throws IOException {
        manager = new InMemoryTaskManager();
        gson = new GsonBuilder()
                .registerTypeAdapter(java.time.LocalDateTime.class, new LocalDateTimeAdapter())
                .registerTypeAdapter(java.time.Duration.class, new DurationAdapter())
                .serializeNulls()
                .create();
        server = new HttpTaskServer(manager, gson);
        server.start();
    }

    @AfterEach
    void tearDown() {
        server.stop(0);
    }
}