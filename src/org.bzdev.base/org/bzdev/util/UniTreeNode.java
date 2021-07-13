package org.bzdev.util;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.NoSuchElementException;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

//@exbundle org.bzdev.util.lpack.Util


/**
 * Class used to construct a tree of elements traversable in one direction.
 * The tree can be traversed only from a leaf or
 * intermediate node towards the root.  As a result, the tree is
 * thread safe without the need for locking (a UniTreeNode's fields
 * are modified only in constructors), resulting in a light-weight
 * implementation.
 * <P>
 * Each leaf's view of the tree is basically a unidirectional link list,
 * but where multiple lists may share common nodes.  There is no operation
 * to remove items from the tree: memory will be reclaimed by the garbage
 * collector once references to a a UniTreeNode are dropped.
 */
public class UniTreeNode<T> implements Iterable<T> {

    static String errorMsg(String key, Object... args) {
	return UtilErrorMsg.errorMsg(key, args);
    }

    UniTreeNode<T> parent = null;
    T element;

    /**
     * Constructor.
     * @param element the element associated with a new root node
     */
    public UniTreeNode(T element) {
	this.element = element;
    }

    /**
     * Constructor that adds a new node to an existing tree.
     * @param element the element associated with a new root node
     * @param parent an existing tree; null to create a new tree
     */
    private UniTreeNode(T element, UniTreeNode<T> parent) {
	this.element = element;
	this.parent = parent;
    }

    /**
     * Add a node to this tree.
     * @parem element  the element to add to this tree
     */
    public UniTreeNode<T> add(T element) {
	return new UniTreeNode<T>(element, this);
    }

    /**
     * Add a node to a tree.
     * @param element  the element to add to this tree
     * @param tree an existing tree
     */
    public static <T> UniTreeNode<T> addTo(T element, UniTreeNode<T> tree) {
	return new UniTreeNode<T>(element, tree);
    }

    /**
     * Get the element associated with a node.
     * @return the element associated with this node
     */
    public T getElement() {
	return element;
    }

    /**
     * Get the parent of this node.
     * @return the next node
     */
    public UniTreeNode<T> parent() {
	return parent;
    }

    @Override
    public Iterator<T> iterator() {
	return new Iterator<T>() {
	    UniTreeNode<T> node = UniTreeNode.this;

	    @Override
	    public boolean hasNext() {
		return node != null;
	    }
	    
	    @Override
	    public T next() {
		if (node == null) {
		    String msg = errorMsg("noMoreIterations");
		    throw new NoSuchElementException(msg);
		}
		T result = node.getElement();
		node = node.parent;
		return result;
	    }
	};
    }

    @Override
    public Spliterator<T> spliterator() {
	return Spliterators.spliteratorUnknownSize(iterator(),
						   Spliterator.IMMUTABLE);
    }

    /**
     * Return a stream of the elements from this leaf to the root of
     * the tree.
     * @return a stream
     */
    public Stream<T> stream() {
	return StreamSupport.stream(spliterator(), false);
    }

    /**
     * Return a parallel stream of the elements from this leaf to the
     * root of the tree.
     * @return a parallel stream
     */
    public Stream<T> parallelStream() {
	return StreamSupport.stream(spliterator(), true);
    }

}

//  LocalWords:  exbundle traversable UniTreeNode's UniTreeNode
//  LocalWords:  noMoreIterations
