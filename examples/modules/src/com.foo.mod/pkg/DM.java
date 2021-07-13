package pkg;
import java.io.*;
import org.bzdev.lang.MethodNotPresentException;
import org.bzdev.lang.annotations.*;

public class DM {
    @DynamicMethod("DMHelper")
    public void print(String str) throws IOException {
	try {
	    DMHelper.getHelper().dispatch(this, str);
	} catch (MethodNotPresentException e) {
	    // nothing to print
	    return;
	}
    }
}
