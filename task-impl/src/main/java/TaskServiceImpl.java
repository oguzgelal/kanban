import akka.Done;
import akka.NotUsed;
import com.kanban.Task;
import com.kanban.TaskService;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.persistence.PersistentEntityRef;
import com.lightbend.lagom.javadsl.persistence.PersistentEntityRegistry;
import com.lightbend.lagom.javadsl.persistence.ReadSide;
import com.lightbend.lagom.javadsl.persistence.cassandra.CassandraSession;
import commands.TaskCommand;
import events.TaskEventProcessor;
import akka.stream.javadsl.Source;
import akka.stream.Materializer;
import akka.stream.javadsl.Sink;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.Row;

import javax.inject.Inject;
import java.util.Optional;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import org.pcollections.PSequence;
import org.pcollections.TreePVector;
import java.util.stream.*;
import java.util.stream.Collectors;

public class TaskServiceImpl implements TaskService {

    private final PersistentEntityRegistry persistentEntityRegistry;
    private final CassandraSession session;
    private final Materializer mat;
    private static final String SELECT_NON_ARCHIVED_BOARDS =
            "SELECT * FROM tasks ALLOW FILTERING";

    /**
     * @param registry
     * @param readSide
     * @param session
     */
    @Inject
    public TaskServiceImpl(final PersistentEntityRegistry registry, ReadSide readSide, CassandraSession session,
            Materializer mat) {
        this.persistentEntityRegistry = registry;
        this.session = session;
        this.mat = mat;

        persistentEntityRegistry.register(TaskEntity.class);
        readSide.register(TaskEventProcessor.class);
    }

    /**
     * @param id
     * @return
     */
    @Override
    public ServiceCall<NotUsed, Optional<Task>> task(String id) {
        return request -> {
            CompletionStage<Optional<Task>> taskFuture = session.selectAll("SELECT * FROM tasks WHERE id = ?", id)
                    .thenApply(
                            rows -> rows
                                    .stream().map(row -> Task.builder().id(row.getString("id"))
                                            .name(row.getString("name")).state(row.getString("state")).build())
                                    .findFirst());
            return taskFuture;
        };
    }

    @Override
    public ServiceCall<NotUsed, Source<Task, ?>> getTasks() {
        System.out.println("Finding the tasks");
        return req -> {
            Source<Task, ?> result = fetchTasks();
            return CompletableFuture.completedFuture(result);
        };
    }

    private Source<Task, NotUsed> fetchTasks() {
        
        return session.select(SELECT_NON_ARCHIVED_BOARDS)
                .map(this::mapTask);
    }
    private Task mapTask(Row row) {
        System.out.println("Maping task");
        return new Task(
                row.getString("id"),
                row.getString("name"),
                row.getString("details"),
                row.getString("color"),
                row.getString("boardId"),
                row.getString("state")      
        );
    }


    /*
    @Override
    public ServiceCall<NotUsed, Source<Task, ?>> getTasks() {
        return request -> CompletableFuture.completedFuture(session.select("SELECT * FROM tasks")
                .map(row -> new Task(row.getString("id"), row.getString("name"), row.getString("state"))));
        
        return request -> {
        Source<Task, ?> x = session.select("SELECT * FROM tasks")
            .map(row -> new Task(row.getString("id"), row.getString("name"), row.getString("state")));
        };
        
    }
    */

    /**
     * @return
     */
    @Override
    public ServiceCall<Task, Task> newTask() {
        return task -> {
            Task newTask = new Task(Long.toString(System.currentTimeMillis()),task.getName(),task.getDetails(),task.getColor(),task.getBoardId(),"BACKLOG");
            PersistentEntityRef<TaskCommand> ref = taskEntityRef(newTask);
            return ref.ask(TaskCommand.CreateTask.builder().task(newTask).build()).thenApply(result -> newTask);
        };
    }

    /**
     * @param id
     * @return
     */
    @Override
    public ServiceCall<Task, Done> updateTask(String id) {
        return task -> {
            PersistentEntityRef<TaskCommand> ref = taskEntityRef(task);
            return ref.ask(TaskCommand.UpdateTask.builder().task(task).build());
        };
    }

    /**
     * @param id
     * @return
     */
    @Override
    public ServiceCall<NotUsed, Done> deleteTask(String id) {
        return request -> {
            Task task = Task.builder().id(id).build();
            PersistentEntityRef<TaskCommand> ref = taskEntityRef(task);
            return ref.ask(TaskCommand.DeleteTask.builder().task(task).build());
        };
    }

    /**
     * @param task
     * @return
     */
    private PersistentEntityRef<TaskCommand> taskEntityRef(Task task) {
        return persistentEntityRegistry.refFor(TaskEntity.class, task.getId());
    }
}
