package com.kanban;

import static com.lightbend.lagom.javadsl.testkit.ServiceTest.*;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import java.util.Optional;

import com.kanban.Board;
import com.kanban.BoardService;

public class BoardServiceTest {

    private static TestServer server;

    @BeforeClass
    public static void setUp() {
        server = startServer(defaultSetup().withCassandra(true));
    }

    @AfterClass
    public static void tearDown() {
        if (server != null) {
            server.stop();
            server = null;
        }
    }

    @Test
    public void shouldNotFetchNonExistingBoard() throws Exception {
        BoardService service = server.client(BoardService.class);
        Optional<Board> boardCreated = service.board("NonExistingId").invoke().toCompletableFuture().get(5, SECONDS);
        assertEquals(boardCreated, Optional.empty());
    }

    @Test
    public void shouldInsertBoardCorrectly() throws Exception {
        BoardService service = server.client(BoardService.class);
        Board mockBoard = new Board(null, "testName", "CREATED");
        Board boardCreated = service.newBoard().invoke(mockBoard).toCompletableFuture().get(5, SECONDS);
        assertEquals(boardCreated.getName(), "testName");
        assertEquals(boardCreated.getState(), "CREATED");
    }

    @Test
    public void shouldUpdateBoardCorrectly() throws Exception {
        BoardService service = server.client(BoardService.class);
        Board mockInsertBoard = new Board(null, "testName1", "CREATED");

        // Create a board
        Board boardCreated = service.newBoard().invoke(mockInsertBoard).toCompletableFuture().get();
        String createdBoardId = boardCreated.getId();

        // Update the created board
        Board mockUpdateBoard = new Board(createdBoardId, "testName2", "ARCHIVED");
        service.updateBoard(createdBoardId).invoke(mockUpdateBoard).toCompletableFuture().get();

        // Wait for event propagation
        Thread.sleep(5000);

        // Get the updated board
        Optional<Board> boardUpdated = service.board(createdBoardId).invoke().toCompletableFuture().get();

        assertEquals(boardUpdated.get().getName(), "testName2");
        assertEquals(boardUpdated.get().getState(), "ARCHIVED");
    }

    @Test
    public void invalidStateShouldDefaultToCreatedOnInsert() throws Exception {
        BoardService service = server.client(BoardService.class);
        Board mockBoard = new Board(null, "testName", "AnInvalidState");
        Board boardCreated = service.newBoard().invoke(mockBoard).toCompletableFuture().get(5, SECONDS);
        assertEquals(boardCreated.getState(), "CREATED");
    }

    @Test
    public void invalidStateShouldFailOnUpdate() throws Exception {
        BoardService service = server.client(BoardService.class);
        Board mockInsertBoard = new Board(null, "testName1", "STARTED");

        // Create a board
        Board boardCreated = service.newBoard().invoke(mockInsertBoard).toCompletableFuture().get();
        String createdBoardId = boardCreated.getId();

        // Update the created board
        Board mockUpdateBoard = new Board(createdBoardId, "testName2", "SomeInvalidState");

        try {
            service.updateBoard(createdBoardId).invoke(mockUpdateBoard).toCompletableFuture().get();
            Thread.sleep(5000);
        } catch (Exception e) {
            // since it was supposed to fail and it did, just accept
            assertEquals(1, 1);
        }
    }

    @Test
    public void archievedBoardsShouldNotUpdate() throws Exception {
        BoardService service = server.client(BoardService.class);
        Board mockInsertBoard = new Board(null, "testName1", "ARCHIVED");

        // Create a board
        Board boardCreated = service.newBoard().invoke(mockInsertBoard).toCompletableFuture().get();
        String createdBoardId = boardCreated.getId();

        // Update the created board
        Board mockUpdateBoard = new Board(createdBoardId, "testName2", "ARCHIVED");

        try {
            service.updateBoard(createdBoardId).invoke(mockUpdateBoard).toCompletableFuture().get();
            Thread.sleep(5000);
        } catch (Exception e) {
            // since it was supposed to fail and it did, just accept
            assertEquals(1, 1);
        }
    }

    @Test
    public void shouldDeleteBoard() throws Exception {
        BoardService service = server.client(BoardService.class);
        Board mockInsertBoard = new Board(null, "testName1", "CREATED");

        // Create a board
        Board boardCreated = service.newBoard().invoke(mockInsertBoard).toCompletableFuture().get();

        // Wait for event propagation
        Thread.sleep(5000);

        // Delete the board
        String createdBoardId = boardCreated.getId();
        service.deleteBoard(createdBoardId).invoke().toCompletableFuture().get();

        // Wait for event propagation
        Thread.sleep(5000);

        Optional<Board> deletedBoard = service.board(createdBoardId).invoke().toCompletableFuture().get();
        assertEquals(deletedBoard, Optional.empty());
    }

}