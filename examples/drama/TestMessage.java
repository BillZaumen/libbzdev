public class TestMessage {
    String string;
    public TestMessage(String string) {
	this.string = string;
    }

    public String getString() {return string;}

    public String toString() {
	return "msg" +"(\"" +string +"\")";
    }
}
