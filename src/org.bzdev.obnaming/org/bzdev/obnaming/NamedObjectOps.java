package org.bzdev.obnaming;

/**
 * Interface for Named Objects.
 * This interface is provided mostly for documentation as javadoc may
 * not show classes that are not declared to be public as is the case
 * with named-object helper classes. The public named-object class
 * should implement this interface so that javdocs will provide
 * appropriate documentation.
 * <P>
 * Note that the protected methods getObjectNamer() and onDelete() may
 * not be documented due to it being defined in a class that is not
 * public. The method getObjectNamer is an access method that returns
 * the object namer associated with a named object, casting it to a
 * type specified by
 * {@link org.bzdev.obnaming.annotations.NamedObject {@literal @}NamedObject}.
 * The method onDelete() will be called once when delete() is called.
 * onDelete() must either call super.onDelete() just before exiting or
 * arrange for super.onDelete() to be called at some later time.
 */
public interface NamedObjectOps  {

    /**
     * Determine if an object is interned in a object namer's tables.
     * @return true if the object is interned; false if not
     */
    boolean isInterned();

    /**
     * Get an object's name.
     * @return the name of the object
     */
    String getName();

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
    boolean canDelete();

    /**
     * Delete an object.
     * An object can only be deleted once.  If this method returns true,
     * the object (if interned) will have been removed from the object namer
     * tables.
     * <P>
     * The implementations provided by
     * {@link DefaultNamedObject DefaultNamedObect} and generated because of
     * a {@link org.bzdev.obnaming.annotations.NamedObject @NamedObject}
     * annotation provide a protected method named onDelete. A
     * subclass that overrides onDelete() must call the onDelete
     * method of its superclass after it's onDelete method has been
     * called and any cleanup actions performed.  In some cases, this
     * may happen at a later time (e.g., if a thread is used for some
     * of the cleanup operations or if it is otherwise necessary to
     * wait).
     *
     * @return true if the deletion request was accepted; false otherwise
     */
    boolean delete();

    /**
     * Determine if an object has been deleted.
     * An object is deleted if the method delete() has been
     * called and returned true and the deletion is not pending.
     * @return true if deleted; false otherwise
     */
    boolean isDeleted();

    /**
     * Determine if an object is being deleted.
     * An deletion is pending if the method delete() has been
     * called and returned true but the deletion has not been
     * completed.
     * @return true if deletion is pending; false otherwise
     */
    boolean deletePending();
}

//  LocalWords:  javadoc javdocs getObjectNamer onDelete namer
//  LocalWords:  NamedObject namer's Subclasses canDelete superclass
//  LocalWords:  superclasses DefaultNamedObject DefaultNamedObect
