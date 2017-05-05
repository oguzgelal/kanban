

import com.fasterxml.jackson.annotation.JsonCreator;
import com.lightbend.lagom.serialization.Jsonable;

public class UpdateFailureException extends RuntimeException implements Jsonable {

    public static final UpdateFailureException INVALID_TASK_STATE
            = new UpdateFailureException("Invalid state submited for the task.");

    public static final UpdateFailureException FORBIDDEN_ARCHIVE_UPDATE
            = new UpdateFailureException("Can't set a task as archived if it hasn't been completed.");

    private String message;

    @JsonCreator
    public UpdateFailureException(String message) {
        super(message);
        this.message = message;
    }


    // ------------------------------------------------------------

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UpdateFailureException that = (UpdateFailureException) o;

        return message != null ? message.equals(that.message) : that.message == null;
    }

    @Override
    public int hashCode() {
        return message != null ? message.hashCode() : 0;
    }
}
