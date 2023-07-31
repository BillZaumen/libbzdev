import org.bzdev.util.ConfigPropUtilities;
import java.io.File;
import java.util.Properties;

public class ConfigPropTest {

    public static void main(String argv[]) throws Exception {
	File f = new File("../swing/config.foo");

	Properties props = ConfigPropUtilities.newInstance(f);

	char[] passphrase = ConfigPropUtilities.getGPGPassphrase(null);

	System.out.println(ConfigPropUtilities
			   .getDecryptedProperty(props, "ebase64.zzz2",
						 passphrase));
	System.out.println(ConfigPropUtilities
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
    }
}
