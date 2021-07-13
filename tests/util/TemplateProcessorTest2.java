import org.bzdev.util.TemplateProcessor;
import org.bzdev.util.TemplateProcessor.KeyMap;
import java.io.*;

public class TemplateProcessorTest2 {
    static public void main(String argv[]) throws Exception {

	TemplateProcessor.KeyMap map = new TemplateProcessor.KeyMap();

	TemplateProcessor.KeyMap map1 = new TemplateProcessor.KeyMap();
	TemplateProcessor.KeyMap map2 = new TemplateProcessor.KeyMap();
	TemplateProcessor.KeyMap map3 = new TemplateProcessor.KeyMap();
	TemplateProcessor.KeyMap map4 = new TemplateProcessor.KeyMap();

	map1.put("value", "10");
	map2.put("value", "20");
	map3.put("value", "30");
	map4.put("value", "40");

	map.put("map1", map1);
	map.put("map2", map1);
	map.put("map3", map1);
	map.put("map4", map1);

	TemplateProcessor tp = new TemplateProcessor(map);
	System.out.println("badTemplate1.tpl ...");
	FileReader fr = new FileReader("badTemplate1.tpl");
	OutputStreamWriter osw = new OutputStreamWriter(System.out);
	try {
	    tp.processTemplate(fr, osw);
	} catch (Exception e) {
	    System.out.println(e.getMessage());
	}

	System.out.println("badTemplate2.tpl ...");
	fr = new FileReader("badTemplate2.tpl");
	try {
	    tp.processTemplate(fr, osw);
	} catch (Exception e) {
	    System.out.println(e.getMessage());
	}
	System.out.println("badTemplate3.tpl ...");
	fr = new FileReader("badTemplate3.tpl");
	try {
	    tp.processTemplate(fr, osw);
	} catch (Exception e) {
	    System.out.println(e.getMessage());
	}

	System.out.println("badTemplate4.tpl ...");
	fr = new FileReader("badTemplate4.tpl");
	try {
	    tp.processTemplate(fr, osw);
	} catch (Exception e) {
	    System.out.println(e.getMessage());
	}


	System.out.println("... four exceptions expected");
    }
}
