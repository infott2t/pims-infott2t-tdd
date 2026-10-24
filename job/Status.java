package job;
import java.util.Objects;

class Status {

    enum Name {
        INIT,
        PENDING,
        IN_PROGRESS,
        COMPLETED
    }

    private final Name name;

    Status(Name name) {
        this.name = Objects.requireNonNull(name, "name");
    }
}