package dmtest.a;
import org.bzdev.lang.*;
import org.bzdev.lang.annotations.*;

public interface Visitor {
    @DynamicMethod("VisitorHelper")
	@DMethodOptions(traceMode=true)
    void visit(Object arg);
}
