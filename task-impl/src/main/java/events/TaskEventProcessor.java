package events;

import akka.Done;
import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.PreparedStatement;
import com.lightbend.lagom.javadsl.persistence.AggregateEventTag;
import com.lightbend.lagom.javadsl.persistence.ReadSideProcessor;
import com.lightbend.lagom.javadsl.persistence.cassandra.CassandraReadSide;
import com.lightbend.lagom.javadsl.persistence.cassandra.CassandraSession;
import events.TaskEvent.*;
import org.pcollections.PSequence;
import org.pcollections.TreePVector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletionStage;


public class TaskEventProcessor extends ReadSideProcessor<TaskEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(TaskEventProcessor.class);

    private final CassandraSession session;
    private final CassandraReadSide readSide;

    private PreparedStatement writeTasks;
    private PreparedStatement deleteTasks;

    /**
     *
     * @param session
     * @param readSide
     */
    @Inject
    public TaskEventProcessor(final CassandraSession session, final CassandraReadSide readSide) {
        this.session = session;
        this.readSide = readSide;
    }

    /**
     *
     * @return
     */
    @Override
    public PSequence<AggregateEventTag<TaskEvent>> aggregateTags() {
        LOGGER.info(" aggregateTags method ... ");
        return TreePVector.singleton(TaskEventTag.INSTANCE);
    }

    /**
     *
     * @return
     */
    @Override
    public ReadSideHandler<TaskEvent> buildHandler() {
        LOGGER.info(" buildHandler method ... ");
        return readSide.<TaskEvent>builder("Tasks_offset")
                .setGlobalPrepare(this::createTable)
                .setPrepare(evtTag -> prepareWriteTask()
                        .thenCombine(prepareDeleteTask(), (d1, d2) -> Done.getInstance())
                )
                .setEventHandler(TaskCreated.class, this::processPostAdded)
                .setEventHandler(TaskUpdated.class, this::processPostUpdated)
                .setEventHandler(TaskDeleted.class, this::processPostDeleted)
                .build();
    }

    /**
     *
     * @return
     */
    // Execute only once while application is start
    private CompletionStage<Done> createTable() {
        return session.executeCreateTable(
                "CREATE TABLE IF NOT EXISTS Tasks ( " +
                        "id TEXT, name TEXT, state TEXT, PRIMARY KEY(id))"
        );
    }

    /*
    * START: Prepare statement for insert Task values into Tasks table.
    * This is just creation of prepared statement, we will map this statement with our event
    */

    /**
     *
     * @return
     */
    private CompletionStage<Done> prepareWriteTask() {
        return session.prepare(
                "INSERT INTO Tasks (id, name, state) VALUES (?, ?, ?)"
        ).thenApply(ps -> {
            setWriteTasks(ps);
            return Done.getInstance();
        });
    }

    /**
     *
     * @param statement
     */
    private void setWriteTasks(PreparedStatement statement) {
        this.writeTasks = statement;
    }

    // Bind prepare statement while TaskCreate event is executed

    /**
     *
     * @param event
     * @return
     */
    private CompletionStage<List<BoundStatement>> processPostAdded(TaskCreated event) {
        BoundStatement bindWriteTask = writeTasks.bind();
        bindWriteTask.setString("id", event.getTask().getId());
        bindWriteTask.setString("name", event.getTask().getName());
        bindWriteTask.setString("state", event.getTask().getState());
        return CassandraReadSide.completedStatements(Arrays.asList(bindWriteTask));
    }
    /* ******************* END ****************************/

    /* START: Prepare statement for update the data in Tasks table.
    * This is just creation of prepared statement, we will map this statement with our event
    */

    /**
     *
     * @param event
     * @return
     */
    private CompletionStage<List<BoundStatement>> processPostUpdated(TaskUpdated event) {
        BoundStatement bindWriteTask = writeTasks.bind();
        bindWriteTask.setString("id", event.getTask().getId());
        bindWriteTask.setString("name", event.getTask().getName());
        bindWriteTask.setString("state", event.getTask().getState());
        return CassandraReadSide.completedStatements(Arrays.asList(bindWriteTask));
    }
    /* ******************* END ****************************/

    /* START: Prepare statement for delete the the Task from table.
    * This is just creation of prepared statement, we will map this statement with our event
    */

    /**
     *
     * @return
     */
    private CompletionStage<Done> prepareDeleteTask() {
        return session.prepare(
                "DELETE FROM Tasks WHERE id=?"
        ).thenApply(ps -> {
            setDeleteTasks(ps);
            return Done.getInstance();
        });
    }

    /**
     *
     * @param deleteTasks
     */
    private void setDeleteTasks(PreparedStatement deleteTasks) {
        this.deleteTasks = deleteTasks;
    }

    /**
     *
     * @param event
     * @return
     */
    private CompletionStage<List<BoundStatement>> processPostDeleted(TaskDeleted event) {
        BoundStatement bindWriteTask = deleteTasks.bind();
        bindWriteTask.setString("id", event.getTask().getId());
        return CassandraReadSide.completedStatements(Arrays.asList(bindWriteTask));
    }
    /* ******************* END ****************************/
}
