import org.bzdev.lang.annotations.*;

@DMethodContext(helper="VisitorHelper", localHelper="SomeVisitorHelper")
public class SomeVisitor implements Visitor {

    static {
	SomeVisitorHelper.register();
    }

    public void visit(Object arg) {
	VisitorHelper.getHelper().dispatch(this, arg);
    }

    @DMethodImpl("VisitorHelper")
    void doVisit1(Double arg) {
	System.out.println("double = " + arg.toString());
    }

    @DMethodImpl("VisitorHelper")
    void doVisit2(Integer arg) {
	System.out.println("integer = " + arg.toString());
    }

    public static void main(String argv[]) {
	Object obj1 = new Double(10.0);
	Object obj2 = new Integer(20);

	SomeVisitor sv = new SomeVisitor();
	
	sv.visit(obj1);
	sv.visit(obj2);

	System.out.println("now print without dynamic methods:");
	NaiveVisitor nv = new NaiveVisitor();
	nv.visit(obj1);
	nv.visit(obj2);
    }
}
