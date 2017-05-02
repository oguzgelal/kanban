package com.kanban;

import akka.Done;
import akka.NotUsed;
import com.lightbend.lagom.javadsl.api.Descriptor;
import com.lightbend.lagom.javadsl.api.Service;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import org.pcollections.PSequence;

import java.util.Optional;

import static com.lightbend.lagom.javadsl.api.Service.named;
import static com.lightbend.lagom.javadsl.api.Service.restCall;
import static com.lightbend.lagom.javadsl.api.transport.Method.*;
import akka.stream.javadsl.Source;

public interface BoardService extends Service {
    /**
     * @param id
     * @return
     */
    ServiceCall<NotUsed, Optional<Board>> board(String id);

    /**
     * @return
     */
    ServiceCall<Board, Done> newBoard();

    /**
     * @param id
     * @return
     */
    ServiceCall<Board, Done> updateBoard(String id);

    /**
     * @param id
     * @return
     */
    ServiceCall<NotUsed, Done> deleteBoard(String id);

    /**
     * @param id
     * @return
     */
    //ServiceCall<NotUsed, Source<Board, ?>> getBoards();
    ServiceCall<NotUsed, PSequence<Board>> getBoards();

    /**
     * @return
     */
    @Override
    default Descriptor descriptor() {

        return named("board")
                .withCalls(restCall(GET, "/api/board/:id", this::board),
                        restCall(POST, "/api/board", this::newBoard),
                        restCall(PUT, "/api/board/:id", this::updateBoard),
                        restCall(DELETE, "/api/board/:id", this::deleteBoard),
                        restCall(GET, "/api/boards", this::getBoards))
                .withAutoAcl(true);
    }
}
