import org.bzdev.net.*;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.net.*;
import javax.net.ssl.*;
import java.security.cert.*;
import java.security.*;
import java.security.spec.*;
import java.time.Instant;
import java.util.Base64;

public class PemTest {

    public static void main(String argv[]) throws Exception {
	URL url = new URL("https://google.com");
	URLConnection urlc = url.openConnection();
	urlc.connect();
	if (urlc instanceof HttpsURLConnection) {
	    java.security.cert.Certificate[] certs = ((HttpsURLConnection)urlc)
		.getServerCertificates();
	    if (certs != null && certs.length > 0) {
		PublicKey pkey = certs[0].getPublicKey();
		byte[] encoded = pkey.getEncoded();
		StringBuilder sb = new StringBuilder();
		PemEncoder encoder = new PemEncoder(sb);

		encoder.addHeader("encryption-algorithm",
				     pkey.getAlgorithm());
		encoder.encode("PUBLIC KEY", encoded);
		PemDecoder.Result result = PemDecoder.decode(sb.toString());

		if (!result.getHeaders().getFirst("encryption-algorithm")
		    .equals(pkey.getAlgorithm())) {
		    throw new Exception();
		}
		if (!result.getType().equals("PUBLIC KEY")) {
		    throw new Exception();
		}
		byte[] decoded = result.getBytes();
		if (decoded.length != encoded.length) {
		    throw new Exception();
		}
		for (int i = 0; i < encoded.length; i++) {
		    if (decoded[i] != encoded[i]) {
			throw new Exception();
		    }
		}
		result = PemDecoder.decode(new StringReader(sb.toString()));
		if (!result.getHeaders().getFirst("encryption-algorithm")
		    .equals(pkey.getAlgorithm())) {
		    throw new Exception();
		}
		if (!result.getType().equals("PUBLIC KEY")) {
		    throw new Exception();
		}
		decoded = result.getBytes();
		if (decoded.length != encoded.length) {
		    throw new Exception();
		}
		for (int i = 0; i < encoded.length; i++) {
		    if (decoded[i] != encoded[i]) {
			throw new Exception();
		    }
		}
		int index = sb.indexOf("BEGIN PUBLIC KEY-----");
		if (index < 0) {
		    throw new Exception();
		}
		index = sb.indexOf("\n", index);
		if (index < 0) {
		    throw new Exception();
		}
		index++;
		//GPG signatures have a blank line after the BEING ... line
		// so reproduce that for testing.  We should get the same
		// results as before.
		sb.insert(index, "\r\n");

		result = PemDecoder.decode(sb.toString());

		if (!result.getHeaders().getFirst("encryption-algorithm")
		    .equals(pkey.getAlgorithm())) {
		    throw new Exception();
		}
		if (!result.getType().equals("PUBLIC KEY")) {
		    throw new Exception();
		}
		decoded = result.getBytes();
		if (decoded.length != encoded.length) {
		    throw new Exception();
		}
		for (int i = 0; i < encoded.length; i++) {
		    if (decoded[i] != encoded[i]) {
			throw new Exception();
		    }
		}
		result = PemDecoder.decode(new StringReader(sb.toString()));
		if (!result.getHeaders().getFirst("encryption-algorithm")
		    .equals(pkey.getAlgorithm())) {
		    throw new Exception();
		}
		if (!result.getType().equals("PUBLIC KEY")) {
		    throw new Exception();
		}
		decoded = result.getBytes();
		if (decoded.length != encoded.length) {
		    throw new Exception();
		}
		for (int i = 0; i < encoded.length; i++) {
		    if (decoded[i] != encoded[i]) {
			throw new Exception();
		    }
		}
	    }
	    System.exit(0);
	}
    }
}
