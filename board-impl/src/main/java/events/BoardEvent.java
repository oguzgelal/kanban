package events;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.kanban.Board;
import com.lightbend.lagom.javadsl.persistence.AggregateEvent;
import com.lightbend.lagom.javadsl.persistence.AggregateEventTagger;
import com.lightbend.lagom.serialization.CompressedJsonable;
import com.lightbend.lagom.serialization.Jsonable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

public interface BoardEvent extends Jsonable, AggregateEvent<BoardEvent> {

    @Override
    default AggregateEventTagger<BoardEvent> aggregateTag() {
        return BoardEventTag.INSTANCE;
    }

    @Value
    @Builder
    @JsonDeserialize
    @AllArgsConstructor
    final class BoardCreated implements BoardEvent, CompressedJsonable {
        Board board;
        String entityId;
    }

    @Value
    @Builder
    @JsonDeserialize
    @AllArgsConstructor
    final class BoardUpdated implements BoardEvent, CompressedJsonable {
        Board board;
        String entityId;
    }

    @Value
    @Builder
    @JsonDeserialize
    @AllArgsConstructor
    final class BoardDeleted implements BoardEvent, CompressedJsonable {
        Board board;
        String entityId;
    }
}
