package states;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.kanban.Task;
import com.lightbend.lagom.serialization.CompressedJsonable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import java.util.Optional;

@Value
@Builder
@JsonDeserialize
@AllArgsConstructor
public class TaskStates implements CompressedJsonable {
    Optional<Task> task;
    String timestamp;
}
