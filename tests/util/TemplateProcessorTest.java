import org.bzdev.util.TemplateProcessor;
import org.bzdev.util.TemplateProcessor.KeyMap;
import java.io.*;

public class TemplateProcessorTest {
    static public void main(String argv[]) throws Exception {
	// test
	boolean useKeyMapList = false;
	boolean useKeyMap = false;
	if (argv.length == 1) {
	    if (argv[0].equals("useKeyMapList")) {
		useKeyMapList = true;
	    }
	    if (argv[0].equals("useKeyMap")) {
		useKeyMap = true;
	    }
	}
	try {
	    // Test bugs that were found during use.
	    FileReader fr = new FileReader("circle.tpl");
	    OutputStreamWriter osw = new OutputStreamWriter(System.out);
	    TemplateProcessor.KeyMap map = new TemplateProcessor.KeyMap();
	    map.put("circle", "circle");
	    TemplateProcessor tp = new TemplateProcessor(map);
	    tp.processTemplate(fr, osw);
	    osw.flush();
	    System.out.println("-----------");
	    fr = new FileReader("circle2.tpl");
	    TemplateProcessor.KeyMapList list =
		new TemplateProcessor.KeyMapList();
	    list.add(map);
	    map = new TemplateProcessor.KeyMap();
	    map.put("square", "square");
	    list.add(map);
	    map = new TemplateProcessor.KeyMap();
	    map.put("list", list);
	    map.print();
	    tp = new TemplateProcessor(map);
	    tp.processTemplate(fr, osw);
	    osw.flush();
	} catch (java.lang.Exception e) {
	    e.printStackTrace();
	    System.exit(1);
	}

	try {
	    FileReader fr = new FileReader("params.tpl");
	    OutputStreamWriter osw = new OutputStreamWriter(System.out);
	    // FileWriter osw = new FileWriter("params.out");

	    TemplateProcessor.KeyMap map = new TemplateProcessor.KeyMap();
	    map.put("hasAllImages", "true");
	    map.put("imageTime", "5000");
	    map.put("minImageTime", "2000");
	    map.put("syncMode", "false");
	    map.put("waitOnError", "false");
	    map.put("highResDir", "high");
	    map.put("wOffset", "15");
	    map.put("hOffset", "15");
	    map.put("wPercent", "100");
	    map.put("hPercent", "100");

	    TemplateProcessor.KeyMap map1 = new TemplateProcessor.KeyMap();
	    map1.put("name", "Bunny1");
	    map1.put("width", "500");
	    map1.put("height", "375");
	    map1.put("ext", "jpg");
	    map1.put("commaSeperator", ",");
	
	    TemplateProcessor.KeyMap map2 = new TemplateProcessor.KeyMap();
	    map2.put("name", "Bunny2");
	    map2.put("width", "500");
	    map2.put("height", "375");
	    map2.put("ext", "jpg");
	    if (useKeyMap) {
		map.put("repeatImageArrayEntries", map1);
	    } else if (useKeyMapList) {
		TemplateProcessor.KeyMapList maps = 
		    new TemplateProcessor.KeyMapList();
		maps.add(map1);
		maps.add(map2);
		map.put("repeatImageArrayEntries", maps);
	    } else {
		KeyMap[] maps = {map1, map2};
		map.put("repeatImageArrayEntries", maps);
	    }
	    TemplateProcessor.KeyMap map3 = new TemplateProcessor.KeyMap();
	    map3.put("x", "10");
	    TemplateProcessor.KeyMap map4 = new TemplateProcessor.KeyMap();
	    map4.put("x", "20");
	    map.put("lowx1", "true", map3, map4);
	    map.put("lowx2", null, map3, map4);

	    map.print();

	    TemplateProcessor tp = new TemplateProcessor(map);

	    tp.processTemplate(fr, osw);
	    osw.flush();
	    // osw.close();
	    System.out.println("------------------------------------");

	    String firstName = "John";
	    String lastName = "Doe";
	    Boolean lastNameFirst = false;
	    String title = null;

	    TemplateProcessor.KeyMap kmap = new TemplateProcessor.KeyMap();
	    if (firstName != null) {
		kmap.put("firstName", firstName.trim());
		// kmap.put("hasFirstName", emptymap);
	    } else {
		kmap.put("noFirstName", new TemplateProcessor.KeyMap());
	    }
	    if (lastName != null) {
		kmap.put("lastName", lastName.trim());
		// kmap.put("hasLastName", new TemplateProcessor.KeyMap());
	    } else {
		kmap.put("noLastName", new TemplateProcessor.KeyMap());
	    }
	    if (lastNameFirst) {
		kmap.put("lastNameFirst", new TemplateProcessor.KeyMap());
	    } else {
		kmap.put("lastNameLast", new TemplateProcessor.KeyMap());
	    }
	    if (title != null) {
		kmap.put("title", title.trim());
		// kmap.put("hasTitle", new TemplateProcessor.KeyMap());
	    } else {
		kmap.put("noTitle", new TemplateProcessor.KeyMap());
	    }
	    kmap.print();
	    System.out.println("kmap.get(\"+firstName\") = "
			       + kmap.get("+firstName").getClass());
	    System.out.println("kmap.get(\"-firstName\") = "
			       + kmap.get("-firstName"));

	    tp = new TemplateProcessor(kmap);
	    fr = new FileReader("params2.tpl");
	    tp.processTemplate(fr, osw);
	    osw.flush();
	    osw.close();

	} catch (java.lang.Exception e) {
	    e.printStackTrace();
	    System.exit(1);
	}
	System.exit(0);
   }
}
