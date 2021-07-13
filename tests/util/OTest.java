import org.bzdev.util.*;


public class OTest {

    public static void main(String argv[]) throws Exception {
	
	System.out.println("STRING:");
	ObjectParser<?> parser =
	    ObjectParser.STRING;

	System.out.println(parser.matches("hello"));
	System.out.println(parser.parse("hello"));

	System.out.println("NULL:");
	System.out.println();
	parser = ObjectParser.NULL;

	System.out.println(parser.matches("null"));
	System.out.println(parser.parse("null"));

	System.out.println();
	System.out.println("BOOLEAN:");
	parser = ObjectParser.BOOLEAN;

	System.out.println(parser.matches("true"));
	System.out.println(parser.parse("true"));
	System.out.println(parser.matches("false"));
	System.out.println(parser.parse("false"));

	System.out.println();
	System.out.println("LONG:");
	parser = ObjectParser.LONG;

	String s = "123456789012345";
	System.out.println(parser.matches(s));
	System.out.println(parser.parse(s));
	s = "-123456789012345";
	System.out.println(parser.matches(s));
	System.out.println(parser.parse(s));
	
	System.out.println("INTEGER:");
	System.out.println();
	parser = ObjectParser.INTEGER;
	s = "1234";
	System.out.println(parser.matches(s));
	System.out.println(parser.parse(s));
	s = "-1234";
	System.out.println(parser.matches(s));
	System.out.println(parser.parse(s));

	
	System.out.println();
	System.out.println("DOUBLE:");
	parser = ObjectParser.DOUBLE;
	s = "1.34";
	System.out.println(parser.matches(s));
	System.out.println(parser.parse(s));
	s = "-1.34";
	System.out.println(parser.matches(s));
	System.out.println(parser.parse(s));
	s = "1.34e10";
	System.out.println(parser.matches(s));
	System.out.println(parser.parse(s));
	s = "-1.34e10";
	System.out.println(parser.matches(s));
	System.out.println(parser.parse(s));
	s = "1.34e-10";
	System.out.println(parser.matches(s));
	System.out.println(parser.parse(s));
	s = "-1.34e-10";
	System.out.println(parser.matches(s));
	System.out.println(parser.parse(s));

	System.out.println();
	System.out.println("NUMBER:");
 	parser = ObjectParser.NUMBER;
	s = "123456789012345";
	System.out.println(parser.matches(s));
	System.out.println(parser.parse(s));
	s = "-123456789012345";
	System.out.println(parser.matches(s));
	System.out.println(parser.parse(s));
	s = "1234";
	System.out.println(parser.matches(s));
	System.out.println(parser.parse(s));
	s = "-1234";
	System.out.println(parser.matches(s));
	System.out.println(parser.parse(s));
	s = "1.34";
	System.out.println(parser.matches(s));
	System.out.println(parser.parse(s));
	s = "-1.34";
	System.out.println(parser.matches(s));
	System.out.println(parser.parse(s));
	s = "1.34e10";
	System.out.println(parser.matches(s));
	System.out.println(parser.parse(s));
	s = "-1.34e10";
	System.out.println(parser.matches(s));
	System.out.println(parser.parse(s));
	s = "1.34e-10";
	System.out.println(parser.matches(s));
	System.out.println(parser.parse(s));
	s = "-1.34e-10";
	System.out.println(parser.matches(s));
	System.out.println(parser.parse(s));

	parser = ObjectParser.INTLONG;
	s = "-1000000000000";
	System.out.println("string: " + s);
	System.out.println(parser.matches(s));
	System.out.println(parser.parse(s));
	


   }
}
