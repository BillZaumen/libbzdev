import org.bzdev.net.FormDataIterator;
import java.io.*;
import java.util.Arrays;


public class FormDataTest {

    static String boundary1 ="------------------------06c985ac7930dd78";
    static String boundary = "------------------------e0e57aa4a6726592";

    public static void main(String argv[]) throws Exception {

	System.out.println("=========== formdata465.txt ==========");
	System.out.println();


	InputStream is;
	FormDataIterator it;
	
	is = new FileInputStream("formdata465.txt");
	it = new FormDataIterator(is, boundary1);
	while (it.hasNext()) {
	    System.out.println("Next entry:");
	    InputStream cis = it.next();
	    if (cis == null) System.out.println("[no input stream]");
	    System.out.println("content-type = " + it.getContentType());
	    System.out.println("media-type = " + it.getMediaType());
	    System.out.println("name = " + it.getName());
	    System.out.println("filename = " + it.getFileName());
	    System.out.println("charset = " + it.getCharset());
	    System.out.println("----------------");
	    if (it.getMediaType() == null
		|| it.getMediaType().startsWith("text/")) {
		cis.transferTo(System.out);
	    }
	    System.out.println("\n----------------");
	}
	System.out.println("Done");

	System.out.println("=========== varying single-entry tests ==========");


	for (int i = 0; i < 10000; i++) {
	    InputStream is1 = new FileInputStream("formdata1.txt");
	    byte[] array = new byte[i];
	    Arrays.fill(array, (byte)'a');
	    ByteArrayInputStream is2 = new ByteArrayInputStream(array);
	    InputStream is3 = new FileInputStream("formdata2.txt");
	    is = new SequenceInputStream(is1,
					 new SequenceInputStream(is2, is3));
	    it = new FormDataIterator(is, boundary1);
	    while (it.hasNext()) {
		InputStream cis = it.next();
		if (!it.getName().equals("foo")) {
		    throw new Exception("wrong name");
		}
		int count = 0;
		int b;
		while ((b = cis.read()) != -1) {
		    if (b != (byte)'a') {
			System.out.println("b = " + b + ", i = " + i);
			throw new Exception("wrong byte");
		    }
		    count++;
		}
		if (count != i) {
		    throw new Exception("wrong number of bytes");
		}
	    }
	}

	System.out.println("=========== varying double-entry tests ==========");


	for (int i = 0; i < 1024; i++) {
	    for (int j = 0; j < 1024; j++) {
		InputStream is1 = new FileInputStream("formdata1.txt");
		byte[] array1 = new byte[i];
		Arrays.fill(array1, (byte)'a');
		ByteArrayInputStream is2 = new ByteArrayInputStream(array1);
		InputStream is3 = new FileInputStream("formdata15.txt");
		byte[] array2 = new byte[j];
		Arrays.fill(array2, (byte)'b');
		ByteArrayInputStream is4 = new ByteArrayInputStream(array2);
		InputStream is5 = new FileInputStream("formdata2.txt");
		// System.out.println("Case i = " + i + ", j = " + j);
		is = new SequenceInputStream
		    (is1, new SequenceInputStream
		     (is2, new SequenceInputStream
		      (is3, new SequenceInputStream(is4, is5))));
		it = new FormDataIterator(is, boundary1);
		String name = null;
		while (it.hasNext()) {
		    InputStream cis = it.next();
		    byte eb = (name == null)? (byte)'a': (byte)'b';
		    name = (name == null)? "foo": "bar";
		    if (!it.getName().equals(name)) {
			throw new Exception("wrong name, expecting " + name);
		    }
		    int count = 0;
		    int b;
		    while ((b = cis.read()) != -1) {
			if (b != (byte)eb) {
			    System.out.println("b = " + b + ", i = " + i
					       + ", j = " + j);
			    throw new Exception("wrong byte");
			}
			count++;
		    }
		    if (count != ((eb == 'a')? i: j)) {
			throw new Exception("wrong number of bytes");
		    }
		}
	    }
	}


	System.out.println("=========== formdata.txt ==========");
	System.out.println();

	is = new FileInputStream("formdata.txt");
	it = new FormDataIterator(is, boundary);
	while (it.hasNext()) {
	    System.out.println("Next entry:");
	    InputStream cis = it.next();
	    if (cis == null) System.out.println("[no input stream]");
	    System.out.println("content-type = " + it.getContentType());
	    System.out.println("media-type = " + it.getMediaType());
	    System.out.println("name = " + it.getName());
	    System.out.println("filename = " + it.getFileName());
	    System.out.println("charset = " + it.getCharset());
	    System.out.println("----------------");
	    if (it.getMediaType() == null
		|| it.getMediaType().startsWith("text/")) {
		cis.transferTo(System.out);
	    }
	    System.out.println("\n----------------");
	}
	System.out.println("Done");
    }
}
