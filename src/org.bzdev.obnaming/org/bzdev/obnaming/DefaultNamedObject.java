package org.bzdev.obnaming;

//@exbundle org.bzdev.obnaming.lpack.Obnaming

/**
 * Default implementation for named objects.
 * The default implementation is a bare-bones implementation
 * suitable for simple applications.
 * The type parameter T is the subclass of DefaultNamedObject that
 * will be used as the common superclass for all named objects that
 * the corresponding object namer will recognize.
 * <P>
 * As an example, to create an object namer, a named object, and
 * a factory for the named object, one would define the following
 * classes:
 * <blockquote><code><pre>
 *  abstract public class OurNamedObject
 *        extends DefaultNamedObject&lt;OurNamedObject&gt;
 *  {
 *       ...
 *       protected OurNamedObject(OurObjectNamer namer,
 *                                String name,
 *                                boolean intern)
 *       {
 *          super(namer, name, intern);
 *          ...
 *       }
 *  }
 *
 *  public class OurObjectNamer
 *       extends DefaultObjectNamer&lt;OurNamedObject&gt;
 *  {
 *       ...
 *       public OurObjectNamer() {
 *           super(OurNamedObject.class);
 *       }
 *  }
 *
 *  abstract public class OurObjectFactory<OBJ extends OurNamedObject>
 *       extends DefaultNOFactory&lt;OurObjectNamer,OurNamedObject,OBJ&gt;
 *  {
 *       ...
 *       protected OurObjectFactory(OurObjectNamer namer) {
 *          super(namer);
 *       }
 *  }
 * </pre></code></blockquote>
 * Additional code (indicated by an ellipsis) is, of course, necessary
 * to do anything useful.
 */
public class DefaultNamedObject<T extends DefaultNamedObject<T>>
    implements NamedObjectOps
{
    static String errorMsg(String key, Object... args) {
	return ObnamingErrorMsg.errorMsg(key, args);
    }

    private String name;

    private DefaultObjectNamer<T> namer;

    private boolean interned;

    /**
     * Creates and returns a copy of this object.
     * This method will throw the exception CloneNotSupportedException
     * if the object is interned.
     * @exception CloneNotSupportedException a clone could not be
     *            created
     * @see java.lang.Object#clone()
     */
    protected Object clone() throws CloneNotSupportedException {
	if (interned) throw new CloneNotSupportedException
			  (errorMsg("noCloneInterned"));
	return super.clone();
    }

    /**
     * Get a named object's object namer.
     * @return the object namer
     */
    protected DefaultObjectNamer<T> getObjectNamer() {
	return namer;
    }

    /**
     * Determine if an object is interned in a object namer's tables.
     * @return true if the object is interned; false if not
     */
    public boolean isInterned() {return interned;}

    static private long nameID =  0;
    private String genName(Class subclass) {
	long newID = nameID++;
	if (nameID < 0) nameID = 0;
        return "[" +subclass.getName()  +(nameID++) +"]";
    }

    /**
     * Get a named object's name.
     * @return the name of the object
     */
    public final String getName() {return name;}

  /**
     * Constructor for interned objects.
     * These objects can be looked up by name using the methods
     * in {@link org.bzdev.obnaming.annotations.ObjectNamer ObjectNamer}.
     * @param namer the object namer
     * @param name the name of the object; null for an automatically generated
     *        name
     * @param intern true if the object can be looked up by using the methods
     *        in {@link org.bzdev.obnaming.DefaultObjectNamer DefaultObjectNamer};
     *        false otherwise.
     * @exception IllegalArgumentException typically means a name is already
     *            in use
     * @see org.bzdev.obnaming.DefaultObjectNamer#getObject(String,Class)
     */
    public DefaultNamedObject(DefaultObjectNamer<T> namer,
			      String name, boolean intern)
        throws IllegalArgumentException
    {
        this.namer = namer;
        Class subclass = this.getClass();
        interned = intern;
        if (intern) {
            if (name == null) {
                name = genName(subclass);
                this.name = name;
                // add the name, but if already in the table, we try again
                // with another automatically generated name.
                while (namer.addObject(name, this) == false) {
                    name = genName(subclass);
                    this.name = name;
                }
            } else {
                this.name = name;
                if (namer.addObject(name, this) == false) {

                 throw new 
		     IllegalArgumentException(errorMsg("objectNameInUse"));
                }
            }
        } else {
            if (name == null) this.name = genName(subclass);
	    else this.name = name;
        }
    }

/**
     * Determine if this named object can be deleted.
     * A named object can be deleted if the method delete
     * has not been called and if the object is not in a state that
     * prevents the object from being deleted.
     * Subclasses that override this method must call canDelete() for
     * their superclasses and return false if the superclass' canDelete
     * method returns false.
     * The default method returns true if delete() has not been
     * called and returned true.
     * @return true if this object can be deleted; false otherwise
     */
    public boolean canDelete() {
        return !deleting && !deleted;
    }

    /**
     * Complete the actions necessary to delete a named object.
     * A subclass that overrides this method must call super.onDelete()
     * at some point to complete the object deletion. This may not be
     * within the onDelete method of the subclass if the deletion must
     * be delayed for some reason (e.g., until some processing that is
     * in progress has been completed).  Once called, the object will
     * be removed from the object-namer's tables and the object will
     * be marked as deleted, so in general cleanup actions by a subclass
     * should occur before it calls super.onDelete().
     */
    protected void onDelete() {
	if (interned && deleting) namer.removeObject(this);
        deleted = true;
        deleting = false;
    }

    private boolean deleted = false;
    private boolean deleting = false;

  /**
     * Delete an object.
     * An object can only be deleted once.  If this method returns true,
     * the object (if interned) will have been removed from the object namer
     * tables.
     * @return true if the deletion request was accepted; false otherwise
     */
    public final boolean delete() {
        boolean cd = canDelete();
        if (cd == false) return false;
	if (deleted) return false;
        if (interned) {
            if (this != namer.getObject(name)) {
                return false;
            }
            deleting = true;
            onDelete();
        } else {
            deleting = true;
            onDelete();
        }
        return true;
    }

    /**
     * Determine if an object has been deleted.
     * An object is deleted if the method delete() has been
     * called and returned true.
     * @return true if deleted; false otherwise
     */
    public final boolean isDeleted() {
        return deleted;
    }
   /**
     * Determine if an object is being deleted.
     * An deletion is pending if the method delete() has been
     * called and returned true but the deletion has not been
     * completed.
     * @return true if deletion is pending; false otherwise
     */
    public final boolean deletePending() {
        return deleting;
    }
}

//  LocalWords:  exbundle DefaultNamedObject superclass namer pre lt
//  LocalWords:  blockquote OurNamedObject OurObjectNamer boolean
//  LocalWords:  DefaultObjectNamer OurObjectFactory DefaultNOFactory
//  LocalWords:  CloneNotSupportedException noCloneInterned namer's
//  LocalWords:  ObjectNamer IllegalArgumentException getObject
//  LocalWords:  objectNameInUse canDelete superclasses onDelete
