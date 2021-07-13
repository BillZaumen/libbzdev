import org.bzdev.lang.annotations.*;

public interface Visitor {
    @DynamicMethod("VisitorHelper")
    void visit(Object arg);
}
