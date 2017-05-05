package commands;

import akka.Done;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.kanban.Task;
import com.lightbend.lagom.javadsl.persistence.PersistentEntity;
import com.lightbend.lagom.serialization.Jsonable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.util.Optional;

public interface TaskCommand extends Jsonable {

    @Value
    @Builder
    @JsonDeserialize
    @AllArgsConstructor
    final class CreateTask implements TaskCommand, PersistentEntity.ReplyType<Done> {
        Task task;
    }

    @Value
    @Builder
    @JsonDeserialize
    @AllArgsConstructor
    final class UpdateTask implements TaskCommand, PersistentEntity.ReplyType<Done> {
        Task task;
    }

    @Value
    @Builder
    @JsonDeserialize
    @AllArgsConstructor
    final class DeleteTask implements TaskCommand, PersistentEntity.ReplyType<Done> {
        Task task;
    }

    @JsonDeserialize
    final class TaskCurrentState implements TaskCommand, PersistentEntity.ReplyType<Optional<Task>> {}
}
