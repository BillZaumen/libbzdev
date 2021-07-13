public class NaiveVisitor implements Visitor {

    public void visit(Object arg) {
	System.out.println("found an object - will not print anything");
	return;
    }

    void visit(Double arg) {
	System.out.println("double = " + arg.toString());
    }


    void visit(Integer arg) {
	System.out.println("integer = " + arg.toString());
    }
}
