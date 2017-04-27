import com.google.inject.AbstractModule;
import com.kanban.BoardService;
import com.lightbend.lagom.javadsl.server.ServiceGuiceSupport;

public class BoardModule extends AbstractModule implements ServiceGuiceSupport {
    @Override
    protected void configure() {
        bindServices(serviceBinding(BoardService.class,BoardServiceImpl.class));
    }
}
