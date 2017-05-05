package com.kanban;

import static com.lightbend.lagom.javadsl.testkit.ServiceTest.*;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import java.util.Optional;

import com.kanban.Task;
import com.kanban.TaskService;

public class TaskServiceTest {

    @Test
    public void shouldCreateTask() throws Exception {
        withServer(defaultSetup().withCassandra(true), server -> {
            TaskService service = server.client(TaskService.class);

            Optional<Task> taskCreated = service.task("IdDoesNotExist").invoke().toCompletableFuture().get(5, SECONDS);
            assertEquals(taskCreated, Optional.empty());
        });
    }

}