package events;

import com.lightbend.lagom.javadsl.persistence.AggregateEventTag;

public class TaskEventTag {

    public static final AggregateEventTag<TaskEvent> INSTANCE = AggregateEventTag.of(TaskEvent.class);
}
