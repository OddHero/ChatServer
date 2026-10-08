import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by joel on 03.01.2016.
 */
public final class Reference {

    static volatile boolean terminate;
    static int port;

    // Thread-safe: users are added/removed from many client threads while others iterate.
    static final List<User> users = new CopyOnWriteArrayList<>();

    private Reference() {
    }
}
