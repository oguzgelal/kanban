import akka.Done;
import com.lightbend.lagom.javadsl.persistence.PersistentEntity;
import commands.TaskCommand;
import events.TaskEvent;
import states.TaskStates;

import java.time.LocalDateTime;
import java.util.Optional;

public class TaskEntity  extends PersistentEntity<TaskCommand, TaskEvent, TaskStates> {
    /**
     *
     * @param snapshotState
     * @return
     */
    @Override
    public Behavior initialBehavior(Optional<TaskStates> snapshotState) {

        // initial behaviour of task
        BehaviorBuilder behaviorBuilder = newBehaviorBuilder(
                TaskStates.builder().task(Optional.empty())
                        .timestamp(LocalDateTime.now().toString()).build()
        );

        behaviorBuilder.setCommandHandler(TaskCommand.CreateTask.class, (cmd, ctx) ->
                ctx.thenPersist(TaskEvent.TaskCreated.builder().task(cmd.getTask())
                        .entityId(entityId()).build(), evt -> ctx.reply(Done.getInstance()))
        );

        behaviorBuilder.setEventHandler(TaskEvent.TaskCreated.class, evt ->
                TaskStates.builder().task(Optional.of(evt.getTask()))
                        .timestamp(LocalDateTime.now().toString()).build()
        );

        behaviorBuilder.setCommandHandler(TaskCommand.UpdateTask.class, (cmd, ctx) ->
                ctx.thenPersist(TaskEvent.TaskUpdated.builder().task(cmd.getTask()).entityId(entityId()).build()
                        , evt -> ctx.reply(Done.getInstance()))
        );

        behaviorBuilder.setEventHandler(TaskEvent.TaskUpdated.class, evt ->
                TaskStates.builder().task(Optional.of(evt.getTask()))
                        .timestamp(LocalDateTime.now().toString()).build()
        );

        behaviorBuilder.setCommandHandler(TaskCommand.DeleteTask.class, (cmd, ctx) ->
                ctx.thenPersist(TaskEvent.TaskDeleted.builder().task(cmd.getTask()).entityId(entityId()).build(),
                        evt -> ctx.reply(Done.getInstance()))
        );

        behaviorBuilder.setEventHandler(TaskEvent.TaskDeleted.class, evt ->
                TaskStates.builder().task(Optional.empty())
                        .timestamp(LocalDateTime.now().toString()).build()
        );

        behaviorBuilder.setReadOnlyCommandHandler(TaskCommand.TaskCurrentState.class, (cmd, ctx) ->
                ctx.reply(state().getTask())
        );

        return behaviorBuilder.build();
    }
}
