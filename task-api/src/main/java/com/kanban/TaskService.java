package com.kanban;

import akka.Done;
import akka.NotUsed;
import com.lightbend.lagom.javadsl.api.Descriptor;
import com.lightbend.lagom.javadsl.api.Service;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import org.pcollections.PSequence;

import java.util.Optional;

import static com.lightbend.lagom.javadsl.api.Service.named;
import static com.lightbend.lagom.javadsl.api.Service.restCall;
import static com.lightbend.lagom.javadsl.api.transport.Method.*;
import akka.stream.javadsl.Source;

public interface TaskService extends Service {
    /**
     * @param id
     * @return
     */
    ServiceCall<NotUsed, Optional<Task>> task(String id);

    /**
     * @return
     */
    ServiceCall<Task, Done> newTask();

    /**
     * @param id
     * @return
     */
    ServiceCall<Task, Done> updateTask(String id);

    /**
     * @param id
     * @return
     */
    ServiceCall<NotUsed, Done> deleteTask(String id);

    /**
     * @param id
     * @return
     */
    //ServiceCall<NotUsed, Source<Task, ?>> getTasks();
    ServiceCall<NotUsed, Source<Task, ?>> getTasks();

    /**
     * @return
     */
    @Override
    default Descriptor descriptor() {

        return named("task")
                .withCalls(restCall(GET, "/api/task/:id", this::task),
                        restCall(POST, "/api/task", this::newTask),
                        restCall(PUT, "/api/task/:id", this::updateTask),
                        restCall(DELETE, "/api/task/:id", this::deleteTask),
                        restCall(GET, "/api/tasks", this::getTasks))
                .withAutoAcl(true);
    }
}
