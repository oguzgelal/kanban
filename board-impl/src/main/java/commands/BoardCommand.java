package commands;

import akka.Done;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.kanban.Board;
import com.lightbend.lagom.javadsl.persistence.PersistentEntity;
import com.lightbend.lagom.serialization.Jsonable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.util.Optional;

public interface BoardCommand extends Jsonable {

    @Value
    @Builder
    @JsonDeserialize
    @AllArgsConstructor
    final class CreateBoard implements BoardCommand, PersistentEntity.ReplyType<Done> {
        Board board;
    }

    @Value
    @Builder
    @JsonDeserialize
    @AllArgsConstructor
    final class UpdateBoard implements BoardCommand, PersistentEntity.ReplyType<Done> {
        Board board;
    }

    @Value
    @Builder
    @JsonDeserialize
    @AllArgsConstructor
    final class DeleteBoard implements BoardCommand, PersistentEntity.ReplyType<Done> {
        Board board;
    }

    @JsonDeserialize
    final class BoardCurrentState implements BoardCommand, PersistentEntity.ReplyType<Optional<Board>> {}
}
