package events;

import akka.Done;
import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.PreparedStatement;
import com.lightbend.lagom.javadsl.persistence.AggregateEventTag;
import com.lightbend.lagom.javadsl.persistence.ReadSideProcessor;
import com.lightbend.lagom.javadsl.persistence.cassandra.CassandraReadSide;
import com.lightbend.lagom.javadsl.persistence.cassandra.CassandraSession;
import events.BoardEvent.*;
import org.pcollections.PSequence;
import org.pcollections.TreePVector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletionStage;


public class BoardEventProcessor extends ReadSideProcessor<BoardEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(BoardEventProcessor.class);

    private final CassandraSession session;
    private final CassandraReadSide readSide;

    private PreparedStatement writeBoards;
    private PreparedStatement deleteBoards;

    /**
     *
     * @param session
     * @param readSide
     */
    @Inject
    public BoardEventProcessor(final CassandraSession session, final CassandraReadSide readSide) {
        this.session = session;
        this.readSide = readSide;
    }

    /**
     *
     * @return
     */
    @Override
    public PSequence<AggregateEventTag<BoardEvent>> aggregateTags() {
        LOGGER.info(" aggregateTags method ... ");
        return TreePVector.singleton(BoardEventTag.INSTANCE);
    }

    /**
     *
     * @return
     */
    @Override
    public ReadSideHandler<BoardEvent> buildHandler() {
        LOGGER.info(" buildHandler method ... ");
        return readSide.<BoardEvent>builder("Boards_offset")
                .setGlobalPrepare(this::createTable)
                .setPrepare(evtTag -> prepareWriteBoard()
                        .thenCombine(prepareDeleteBoard(), (d1, d2) -> Done.getInstance())
                )
                .setEventHandler(BoardCreated.class, this::processPostAdded)
                .setEventHandler(BoardUpdated.class, this::processPostUpdated)
                .setEventHandler(BoardDeleted.class, this::processPostDeleted)
                .build();
    }

    /**
     *
     * @return
     */
    // Execute only once while application is start
    private CompletionStage<Done> createTable() {
        return session.executeCreateTable(
                "CREATE TABLE IF NOT EXISTS Boards ( " +
                        "id TEXT, name TEXT, state TEXT, PRIMARY KEY(id))"
        );
    }

    /*
    * START: Prepare statement for insert Board values into Boards table.
    * This is just creation of prepared statement, we will map this statement with our event
    */

    /**
     *
     * @return
     */
    private CompletionStage<Done> prepareWriteBoard() {
        return session.prepare(
                "INSERT INTO Boards (id, name, state) VALUES (?, ?, ?)"
        ).thenApply(ps -> {
            setWriteBoards(ps);
            return Done.getInstance();
        });
    }

    /**
     *
     * @param statement
     */
    private void setWriteBoards(PreparedStatement statement) {
        this.writeBoards = statement;
    }

    // Bind prepare statement while BoardCreate event is executed

    /**
     *
     * @param event
     * @return
     */
    private CompletionStage<List<BoundStatement>> processPostAdded(BoardCreated event) {
        BoundStatement bindWriteBoard = writeBoards.bind();
        bindWriteBoard.setString("id", event.getBoard().getId());
        bindWriteBoard.setString("name", event.getBoard().getName());
        bindWriteBoard.setString("state", event.getBoard().getState());
        return CassandraReadSide.completedStatements(Arrays.asList(bindWriteBoard));
    }
    /* ******************* END ****************************/

    /* START: Prepare statement for update the data in Boards table.
    * This is just creation of prepared statement, we will map this statement with our event
    */

    /**
     *
     * @param event
     * @return
     */
    private CompletionStage<List<BoundStatement>> processPostUpdated(BoardUpdated event) {
        BoundStatement bindWriteBoard = writeBoards.bind();
        bindWriteBoard.setString("id", event.getBoard().getId());
        bindWriteBoard.setString("name", event.getBoard().getName());
        bindWriteBoard.setString("state", event.getBoard().getState());
        return CassandraReadSide.completedStatements(Arrays.asList(bindWriteBoard));
    }
    /* ******************* END ****************************/

    /* START: Prepare statement for delete the the Board from table.
    * This is just creation of prepared statement, we will map this statement with our event
    */

    /**
     *
     * @return
     */
    private CompletionStage<Done> prepareDeleteBoard() {
        return session.prepare(
                "DELETE FROM Boards WHERE id=?"
        ).thenApply(ps -> {
            setDeleteBoards(ps);
            return Done.getInstance();
        });
    }

    /**
     *
     * @param deleteBoards
     */
    private void setDeleteBoards(PreparedStatement deleteBoards) {
        this.deleteBoards = deleteBoards;
    }

    /**
     *
     * @param event
     * @return
     */
    private CompletionStage<List<BoundStatement>> processPostDeleted(BoardDeleted event) {
        BoundStatement bindWriteBoard = deleteBoards.bind();
        bindWriteBoard.setString("id", event.getBoard().getId());
        return CassandraReadSide.completedStatements(Arrays.asList(bindWriteBoard));
    }
    /* ******************* END ****************************/
}
