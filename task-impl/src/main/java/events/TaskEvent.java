package events;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.kanban.Task;
import com.lightbend.lagom.javadsl.persistence.AggregateEvent;
import com.lightbend.lagom.javadsl.persistence.AggregateEventTagger;
import com.lightbend.lagom.serialization.CompressedJsonable;
import com.lightbend.lagom.serialization.Jsonable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

public interface TaskEvent extends Jsonable, AggregateEvent<TaskEvent> {

    @Override
    default AggregateEventTagger<TaskEvent> aggregateTag() {
        return TaskEventTag.INSTANCE;
    }

    @Value
    @Builder
    @JsonDeserialize
    @AllArgsConstructor
    final class TaskCreated implements TaskEvent, CompressedJsonable {
        Task task;
        String entityId;
    }

    @Value
    @Builder
    @JsonDeserialize
    @AllArgsConstructor
    final class TaskUpdated implements TaskEvent, CompressedJsonable {
        Task task;
        String entityId;
    }

    @Value
    @Builder
    @JsonDeserialize
    @AllArgsConstructor
    final class TaskDeleted implements TaskEvent, CompressedJsonable {
        Task task;
        String entityId;
    }
}
