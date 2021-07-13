import org.bzdev.imageio.ImageMimeInfo;
import java.io.File;

public class ImageMIMEInfoTest {
    public static void main(String argv[]) throws Exception {
	for (String s: ImageMimeInfo.getFormatNames()) {
	    String mtype = ImageMimeInfo.getMimeType(s);
	    System.out.println (s + " --> " + mtype);
	    System.out.print("   ");
	    for (String suffix: ImageMimeInfo.getSuffixes(mtype)) {
		System.out.print(" " + suffix);
	    }
	    System.out.println();
	}
	System.out.println("FILE.JPG -> "
			   +ImageMimeInfo.getMIMETypeForSuffix("JPG"));
	System.out.println("FILE.jpg -> "
			   +ImageMimeInfo.getMIMETypeForSuffix("jpg"));

	System.out.println("FILE.PNG -> "
			   +ImageMimeInfo.getMIMETypeForSuffix("PNG"));
	System.out.println("FILE.png -> "
			   +ImageMimeInfo.getMIMETypeForSuffix("png"));

	System.out.println("extension for foo.jpg: "
			   + ImageMimeInfo.getFilenameExtension("foo.jpg"));

	System.out.println("format mame for foo.jpg: "
			   + ImageMimeInfo.getFormatNameForFile("foo.jpg"));

	System.out.println("format mame for FILE foo.jpg: "
			   + ImageMimeInfo
			   .getFormatNameForFile(new File("foo.jpg")));

	System.exit(0);


    }
}
