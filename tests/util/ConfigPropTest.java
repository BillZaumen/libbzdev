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
			   
	System.out.println(ConfigPropUtilities
			   .getProperty(props, "key1"));
	System.out.println(ConfigPropUtilities
			   .getProperty(props, "key2"));
	System.out.println(ConfigPropUtilities
			   .getProperty(props, "key3"));
    }
}
