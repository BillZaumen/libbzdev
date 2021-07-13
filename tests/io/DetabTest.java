import java.io.FileReader;
import org.bzdev.io.DetabReader;

public class DetabTest {
    public static void main(String argv[]) throws Exception {
	FileReader fr = new FileReader(argv[0]);
	DetabReader r = new DetabReader(fr);
	int b = r.read();
	while (b != -1) {
	    System.out.print((char) b);
	    b = r.read();
	}
	r.close();

	fr = new FileReader(argv[0]);
	r = new DetabReader(fr);
	char[] buf = new char[256];
	for (int i = 0; i < 20; i++) {
	    int cnt = r.read(buf, i, 17);
	    if (cnt == -1) {
		r.close();
		break;
	    }
	    for (int j = 0; j < cnt; j++) {
		System.out.print(buf[i+j]);
	    }
	}
	r.close();

	fr = new FileReader(argv[0]);
	r = new DetabReader(fr);
	b = r.read();
	while (b != -1) {
	    System.out.print((char) b);
	    b = r.read();
	}
	r.close();

    }
}
