package testpkg;

public class ATestObjectSC<I,R> {
    I index;
    R x;
    public ATestObjectSC() {
    }
    public ATestObjectSC(I index, R x) throws Exception {
	this.index = index;
	this.x = x;
	if (index == null) throw new Exception();
    }
}