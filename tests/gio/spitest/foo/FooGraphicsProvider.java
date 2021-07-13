package foo;

public class FooGraphicsProvider implements org.bzdev.gio.spi.OSGProvider {
    String[] types = {"foo"};
    String[] suffixes = {"foo"};

    public String[] getTypes() {return types;}

    public String[] getSuffixes(String type) {
	return type.equals("foo")? suffixes: null;
    }

    public String getMediaType(String type) {
	return type.equals("foo")? "image/foo": null;
    }

    public Class<FooGraphics> getOsgClass() {return FooGraphics.class;}
}
