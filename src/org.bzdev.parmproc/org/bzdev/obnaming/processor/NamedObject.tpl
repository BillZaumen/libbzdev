$(package)
import org.bzdev.obnaming.*;
// import javax.annotation.processing.Generated;

// @Generated(value="$(generator)", date="$(date)")
class $(helperClass)
     $(extendsHelperSuperclass)$(helperSuperclassTypeParms)
     implements NamedObjectOps
{
    private String name;

    private $(namerHelperClass) namer;

    private boolean interned;

    /**
     * Creates and returns a copy of this object.
     * This method will throw the exception CloneNotSupportedException
     * if the object is interned.
     * @exception CloneNotSupportedException a clone could not be
     *            created
     * @see java.lang.Object#clone()
     */
    protected Object clone()  throws CloneNotSupportedException {
	if (interned) throw new CloneNotSupportedException
			  ("cannot clone interned named objects");
	return super.clone();
    }

    /**
     * Determine if an object is interned in a object namer's tables.
     * @return true if the object is interned; false if not
     */
    public boolean isInterned() {
        return interned;
    }

    /**
     * Get the object namer for a named object.
     * @return the object namer for this named object
     */
    protected $(namerClass) getObjectNamer() {
       if (namer instanceof $(namerClass)) {
          return ($(namerClass)) namer;
       } else {
         throw new IllegalStateException("wrong object namer");
       }
    }

    static private long nameID =  0;
    private String genName(Class subclass) {
	long newID = nameID++;
	if (nameID < 0) nameID = 0;
        return "[" +subclass.getName()  +(nameID++) +"]";
    }

    /**
     * Get an object's name.
     * @return the name of the object
     */
    public final String getName() {return name;}

    $(ocloop:cloopEnd)
    $(helperClass)($(namerHelperClass) namer, String name, boolean intern
                   $(constrArgs))
        throws IllegalArgumentException$(constrExceptions)
    {
        super($(superCall));
	if (!(namer instanceof $(namerClass))) {
	    throw new IllegalArgumentException("first argument not an instance "
					       + "of $(namerClass)");
	}
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
                        IllegalArgumentException("object name is in use");
                }
            }
        } else {
            if (name == null) this.name = genName(subclass);
	    else this.name = name;
        }
    }
    $(cloopEnd)

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
	if (interned && deleting) {
            namer.removeObject(this);
	}
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
     * <P>
     * The implementations provided by
     * {@link DefaultNamedObject DefaultNamedObect} and generated because of
     * a {@link org.bzdev.obnaming.annotations.NamedObject @NamedObject}
     * annotation provide a protected method named onDelete.
     * A subclass that overrides onDelete() must call
     * the onDelete method of its superclass after it's onDelete
     * method has been called and any cleanup actions performed.  In
     * some cases, this may happen at a later time (e.g., if a thread
     * is used for some of the cleanup operations or if it is otherwise
     * necessary to wait).
     *
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
