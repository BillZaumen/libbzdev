$(package)

/**
 * Factory for creating instances of $(stdFactory).$(showParmDoc:endShowParmDoc)
 * <P>
 * The parameters this factory uses are defined as follows:
 * <IFRAME SRC="{@docRoot}/factories-api/$(packagePath)$(packagePathSep)$(stdFactory).html" style= "$(style)">
 * Please see
 * <A HREF="{@docRoot}/factories-api/$(packagePath)$(packagePathSep)$(stdFactory).html">
 * the parameter documentation</A> for a table of the parameters that are
 * supported by this factory.
 * </IFRAME>$(endShowParmDoc)
 */
public class $(stdFactory) extends $(factory)<$(typeParmClasses)> {

    private $(namerClass) $(namerVar);

    /**
     * Constructor.
     * @param $(namerVar) $(namerDoc)
     */
    public $(stdFactory)($(namerClass) $(namerVar)) {
	super($(namerVar));
	this.$(namerVar) = $(namerVar);
    }

    /**
     * Constructor for a service provider.
     * This constructor should not be used directly. It is necessary
     * because of the introduction of modules in Java 9, and is used by
     * a service provider that allows factories to be listed,
     * possibly with documentation regarding their parameters. It
     * just calls the default constructor with a null argument.
     */
    public $(stdFactory)() {
	this(null);
    }

    /**
     * Construct a new object.
     * Please see
     * {@link org.bzdev.obnaming.NamedObjectFactory#newObject(String)}
     * for details.
     * @param name the name of the object to be created
     * @return the new object
    */
    @Override
    protected $(namedObjectClass) newObject(String name) {
	return new $(namedObjectClass)($(namerVar), name, willIntern());
    }
}
