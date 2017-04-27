package states;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.kanban.Board;
import com.lightbend.lagom.serialization.CompressedJsonable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import java.util.Optional;

@Value
@Builder
@JsonDeserialize
@AllArgsConstructor
public class BoardStates implements CompressedJsonable {
    Optional<Board> board;
    String timestamp;
}
