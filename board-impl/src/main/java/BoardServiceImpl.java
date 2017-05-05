import akka.Done;
import akka.NotUsed;
import com.kanban.Board;
import com.kanban.BoardService;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.persistence.PersistentEntityRef;
import com.lightbend.lagom.javadsl.persistence.PersistentEntityRegistry;
import com.lightbend.lagom.javadsl.persistence.ReadSide;
import com.lightbend.lagom.javadsl.persistence.cassandra.CassandraSession;
import commands.BoardCommand;
import events.BoardEventProcessor;
import akka.stream.javadsl.Source;
import akka.stream.Materializer;
import akka.stream.javadsl.Sink;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.Row;

import javax.inject.Inject;
import java.util.Optional;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import org.pcollections.PSequence;
import org.pcollections.TreePVector;
import java.util.stream.*;
import java.util.stream.Collectors;

public class BoardServiceImpl implements BoardService {

    private final PersistentEntityRegistry persistentEntityRegistry;
    private final CassandraSession session;
    private final Materializer mat;
    private static final String SELECT_NON_ARCHIVED_BOARDS =
            "SELECT * FROM boards WHERE STATE='CREATED' ALLOW FILTERING";

    /**
     * @param registry
     * @param readSide
     * @param session
     */
    @Inject
    public BoardServiceImpl(final PersistentEntityRegistry registry, ReadSide readSide, CassandraSession session,
            Materializer mat) {
        this.persistentEntityRegistry = registry;
        this.session = session;
        this.mat = mat;

        persistentEntityRegistry.register(BoardEntity.class);
        readSide.register(BoardEventProcessor.class);
    }

    /**
     * @param id
     * @return
     */
    @Override
    public ServiceCall<NotUsed, Optional<Board>> board(String id) {
        return request -> {
            CompletionStage<Optional<Board>> boardFuture = session.selectAll("SELECT * FROM boards WHERE id = ?", id)
                    .thenApply(
                            rows -> rows
                                    .stream().map(row -> Board.builder().id(row.getString("id"))
                                            .name(row.getString("name")).state(row.getString("state")).build())
                                    .findFirst());
            return boardFuture;
        };
    }

    @Override
    public ServiceCall<NotUsed, Source<Board, ?>> getBoards() {
        System.out.println("Finding the boards");
        return req -> {
            Source<Board, ?> result = fetchBoards();
            return CompletableFuture.completedFuture(result);
        };
    }

    private Source<Board, NotUsed> fetchBoards() {
        
        return session.select(SELECT_NON_ARCHIVED_BOARDS)
                .map(this::mapBoard);
    }
    private Board mapBoard(Row row) {
        System.out.println("Maping board");
        return new Board(
                row.getString("id"),
                row.getString("name"),                
                row.getString("state")
        );
    }


    /*
    @Override
    public ServiceCall<NotUsed, Source<Board, ?>> getBoards() {
        return request -> CompletableFuture.completedFuture(session.select("SELECT * FROM boards")
                .map(row -> new Board(row.getString("id"), row.getString("name"), row.getString("state"))));
        
        return request -> {
        Source<Board, ?> x = session.select("SELECT * FROM boards")
            .map(row -> new Board(row.getString("id"), row.getString("name"), row.getString("state")));
        };
        
    }
    */

    /**
     * @return
     */
    @Override
    public ServiceCall<Board, Board> newBoard() {
        return board -> {
            Board newBoard = new Board(Long.toString(System.currentTimeMillis()),board.getName(),"CREATED");
            PersistentEntityRef<BoardCommand> ref = boardEntityRef(newBoard);
            return ref.ask(BoardCommand.CreateBoard.builder().board(newBoard).build()).thenApply(result -> newBoard);
        };
    }

    /**
     * @param id
     * @return
     */
    @Override
    public ServiceCall<Board, Done> updateBoard(String id) {
        return board -> {
            PersistentEntityRef<BoardCommand> ref = boardEntityRef(board);
            return ref.ask(BoardCommand.UpdateBoard.builder().board(board).build());
        };
    }

    /**
     * @param id
     * @return
     */
    @Override
    public ServiceCall<NotUsed, Done> deleteBoard(String id) {
        return request -> {
            Board board = Board.builder().id(id).build();
            PersistentEntityRef<BoardCommand> ref = boardEntityRef(board);
            return ref.ask(BoardCommand.DeleteBoard.builder().board(board).build());
        };
    }

    /**
     * @param board
     * @return
     */
    private PersistentEntityRef<BoardCommand> boardEntityRef(Board board) {
        return persistentEntityRegistry.refFor(BoardEntity.class, board.getId());
    }
}
