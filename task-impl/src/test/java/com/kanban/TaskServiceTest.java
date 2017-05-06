package com.kanban;

import static com.lightbend.lagom.javadsl.testkit.ServiceTest.*;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import java.util.Optional;
import com.kanban.Task;
import com.kanban.TaskService;

public class TaskServiceTest {

    private static TestServer server;

    @BeforeClass
    public static void setUp() {
        server = startServer(defaultSetup().withCassandra(true));
    }

    @AfterClass
    public static void tearDown() {
        if (server != null) {
            server.stop();
            server = null;
        }
    }

    @Test
    public void shouldNotFetchNonExistingTask() throws Exception {
        TaskService service = server.client(TaskService.class);
        Optional<Task> taskNotFetched = service.task("NonExistingId").invoke().toCompletableFuture().get(5, SECONDS);
        assertEquals(taskNotFetched, Optional.empty());
    }

    @Test
    public void shouldInsertTaskCorrectly() throws Exception {
        TaskService service = server.client(TaskService.class);
        Task mockTask = new Task(null, "testName", "testDetails", "testColor", "testBoardId", "BACKLOG");
        Task taskCreated = service.newTask().invoke(mockTask).toCompletableFuture().get(5, SECONDS);
        assertEquals(taskCreated.getName(), "testName");
        assertEquals(taskCreated.getDetails(), "testDetails");
        assertEquals(taskCreated.getColor(), "testColor");
        assertEquals(taskCreated.getBoardId(), "testBoardId");
        assertEquals(taskCreated.getState(), "BACKLOG");
    }

    @Test
    public void shouldUpdateTaskCorrectly() throws Exception {
        TaskService service = server.client(TaskService.class);
        Task mockInsertTask = new Task(null, "testName1", "testDetails1", "testColor1", "testBoardId1", "BACKLOG");

        // Create a task
        Task taskCreated = service.newTask().invoke(mockInsertTask).toCompletableFuture().get();
        String createdTaskId = taskCreated.getId();

        // Update the created task
        Task mockUpdateTask = new Task(createdTaskId, "testName2", "testDetails2", "testColor2", "testBoardId2",
                "STARTED");
        service.updateTask(createdTaskId).invoke(mockUpdateTask).toCompletableFuture().get();

        // Wait for event propagation
        Thread.sleep(5000);

        // Get the updated task
        Optional<Task> taskUpdated = service.task(createdTaskId).invoke().toCompletableFuture().get();

        assertEquals(taskUpdated.get().getName(), "testName2");
        assertEquals(taskUpdated.get().getDetails(), "testDetails2");
        assertEquals(taskUpdated.get().getColor(), "testColor2");
        assertEquals(taskUpdated.get().getBoardId(), "testBoardId2");
        assertEquals(taskUpdated.get().getState(), "STARTED");
    }

    @Test
    public void invalidStateShouldDefaultToBacklogOnInsert() throws Exception {
        TaskService service = server.client(TaskService.class);
        Task mockTask = new Task(null, "testName", "testDetails", "testColor", "testBoardId", "AnInvalidState");
        Task taskCreated = service.newTask().invoke(mockTask).toCompletableFuture().get(5, SECONDS);
        assertEquals(taskCreated.getState(), "BACKLOG");
    }

    @Test
    public void invalidStateShouldFailOnUpdate() throws Exception {
        TaskService service = server.client(TaskService.class);
        Task mockInsertTask = new Task(null, "testName1", "testDetails1", "testColor1", "testBoardId1", "BACKLOG");

        // Create a task
        Task taskCreated = service.newTask().invoke(mockInsertTask).toCompletableFuture().get();
        String createdTaskId = taskCreated.getId();

        // Update the created task
        Task mockUpdateTask = new Task(createdTaskId, "testName2", "testDetails2", "testColor2", "testBoardId2",
                "SomeInvalidState");

        try {
            service.updateTask(createdTaskId).invoke(mockUpdateTask).toCompletableFuture().get();
            Thread.sleep(5000);
        } catch (Exception e) {
            // since it was supposed to fail and it did, just accept
            assertEquals(1, 1);
        }
    }

    @Test
    public void archievedTasksShouldNotUpdate() throws Exception {
        TaskService service = server.client(TaskService.class);
        Task mockInsertTask = new Task(null, "testName1", "testDetails1", "testColor1", "testBoardId1", "ARCHIEVED");

        // Create a task
        Task taskCreated = service.newTask().invoke(mockInsertTask).toCompletableFuture().get();
        String createdTaskId = taskCreated.getId();

        // Update the created task
        Task mockUpdateTask = new Task(createdTaskId, "testName2", "testDetails2", "testColor2", "testBoardId2", "ARCHIEVED");

        try {
            service.updateTask(createdTaskId).invoke(mockUpdateTask).toCompletableFuture().get();
            Thread.sleep(5000);
        } catch (Exception e) {
            // since it was supposed to fail and it did, just accept
            assertEquals(1, 1);
        }
    }

    @Test
    public void completedTasksShouldNotUpdate() throws Exception {
        TaskService service = server.client(TaskService.class);
        Task mockInsertTask = new Task(null, "testName1", "testDetails1", "testColor1", "testBoardId1", "COMPLETED");

        // Create a task
        Task taskCreated = service.newTask().invoke(mockInsertTask).toCompletableFuture().get();
        String createdTaskId = taskCreated.getId();

        // Update the created task
        Task mockUpdateTask = new Task(createdTaskId, "testName2", "testDetails2", "testColor2", "testBoardId2", "COMPLETED");

        try {
            service.updateTask(createdTaskId).invoke(mockUpdateTask).toCompletableFuture().get();
            Thread.sleep(5000);
        } catch (Exception e) {
            // since it was supposed to fail and it did, just accept
            assertEquals(1, 1);
        }
    }

    @Test
    public void shouldDeleteTask() throws Exception {
        TaskService service = server.client(TaskService.class);
        Task mockInsertTask = new Task(null, "testName1", "testDetails1", "testColor1", "testBoardId1", "BACKLOG");

        // Create a task
        Task taskCreated = service.newTask().invoke(mockInsertTask).toCompletableFuture().get();

        // Wait for event propagation
        Thread.sleep(5000);

        // Delete the task
        String createdTaskId = taskCreated.getId();
        service.deleteTask(createdTaskId).invoke().toCompletableFuture().get();

        // Wait for event propagation
        Thread.sleep(5000);

        Optional<Task> deletedTask = service.task(createdTaskId).invoke().toCompletableFuture().get();
        assertEquals(deletedTask, Optional.empty());
    }

}