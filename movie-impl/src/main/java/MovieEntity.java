import akka.Done;
import com.lightbend.lagom.javadsl.persistence.PersistentEntity;
import commands.MovieCommand;
import events.MovieEvent;
import states.MovieStates;

import java.time.LocalDateTime;
import java.util.Optional;

public class MovieEntity  extends PersistentEntity<MovieCommand, MovieEvent, MovieStates> {
    /**
     *
     * @param snapshotState
     * @return
     */
    @Override
    public Behavior initialBehavior(Optional<MovieStates> snapshotState) {

        // initial behaviour of movie
        BehaviorBuilder behaviorBuilder = newBehaviorBuilder(
                MovieStates.builder().movie(Optional.empty())
                        .timestamp(LocalDateTime.now().toString()).build()
        );

        behaviorBuilder.setCommandHandler(MovieCommand.CreateMovie.class, (cmd, ctx) ->
                ctx.thenPersist(MovieEvent.MovieCreated.builder().movie(cmd.getMovie())
                        .entityId(entityId()).build(), evt -> ctx.reply(Done.getInstance()))
        );

        behaviorBuilder.setEventHandler(MovieEvent.MovieCreated.class, evt ->
                MovieStates.builder().movie(Optional.of(evt.getMovie()))
                        .timestamp(LocalDateTime.now().toString()).build()
        );

        behaviorBuilder.setCommandHandler(MovieCommand.UpdateMovie.class, (cmd, ctx) ->
                ctx.thenPersist(MovieEvent.MovieUpdated.builder().movie(cmd.getMovie()).entityId(entityId()).build()
                        , evt -> ctx.reply(Done.getInstance()))
        );

        behaviorBuilder.setEventHandler(MovieEvent.MovieUpdated.class, evt ->
                MovieStates.builder().movie(Optional.of(evt.getMovie()))
                        .timestamp(LocalDateTime.now().toString()).build()
        );

        behaviorBuilder.setCommandHandler(MovieCommand.DeleteMovie.class, (cmd, ctx) ->
                ctx.thenPersist(MovieEvent.MovieDeleted.builder().movie(cmd.getMovie()).entityId(entityId()).build(),
                        evt -> ctx.reply(Done.getInstance()))
        );

        behaviorBuilder.setEventHandler(MovieEvent.MovieDeleted.class, evt ->
                MovieStates.builder().movie(Optional.empty())
                        .timestamp(LocalDateTime.now().toString()).build()
        );

        behaviorBuilder.setReadOnlyCommandHandler(MovieCommand.MovieCurrentState.class, (cmd, ctx) ->
                ctx.reply(state().getMovie())
        );

        return behaviorBuilder.build();
    }
}
