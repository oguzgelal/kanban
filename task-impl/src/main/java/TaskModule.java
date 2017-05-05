import com.google.inject.AbstractModule;
import com.kanban.TaskService;
import com.lightbend.lagom.javadsl.server.ServiceGuiceSupport;

public class TaskModule extends AbstractModule implements ServiceGuiceSupport {
    @Override
    protected void configure() {
        bindServices(serviceBinding(TaskService.class,TaskServiceImpl.class));
    }
}
