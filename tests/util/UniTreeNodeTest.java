import org.bzdev.util.UniTreeNode;


public class UniTreeNodeTest {

    public static void main(String argv[]) throws Exception {

	UniTreeNode<Number> root = new UniTreeNode<>(10.0);
	UniTreeNode<Number> next1 = root.add(30.0);
	UniTreeNode<Number> next2 = root.add(35.0);
	next1 = next1.add(40.0);
	next2 = next2.add(45.0);
	UniTreeNode<Number> next3 = null;
	next3 = UniTreeNode.addTo(11.0, next3);
	next3 = UniTreeNode.addTo(22.0, next3);
	next3 = UniTreeNode.addTo(33.0, next3);

	System.out.println("next1: " + next1.getElement()
			   + " " + next1.parent().getElement()
			   + " " + next1.parent().parent().getElement());
	System.out.println("next2: " + next2.getElement()
			   + " " + next2.parent().getElement()
			   + " " + next2.parent().parent().getElement());

	System.out.println("next3: " + next3.getElement()
			   + " " + next3.parent().getElement()
			   + " " + next3.parent().parent().getElement());

	System.out.println("next3 values:");
	for (Number n: next3) {
	    System.out.println("    " + n);
	}

	System.out.println("next values (via stream):");
	next3.stream().forEach((e) -> {System.out.println("    " + e);});

	System.out.println("next values (via parallelstream):");
	next3.parallelStream().forEach((e) -> {
		System.out.println("    " + e);
	    });
    }
}

