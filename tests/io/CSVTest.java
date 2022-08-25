import org.bzdev.io.*;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.FileReader;

public class CSVTest {

    static void testInput() throws Exception {
	InputStream is = new FileInputStream("input.csv");
	Reader r = new InputStreamReader(is, "UTF-8");
	CSVReader csvr = new CSVReader(r, false, null);
	String[] row;
	System.out.println("--------- input.csv ------------");
	while ((row = csvr.nextRow()) != null) {
	    for (int i = 0; i < row.length; i++) {
		System.out.print(row[i] + "\t");
	    }
	    System.out.println();
	}
	System.out.println("--------------------------------");
    }


    public static void main(String argv[]) throws Exception {
	testInput();

	String strings[][] = {
	    {"col1", "col2", "col3"},
	    {"abc", "\"hello\"", "ef\"g"},
	    {"h", "good,bye", "abc\"\"efg"},
	    {"", "\"", ","}
	};

	FileWriter out = new FileWriter("CSVTest.csv");
	CSVWriter w = new CSVWriter(out, 3, true);

	for (String[] row: strings) {
	    w.writeRow(row);
	}
	w.close();

	System.out.println("reading file");

	FileReader in = new FileReader("CSVTest.csv");
	CSVReader r = new CSVReader(in, true);
	for (String h: r.getHeaders()) {
	    System.out.print(h + "\t");
	}
	System.out.println();

	String[][] strings2 = new String[3][];
	String[] tmp;
	int index = 0;
	while ((tmp = r.nextRow()) != null) {
	    strings2[index++] = tmp;
	}
	if (index != 3) {
	    throw new Exception("wrong number of lines");
	}
	for (int i = 0; i < 3; i++) {
	    for (int j = 0; j < 3; j++) {
		if (!strings2[i][j].equals(strings[i+1][j])) {
		    throw new Exception("string mismatch: (" + i
					+ "," + j + ")");
		}
	    }
	}
	r.close();
	
	strings2 = new String[3][3];
	in = new FileReader("CSVTest.csv");
	r = new CSVReader(in, true);
	char[] cbuf = new char[128];
	for (int i = 0; i < 3; i++) {
	    for (int j = 0; j < 3; j++) {
		int len = r.read(cbuf);
		strings2[i][j] = (len == -1)? "": new String(cbuf, 0, len);
		r.nextField();
	    }
	}
	for (int i = 0; i < 3; i++) {
	    for (int j = 0; j < 3; j++) {
		if (!strings2[i][j].equals(strings[i+1][j])) {
		    throw new Exception("string mismatch: (" + i
					+ "," + j + ")");
		}
	    }
	}
	r.close();

	strings2 = new String[2][3];
	in = new FileReader("CSVTest.csv");
	r = new CSVReader(in, true);
	for (int i = 0; i < 2; i++) {
	    for (int j = 0; j < 3; j++) {
		int len = r.read(cbuf);
		strings2[i][j] = (len == -1)? "": new String(cbuf, 0, len);
		r.nextField();
	    }
	    if (i == 0) {
		r.nextRow();
	    } else if (r.nextRow() != null) {
		throw new Exception("did not end when expected");
	    }
	}
	for (int i = 0; i < 2; i++) {
	    for (int j = 0; j < 3; j++) {
		if (!strings2[i][j].equals(strings[2*i+1][j])) {
		    throw new Exception("string mismatch: (" + i
					+ "," + j + ")");
		}
	    }
	}
	r.close();

	out = new FileWriter("CSVTest.csv");
	w = new CSVWriter(out, 3, true);

	for (String[] line: strings) {
	    for (String field: line) {
		w.writeField(field);
	    }
	}
	w.close();

	in = new FileReader("CSVTest.csv");
	r = new CSVReader(in, true);
	strings2 = new String[3][];
        index = 0;
	while ((tmp = r.nextRow()) != null) {
	    strings2[index++] = tmp;
	}
	if (index != 3) {
	    throw new Exception("wrong number of lines");
	}
	for (int i = 0; i < 3; i++) {
	    for (int j = 0; j < 3; j++) {
		if (!strings2[i][j].equals(strings[i+1][j])) {
		    throw new Exception("string mismatch: (" + i
					+ "," + j + ")");
		}
	    }
	}
	r.close();

	in = new FileReader("CSVTest.csv");
	r = new CSVReader(in, false, null);
	strings2 = new String[4][];
        index = 0;
	System.out.println("reading file including headers");
	while ((tmp = r.nextRow()) != null) {
	    strings2[index++] = tmp;
	    for (int j = 0; j < tmp.length; j++) {
		System.out.print(tmp[j] + "\t" );
	    }
	    System.out.println();
	}
	if (index != 4) {
	    throw new Exception("wrong number of lines");
	}
	for (int i = 0; i < 4; i++) {
	    for (int j = 0; j < 3; j++) {
		if (!strings2[i][j].equals(strings[i][j])) {
		    throw new Exception("string mismatch: (" + i
					+ "," + j + ")");
		}
	    }
	}
	r.close();


	out = new FileWriter("CSVTest.csv");
	w = new CSVWriter(out, 3, true);

	for (String[] line: strings) {
	    w.writeField(line[0]);
	    w.nextRow();
	}
	w.close();

	in = new FileReader("CSVTest.csv");
	r = new CSVReader(in, true);
	strings2 = new String[3][];
        index = 0;
	while ((tmp = r.nextRow()) != null) {
	    strings2[index++] = tmp;
	}
	if (index != 3) {
	    throw new Exception("wrong number of lines");
	}
	for (int i = 0; i < 3; i++) {
	    if (!strings2[i][0].equals(strings[i+1][0])) {
		throw new Exception("string mismatch: (" + i
				    + "," + 0 + ")");
	    }
	    for (int j = 1; j < 3; j++) {
		if (strings2[i][j].length() != 0) {
		    throw new Exception("string mismatch: (" + i
					+ "," + j + ")");
		}
	    }
	}
	r.close();

    }
}
