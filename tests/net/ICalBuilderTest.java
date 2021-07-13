import java.io.*;
import java.time.*;
import java.util.*;
import org.bzdev.net.calendar.*;

public class ICalBuilderTest {

    static ArrayList<String> list = new ArrayList<>();
    static ArrayList<Character> dlist = new ArrayList<>();

    public static void delimTest(String line) {
	StringBuilder sb = new StringBuilder();
	String[] strings1 = line.split("[,;]", -1);
	String[] strings2 = line.split("(\\\\)*[,;]", -1);
	sb.setLength(0);
	list = new ArrayList<>();
	dlist = new ArrayList<>();
	int lenm1 = strings1.length - 1;
	int index = 0;
	for (int i = 0; i < strings1.length; i++) {
	    int len1 = strings1[i].length();
	    int len2 = strings2[i].length();
	    index+= len1;
	    if ((len2 - len1) % 2 == 0) {
		sb.append(strings1[i]);
		list.add(sb.toString());
		if (index < line.length()) {
		    dlist.add(line.charAt(index));
		}
		sb.setLength(0);
	    } else {
		String substring =
		    strings1[i].substring(0,len1-1);
		sb.append(substring);
		if (i < lenm1) {
		    sb.append(line.charAt(index));
		}
	    }
	    index++;
	}
	if (sb.length() > 0) {
	    list.add(sb.toString());
	    sb.setLength(0);
	}
    }


    public static void main(String argv[]) throws Exception {

	// Folding test
	String test =
	    "DTSTART;VALUE=DATE;TZID=/freeassociation.sourceforge.net/America/Los_Angeles:20201010";
	System.out.println("------ original -------");
	System.out.println(test);
	System.out.println("------- folded ---------");
	System.out.println(ICalBuilder.fold(test));
	System.out.println("------------------------");

	String params[] = {
	    "VALUE=DATE",
	    "TZID=/freeassociation.sourceforge.net/America/Los_Angeles"};
	String prop = ICalBuilder.icalProp("DTSTART", params, false,
					   "20201010");
	System.out.println("prop = <" + prop + ">");
	System.out.println("------- folded ---------");
	System.out.println(ICalBuilder.fold(prop));
	System.out.println("------------------------");

	ICalBuilder icb = new ICalBuilder();
	icb.setMethod("PUBLISH");
	Instant created = Instant.now();
	ICalBuilder.Event event = new ICalBuilder.Event("initialUID", 1,
							created, created);
	event.setSummary("event", "altrep=\"https://foo.com/abc;d=4\"");
	event.setDescription("a very long description of event an "
		      + "xxxx xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"
		      + "xxxx xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"
		      + "xxxx xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"
		      + "xxxx xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"
		      + "xxxx xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"
		      + "xxxx xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"
		      + "xxxx xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"
		      + "xxxx xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"
			     + "xxxx xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
	event.setLocation("location");

	event.setStartTime(Instant.parse("2020-10-10T19:30:00Z"));
	event.setEndTime(Instant.parse("2020-10-10T23:30:00Z"));
	ICalBuilder.Alarm alarm =
	    new ICalBuilder.Alarm(event, -120, ICalBuilder.AlarmType.AUDIO,
				  true);
	alarm.repeat(2, 5);
	alarm = new ICalBuilder.Alarm(event, -120,
				      ICalBuilder.AlarmType.DISPLAY, true);
	alarm.repeat(3, 6);
	alarm = new ICalBuilder.Alarm(event, -120, ICalBuilder.AlarmType.EMAIL,
				      true);
	alarm.setSummary("summary", "LANGUAGE=en");
	alarm.setDescription("description", "LANGUAGE=en");
	icb.add(event);
	ICalBuilder.ToDo todo = new ICalBuilder.ToDo("initialUID", 1,
						     created, created);
	todo.setStartTime(LocalDateTime.parse("2010-01-10T19:30:00"));
	todo.setSummary("summary");
	todo.setDescription("description");
	todo.setLocation("location");

	todo.setClassification("PUBLIC");
	todo.addComment("A comment");
	todo.addComment("A comment", "LANGUAGE=en");
	todo.addCategories("FOO", "BAR");
	todo.setStatus(ICalBuilder.Status.NEEDS_ACTION);
	todo.setPriority(1);
	todo.setGeo(30.20, 120.10);
	todo.setDuration(20, ICalBuilder.Units.MINUTES);
	todo.setPercentCompleted(10);
	alarm = new ICalBuilder.Alarm(todo, -120,
				      ICalBuilder.AlarmType.DISPLAY, true);
	alarm.repeat(3, 6);
	alarm = new ICalBuilder.Alarm(todo, -120, ICalBuilder.AlarmType.EMAIL,
				      true);
	icb.add(todo);

	ICalBuilder.Journal journal = new ICalBuilder.Journal
	    ("journalUID", 1, created, created);
	journal.setStartTime
	    (LocalDate.parse("2020-10-10"),"VALUE=DATE",
	      "TZID=/freeassociation.sourceforge.net/America/Los_Angeles");
	journal.setSummary("journal summary", "LANGUAGE=en");
	journal.addDescription("description1", "LANGUAGE=en");
	journal.addDescription("description2", "LANGUAGE=de");

	icb.add(journal);

	String tzcontents[] = {
	    "TZID:/freeassociation.sourceforge.net/America/Los_Angeles",
	    "X-LIC-LOCATION:America/Los_Angeles",
	    "BEGIN:DAYLIGHT",
	    "TZNAME:PDT",
	    "DTSTART:20070310T020000",
	    "TZOFFSETFROM:-0800",
	    "TZOFFSETTO:-0700",
	    "RRULE:FREQ=YEARLY;BYDAY=2SU;BYMONTH=3",
	    "END:DAYLIGHT",
	    "BEGIN:STANDARD",
	    "TZNAME:PST",
	    "DTSTART:20071103T020000",
	    "TZOFFSETFROM:-0700",
	    "TZOFFSETTO:-0800",
	    "RRULE:FREQ=YEARLY;BYDAY=1SU;BYMONTH=11",
	    "END:STANDARD"
	};
	ICalBuilder.TimeZone tz = new ICalBuilder.TimeZone(tzcontents);
	icb.add(tz);

	byte[] bytes = icb.toByteArray();
	System.out.write(bytes);

	icb.write(new File("testcal.ics"));

	System.out.println("---------- comma test ------------");

	String[] cstrings = {
	    "abc,def",
	    "abc\\,def",
	    "abc\\\\,ef",
	    ",abc",
	    "abc,",
	    "\\,abc",
	    "abc\\,",
	    "\\\\,abc",
	    "abc\\\\,",
	    "abc,,def",
	    "abcdef"
	};

	for (String s: cstrings) {
	    delimTest(s);
	    System.out.println("\"" + s + "\":");
	    for (String s2: list) {
		System.out.println("    \"" + s2 + "\"");
	    }
	}

	System.out.println("--------- replace test -----------");

	String[] strings = {
	    "abc\\nefg",
	    "abc\\\\ndef",
	    "abc\\\\\\ndef",
	    "\\nefg",
	    "\\\\ndef",
	    "\\\\\\ndef",
	    "abc\\,bef"
	};

	for (String s: strings) {
	    String ns = s.replaceAll("((^|[^\\\\])(\\\\\\\\)*)\\\\n", "$1\n")
		.replaceAll("\\\\\\\\", "\\\\");
		System.out.format("\"%s\" ---> \"%s\"\n", s, ns);
	}

	System.out.println("---------- delim test ------------");

	String[] dstrings = {
	    "abc,def,ghi",
	    "a\\,bc",
	    "abc;def,ghi;jkl;mno",
	    "abc\\;def,ghi\\;jkl\\;mno",
	};

	for (String s: dstrings) {
	    delimTest(s);
	    System.out.println("\"" + s + "\":");
	    int i = 0;
	    System.out.print("    ");
	    for (String s3: list) {
		System.out.print("\"" + s3 + "\"");
		if (i < dlist.size()) {
		    System.out.print(dlist.get(i++));
		} else {
		    System.out.println();
		}
	    }
	}
	System.out.println("---------- parser test -----------");


	ICalParser parser = new ICalParser(bytes);

	for (ICalProperty property: parser.getProperties()) {
	    // top-level properties are trivial cases.
	    System.out.println("   " + property.getName() + ":");
	    System.out.println("       value = " + property.getValue());
	}

	for (ICalComponent node: parser.getComponents()) {
	    System.out.println("Begin " + node.getName());
	    for (ICalProperty property: node.getProperties()) {
		System.out.println("   " + property.getName() + ":");
		for (ICalParameter param: property.getParameters()) {
		    System.out.println("        param name = "
				       + param.getName());
		    System.out.println("        wasQuoted = "
				       + param.wasQuoted());
		    System.out.println("        param value = "
				       + param.getValue());
		}
		if (property.isMultiValued()) {
		    System.out.println("        values:");
		    List<Character> delims = property.getDelims();
		    int ind = 0;
		    for (String val: property.getValues()) {
			ind ++;
			System.out.print("            " + val);
			if (ind < delims.size()) {
			    System.out.println(" [" + delims.get(ind)  + "]");
			} else {
			    System.out.println();
			}
		    }
		} else {
		    System.out.println("        value = "
				       + property.getValue());
		}
	    }
	    for (ICalComponent child: node.getComponents()) {
		System.out.println("    child " + child.getName());
	    }
	}

	System.out.println("-------- check reading from a file ----------");

	parser = new ICalParser(new File("testcal.ics"));
	
	for (ICalProperty property: parser.getProperties()) {
	    // top-level properties are trivial cases.
	    System.out.println("   " + property.getName() + ":");
	    System.out.println("       value = " + property.getValue());
	}

	for (ICalComponent node: parser.getComponents()) {
	    System.out.println("Begin " + node.getName());
	    for (ICalProperty property: node.getProperties()) {
		System.out.println("   " + property.getName() + ":");
		for (ICalParameter param: property.getParameters()) {
		    System.out.println("        param name = "
				       + param.getName());
		    System.out.println("        wasQuoted = "
				       + param.wasQuoted());
		    System.out.println("        param value = "
				       + param.getValue());
		}
		if (property.isMultiValued()) {
		    System.out.println("        values:");
		    List<Character> delims = property.getDelims();
		    int ind = 0;
		    for (String val: property.getValues()) {
			ind ++;
			System.out.print("            " + val);
			if (ind < delims.size()) {
			    System.out.println(" [" + delims.get(ind)  + "]");
			} else {
			    System.out.println();
			}
		    }
		} else {
		    System.out.println("        value = "
				       + property.getValue());
		}
	    }
	    for (ICalComponent child: node.getComponents()) {
		System.out.println("    child " + child.getName());
	    }
	}
    }
}
