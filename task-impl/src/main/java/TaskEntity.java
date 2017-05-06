import akka.Done;
import com.lightbend.lagom.javadsl.persistence.PersistentEntity;
import commands.TaskCommand;
import events.TaskEvent;
import states.TaskStates;
import com.kanban.Task;
import java.util.concurrent.CompletionStage;

import java.time.LocalDateTime;
import java.util.Optional;

public class TaskEntity extends PersistentEntity<TaskCommand, TaskEvent, TaskStates> {

        @Override
        public Behavior initialBehavior(Optional<TaskStates> snapshotState) {

                // initial behaviour of task
                BehaviorBuilder behaviorBuilder = newBehaviorBuilder(TaskStates.builder().task(Optional.empty())
                                .timestamp(LocalDateTime.now().toString()).build());

                behaviorBuilder.setCommandHandler(TaskCommand.CreateTask.class, (cmd, ctx) -> {

                        Task addedTask = cmd.getTask();
                        String boardId = addedTask.getBoardId();

                        System.out.println("board id:");
                        System.out.println(boardId);
                        // TODO: Check if board with boardId exists

                        return ctx.thenPersist(TaskEvent.TaskCreated.builder().task(cmd.getTask())
                                                .entityId(entityId()).build(), evt -> ctx.reply(Done.getInstance()));
                });

                behaviorBuilder.setEventHandler(TaskEvent.TaskCreated.class, evt -> TaskStates.builder()
                                .task(Optional.of(evt.getTask())).timestamp(LocalDateTime.now().toString()).build());

                behaviorBuilder.setCommandHandler(TaskCommand.UpdateTask.class, (cmd, ctx) -> {
                        Task currentTask = state().getTask().get();
                        Task updatedTask = cmd.getTask();
                        if (currentTask != null && updatedTask != null) {
                                if (!updatedTask.getState().equals("BACKLOG")
                                                && !updatedTask.getState().equals("SCHEDULED")
                                                && !updatedTask.getState().equals("STARTED")
                                                && !updatedTask.getState().equals("COMPLETED")
                                                && !updatedTask.getState().equals("DELETED")
                                                && !updatedTask.getState().equals("ARCHIVED")) {
                                        //throw exception as the updated state is invalid  
                                        ctx.commandFailed(UpdateFailureException.INVALID_TASK_STATE);
                                }
                                if (updatedTask.getState().equals("ARCHIVED")
                                                && !currentTask.getState().equals("COMPLETED")) {
                                        ctx.commandFailed(UpdateFailureException.INVALID_TASK_STATE);
                                }
                        }

                        return ctx.thenPersist(TaskEvent.TaskUpdated.builder().task(cmd.getTask()).entityId(entityId())
                                        .build(), evt -> ctx.reply(Done.getInstance()));
                });

                behaviorBuilder.setEventHandler(TaskEvent.TaskUpdated.class, evt -> TaskStates.builder()
                                .task(Optional.of(evt.getTask())).timestamp(LocalDateTime.now().toString()).build());

                behaviorBuilder.setCommandHandler(TaskCommand.DeleteTask.class, (cmd, ctx) -> ctx.thenPersist(
                                TaskEvent.TaskDeleted.builder().task(cmd.getTask()).entityId(entityId()).build(),
                                evt -> ctx.reply(Done.getInstance())));

                behaviorBuilder.setEventHandler(TaskEvent.TaskDeleted.class, evt -> TaskStates.builder()
                                .task(Optional.empty()).timestamp(LocalDateTime.now().toString()).build());

                behaviorBuilder.setReadOnlyCommandHandler(TaskCommand.TaskCurrentState.class,
                                (cmd, ctx) -> ctx.reply(state().getTask()));

                return behaviorBuilder.build();
        }
}
