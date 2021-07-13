package org.bzdev.obnaming;
import java.util.ArrayList;
import java.util.Collection;
import java.util.MissingResourceException;
import java.util.HashMap;

/**
 * Class for managing a collection of Parm instances to simplify
 * building named-object factories.
 * Subclasses should implement a method named setDefaults that
 * takes one argument (the factory) and that does not return a
 * value.  This method should restore all parameters associated
 * with this ParmManager to their default values.  It may be
 * called by the factory to restore parameters defined using
 * annotations to their default values when the factory's clear
 * method is called.
 */
public abstract class ParmManager<T extends NamedObjectFactory> {

    private ArrayList<Parm> parmlist = new ArrayList<Parm>();
    private HashMap<String,Parm>parmMap = new HashMap<>();

    ArrayList<Parm> getParmList() {return parmlist;}

    /**
     * Get the Parm instances that have been defined.
     * @return an array of Parm
     */
    protected Parm[] getParms() {
	return parmlist.toArray(new Parm[parmlist.size()]);
    }

    /**
     * Return the parameter data that a ParmManager provides for a given name.
     * This is useful in cases where a particular parameter is removed by
     * a factory after being installed from a ParmManager and access to the
     * original is needed.
     * @param name the name of the parameter
     * @return the parameter data; null if there is none
     */
    public Parm getParm(String name) {
	return parmMap.get(name);
    }

    /**
     * Add a Parm.
     * @param parm the Parm to add
     */
    protected void addParm(Parm parm) {
	parmlist.add(parm);
	parmMap.put(parm.name, parm);
    }

    /**
     * Add every Parm in a collection.
     * @param collection the Parm collection
     */
    protected void addAll(Collection<Parm> collection) {
	parmlist.addAll(collection);
	for(Parm parm: collection) {
	    parmMap.put(parm.name, parm);
	}
    }

    /**
     * Add every Parm in an array.
     * @param parms an array of the Parm instances to add
     */
    protected void addALL(Parm[] parms) {
	for (Parm parm: parms) {
	    parmlist.add(parm);
	    parmMap.put(parm.name, parm);
	}
    }

    private T factory;

    /**
     * Constructor.
     * @param factory the factory that will interact with this ParmManager
     */
    protected ParmManager(T factory) {
	this.factory = factory;
    }

    /**
     * Get the factory.
     * @return the factory used to initialize this object
     */
    protected T getFactory() {return factory;}

    /**
     * Add a resource bundle for tips associated with configuration entries.
     * A subclass should call this method within the constructor.
     * @param baseName the fully qualified name of a class representing this
     *        resource
     * @param clazz the class of the parm manager adding the resource bundle
     */
    protected void addTipResourceBundle(String baseName,
					Class clazz)
	throws NullPointerException, MissingResourceException
    {
	factory.addTipResourceBundle(baseName, clazz);
    }

    /**
     * Add a resource bundle for tips associated with compound configuration
     * entries.
     * A subclass should call this method within the constructor.
     * @param keyPrefix the prefix for a keyed parameter name
     * @param delimiter the delimiter for a keyed parameter name
     * @param baseName the fully qualified name of a class representing this
     *        resource
     * @param clazz the class of the parm manager adding the resource bundle
     */
    protected void addTipResourceBundle(String keyPrefix,
					String delimiter,
					String baseName,
					Class clazz)
	throws NullPointerException, MissingResourceException
    {
	factory.addTipResourceBundle(keyPrefix, delimiter, baseName, clazz);
    }

    /**
     * Add a resource bundle for docs associated with configuration entries.
     * A subclass should call this method within the constructor.
     * @param baseName the fully qualified name of a class representing this
     *        resource
     * @param clazz the class of the parm manager adding the resource bundle
     */
    protected void addDocResourceBundle(String baseName,
					Class clazz)
	throws NullPointerException, MissingResourceException
    {
	factory.addDocResourceBundle(baseName, clazz);
    }

    /**
     * Add a resource bundle for docs associated with compound configuration
     * entries.
     * A subclass should call this method within the constructor.
     * @param keyPrefix the prefix for a keyed parameter name
     * @param delimiter the delimiter for a keyed parameter name
     * @param baseName the fully qualified name of a class representing this
     *        resource
     * @param clazz the class of the parm manager adding the resource bundle
     */
    protected void addDocResourceBundle(String keyPrefix,
					String delimiter,
					String baseName,
					Class clazz)
	throws NullPointerException, MissingResourceException
    {
	factory.addDocResourceBundle(keyPrefix, delimiter, baseName, clazz);
    }

    /**
     * Add a resource bundle for labels associated with configuration
     * parameters.
     * A subclass should call this method within the constructor.
     * @param baseName the fully qualified name of a class representing
     *        this resource
     * @param clazz the class of the parm manager adding the resource bundle
     */
    protected void addLabelResourceBundle(String baseName,
					  Class clazz)
	throws NullPointerException, MissingResourceException
    {
	factory.addLabelResourceBundle(baseName, clazz);
    }

    /**
     * Add a resource bundle for labels associated with
     * keyed configuration parameters.
     * A subclass should call this method within the constructor.
     * @param keyPrefix the prefix for a keyed parameter name
     * @param delimiter the delimiter for a keyed parameter name
     * @param baseName the fully qualified name of a class representing
     *        this resource
     * @param clazz the class of the parm manager adding the resource bundle
     */
    protected void addLabelResourceBundle(String keyPrefix,
					  String delimiter,
					  String baseName,
					  Class clazz)
	throws NullPointerException, MissingResourceException
    {
	factory.addLabelResourceBundle(keyPrefix, delimiter, baseName, clazz);
    }

    /**
     * Set factory fields to their default values.
     * The fields that will be set to their default values are the
     * the fields associated with the parameters that this parameter
     * manager defines.
     * <P>
     * This method should be called by the factory's <code>clear()</code>
     * method.
     * @param factory the named-object factory associated with this
     *        parameter manager
     */
    protected abstract void setDefaults(T factory);

}

//  LocalWords:  Parm Subclasses setDefaults ParmManager parm parms
//  LocalWords:  baseName clazz keyPrefix
