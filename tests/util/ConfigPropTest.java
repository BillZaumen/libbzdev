import org.bzdev.util.ConfigPropUtilities;
import org.bzdev.util.ConfigProperties;
import java.io.File;
import java.io.FileWriter;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Properties;

public class ConfigPropTest {

    private static Charset UTF8 = Charset.forName("UTF-8");

    static void test() {
	String recipients[] = {"wtz-email", "wtz-backup"};
	String encoded = ConfigPropUtilities.encodeRecipients(recipients);
	System.out.println("encoded = " + encoded);
	String[] array = ConfigPropUtilities.decodeRecipients(encoded);
	System.out.println("array.length = " + array.length);
	System.out.println("array[0] = " + array[0]);
	System.out.println("array[1] = " + array[1]);
	String encoded2 = ConfigPropUtilities.encodeRecipients(array);
	System.out.println("encoded2 = " + encoded2);
    }


    public static void main(String argv[]) throws Exception {

	test();

	String recipients[] = {"r1", "r2", "r3"};
	List<String>rlist = List.of(recipients);

	String encoded = ConfigPropUtilities.encodeRecipients(recipients);
	String encoded2 = ConfigPropUtilities.encodeRecipients(rlist);
	if (!encoded.equals(encoded2)) throw new Exception();
	System.out.println("encoded = " + encoded);
	String decoded[] = ConfigPropUtilities
	    .decodeRecipients(encoded);
	if (decoded.length != recipients.length) throw new Exception();
	for (int i = 0; i < recipients.length; i++) {
	    if (!recipients[i].equals(decoded[i])) throw new Exception();
	}
	String recip2[] = {"r1"};
	encoded = ConfigPropUtilities.encodeRecipients(recip2);
	rlist = List.of(recip2);
	encoded2 = ConfigPropUtilities.encodeRecipients(rlist);
	if (!encoded.equals(encoded2)) throw new Exception();
	decoded = ConfigPropUtilities.decodeRecipients(encoded);
	if (decoded.length != recip2.length) throw new Exception();
	for (int i = 0; i < recip2.length; i++) {
	    if (!recip2[i].equals(decoded[i])) throw new Exception();
	}
	String recip3[] = {};
	encoded = ConfigPropUtilities.encodeRecipients(recip3);
	rlist = List.of(recip3);
	encoded2 = ConfigPropUtilities.encodeRecipients(rlist);
	if (!encoded.equals(encoded2)) throw new Exception();
	decoded = ConfigPropUtilities.decodeRecipients(encoded);
	if (decoded.length != recip3.length) throw new Exception();
	String recip4[] = {""};
	encoded = ConfigPropUtilities.encodeRecipients(recip4);
	rlist = List.of(recip4);
	encoded2 = ConfigPropUtilities.encodeRecipients(rlist);
	if (!encoded.equals(encoded2)) throw new Exception();
	decoded = ConfigPropUtilities.decodeRecipients(encoded);
	if (decoded.length != recip4.length-1) throw new Exception();

	File f = new File("../swing/config.foo");

	Properties props = ConfigPropUtilities.newInstance(f);

	char[] passphrase = ConfigPropUtilities.getPassphrase(null);

	System.out.println("ebase64.password = " +
			   new String(ConfigPropUtilities
				      .getDecryptedProperty(props,
							    "ebase64.password",
							    passphrase)));
	System.out.println("base64.zzz1 = " + ConfigPropUtilities
			   .getProperty(props, "base64.zzz1"));
	System.out.println("key1 = " + ConfigPropUtilities
			   .getProperty(props, "key1"));
	System.out.println("key2 = " + ConfigPropUtilities
			   .getProperty(props, "key2"));
	System.out.println("key3 = " + ConfigPropUtilities
			   .getProperty(props, "key3"));
	System.out.println("key4 = " + ConfigPropUtilities
			   .getProperty(props, "key4"));
	System.out.println("key5 = " + ConfigPropUtilities
			   .getProperty(props, "key5"));
	System.out.println("key6 = " + ConfigPropUtilities
			   .getProperty(props, "key6"));
	System.out.println("key7 = " + ConfigPropUtilities
			   .getProperty(props, "key7"));

	System.out.println("now try ConfigProperties");
	ConfigProperties cprops =
	    new ConfigProperties(f, "application/foo");
	System.out.println("ebase64.password = " +
			   new String(cprops
				      .getDecryptedProperty("ebase64.password",
							    passphrase)));
	System.out.println("base64.zzz1 = "
			   + cprops.getProperty("base64.zzz1"));
	System.out.println("key1 = " + cprops.getProperty("key1"));
	System.out.println("key2 = " + cprops.getProperty("key2"));
	System.out.println("key3 = " + cprops.getProperty("key3"));
	System.out.println("key4 = " + cprops.getProperty("key4"));
	System.out.println("key5 = " + cprops.getProperty("key5"));
	System.out.println("key6 = " + cprops.getProperty("key6"));
	System.out.println("key7 = " + cprops.getProperty("key7"));
	String stored = cprops.store();

	System.out.println("now try ConfigProperties after storing/reloading");

	cprops = new ConfigProperties(stored, "application/foo");
	System.out.println("ebase64.password = " +
			   new String(cprops
				      .getDecryptedProperty("ebase64.password",
							    passphrase)));
	System.out.println("base64.zzz1 = "
			   + cprops.getProperty("base64.zzz1"));
	System.out.println("key1 = " + cprops.getProperty("key1"));
	System.out.println("key2 = " + cprops.getProperty("key2"));
	System.out.println("key3 = " + cprops.getProperty("key3"));
	System.out.println("key4 = " + cprops.getProperty("key4"));
	System.out.println("key5 = " + cprops.getProperty("key5"));
	System.out.println("key6 = " + cprops.getProperty("key6"));
	System.out.println("key7 = " + cprops.getProperty("key7"));

	if (argv.length == 0) System.exit(0);

	String list[] = {argv[0]};
	props = new Properties();
	ConfigPropUtilities.setProperty(props,"foo.val", "$10");
	ConfigPropUtilities.setProperty(props, "foo.int", "20");
	ConfigPropUtilities.setProperty(props, "ebase64.key1", "encrypted key1",
					"password".toCharArray());
	ConfigPropUtilities.setProperty(props, "ebase64.key2", "encrypted key2",
					null, list);
	ConfigPropUtilities.setProperty(props, "base64.key3", "value for key3");

	System.out.println("ebase64.key1: "
			   + props.getProperty("ebase64.key1"));
	System.out.println("ebase64.key2: "
			   + props.getProperty("ebase64.key2"));
	System.out.println("base64.key3: "
			   + props.getProperty("base64.key3"));


	ConfigPropUtilities.store(props, new File("junk"),
				  "application/foo");

	System.out.println("foo.val = " + ConfigPropUtilities
			   .getProperty(props, "foo.val"));
	System.out.println("foo.int = " + ConfigPropUtilities
			   .getProperty(props, "foo.int"));
	System.out.println("ebase64.key1 = "
			   + new String(ConfigPropUtilities
					.getDecryptedProperty(props,
							      "ebase64.key1",
							      "password"
							      .toCharArray())));
	System.out.println("ebase64.key2 = "
			   + new String(ConfigPropUtilities
					.getDecryptedProperty(props,
							      "ebase64.key2",
							      passphrase)));

	System.out.println("base64.key3 = " + ConfigPropUtilities
				.getProperty(props, "base64.key3"));
    }
}
