import akka.Done;
import com.lightbend.lagom.javadsl.persistence.PersistentEntity;
import commands.BoardCommand;
import events.BoardEvent;
import states.BoardStates;

import java.time.LocalDateTime;
import java.util.Optional;

public class BoardEntity  extends PersistentEntity<BoardCommand, BoardEvent, BoardStates> {
    /**
     *
     * @param snapshotState
     * @return
     */
    @Override
    public Behavior initialBehavior(Optional<BoardStates> snapshotState) {

        // initial behaviour of board
        BehaviorBuilder behaviorBuilder = newBehaviorBuilder(
                BoardStates.builder().board(Optional.empty())
                        .timestamp(LocalDateTime.now().toString()).build()
        );

        behaviorBuilder.setCommandHandler(BoardCommand.CreateBoard.class, (cmd, ctx) ->
                ctx.thenPersist(BoardEvent.BoardCreated.builder().board(cmd.getBoard())
                        .entityId(entityId()).build(), evt -> ctx.reply(Done.getInstance()))
        );

        behaviorBuilder.setEventHandler(BoardEvent.BoardCreated.class, evt ->
                BoardStates.builder().board(Optional.of(evt.getBoard()))
                        .timestamp(LocalDateTime.now().toString()).build()
        );

        behaviorBuilder.setCommandHandler(BoardCommand.UpdateBoard.class, (cmd, ctx) ->
                ctx.thenPersist(BoardEvent.BoardUpdated.builder().board(cmd.getBoard()).entityId(entityId()).build()
                        , evt -> ctx.reply(Done.getInstance()))
        );

        behaviorBuilder.setEventHandler(BoardEvent.BoardUpdated.class, evt ->
                BoardStates.builder().board(Optional.of(evt.getBoard()))
                        .timestamp(LocalDateTime.now().toString()).build()
        );

        behaviorBuilder.setCommandHandler(BoardCommand.DeleteBoard.class, (cmd, ctx) ->
                ctx.thenPersist(BoardEvent.BoardDeleted.builder().board(cmd.getBoard()).entityId(entityId()).build(),
                        evt -> ctx.reply(Done.getInstance()))
        );

        behaviorBuilder.setEventHandler(BoardEvent.BoardDeleted.class, evt ->
                BoardStates.builder().board(Optional.empty())
                        .timestamp(LocalDateTime.now().toString()).build()
        );

        behaviorBuilder.setReadOnlyCommandHandler(BoardCommand.BoardCurrentState.class, (cmd, ctx) ->
                ctx.reply(state().getBoard())
        );

        return behaviorBuilder.build();
    }
}
