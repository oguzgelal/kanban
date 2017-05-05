package com.kanban;

import static com.lightbend.lagom.javadsl.testkit.ServiceTest.*;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import java.util.Optional;

import com.kanban.Board;
import com.kanban.BoardService;

public class BoardServiceTest {

    @Test
    public void shouldCreateTask() throws Exception {
        withServer(defaultSetup().withCassandra(true), server -> {
            BoardService service = server.client(BoardService.class);

            Optional<Board> boardCreated = service.board("IdDoesNotExist").invoke().toCompletableFuture().get(5, SECONDS);
            assertEquals(boardCreated, Optional.empty());
        });
    }

}