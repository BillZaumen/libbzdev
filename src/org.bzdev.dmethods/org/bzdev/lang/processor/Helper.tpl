$(package)
import org.bzdev.util.ClassSorter;
import org.bzdev.lang.DMethodParameters;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
// import javax.annotation.processing.Generated;

// @Generated(value="$(generator)", date="$(date)")
/**
 * Dynamic-method helper.
 */
$(annotation)public class $(simpleHelperName) {
    static public interface Caller {
       $(baseReturnType) call($(baseType) obj, $(baseArgType) arg)$(callThrowables:endCallThrowables)
	    throws $(callThrowablesList:endCallThrowablesList)$(delim)$(throwable)$(endCallThrowablesList)$(endCallThrowables);
    
    }$(usesLock:endUsesLock)
    static $(lockTypeString) lock = new $(lockTypeString)();$(endUsesLock)
    static $(helperName) helper = new $(helperName)();
    static Map<Class,$(simpleHelperName)> map = new HashMap<Class,$(simpleHelperName)>();
    Map<Class,$(simpleHelperName).Caller> cmap = new HashMap<Class,$(simpleHelperName).Caller>();
    ClassSorter sorter = new ClassSorter();
    LinkedList<Class<?>> classList = null;
    static final int LIMIT = $(limitFactor) * DMethodParameters.getDefaultCacheLimit();
    $(!Note: clist is used to age out old entries because cmap contains
	     permanent entries that must not be aged out, preventing
	     the use of a LinkedHashMap for this purpose.)
    LinkedList<Class<?>> clist = new LinkedList<Class<?>>();
    protected void addDispatch(Class<?> type, $(simpleHelperName).Caller caller)
    {$(usesLock:endUsesLock)
	Lock lock = this.$(wLockString);$(endUsesLock)
	try {$(usesLock:endUsesLock)
	    lock.lock();$(endUsesLock)$(trace:endTrace)
	    Appendable out = DMethodParameters.getTracingOutput();
	    try {
		out.append(getClass().getName() + ": adding " + type.getName()
			   + "\n");
	    } catch (IOException eio) {}
	    $(endTrace)
	    cmap.put(type, caller);
	    sorter.addKey(type);
        } finally {$(usesLock:endUsesLock)
	    lock.unlock();$(endUsesLock)
        }
    }
    protected void addDispatchComplete() {
	try {$(usesLock:endUsesLock)
	    lock.lock();$(endUsesLock)
	    classList = sorter.createList();
	} finally {$(usesLock:endUsesLock)
	    lock.unlock();$(endUsesLock)
        }
    }
    Class<$(baseType)> baseType = $(baseType).class;
    Class<$(baseArgType)> mbaseType = $(baseArgType).class;
    void register(Class clazz, $(simpleHelperName) h) {$(usesLock:endUsesLock)
        Lock lock = this.$(wLockString);$(endUsesLock)
	try {$(usesLock:endUsesLock)
	    lock.lock();$(endUsesLock)
	    $(simpleHelperName).map.put(clazz, h);
	} finally {$(usesLock:endUsesLock)
	    lock.unlock();$(endUsesLock)
	}
    }
    protected void register(Class clazz) {
	helper.register(clazz, this);
    }
    public final static $(simpleHelperName) getHelper() {
	return helper;
    }
    protected $(baseReturnType) dispatch($(baseType) obj, $(baseArgType) msg)$(callThrowables:endCallThrowables)
	    throws $(callThrowablesList:endCallThrowablesList)$(delim)$(throwable)$(endCallThrowablesList)$(endCallThrowables)
    {$(usesLock:endUsesLock)
	Lock lock = this.$(rLockString);$(endUsesLock)
	Class clazz = obj.getClass();
	Class mclazz = (msg == null)? $(baseArgType).class: msg.getClass();
	Class origMclazz = mclazz;
	$(simpleHelperName) chelper = map.get(clazz);
	Class origClass = clazz;
	$(simpleHelperName) origChelper = chelper;
	boolean entryMissing = false;
	boolean entryFound = false;
	$(simpleHelperName).Caller ch = null;$(trace:endTrace)
	Appendable out = DMethodParameters.getTracingOutput();
	$(endTrace)
	try {$(usesLock:endUsesLock)
	    lock.lock();$(endUsesLock)
	    if (chelper != null) {
		  for (;;) {$(trace:endTrace)
		    Class<?> chclass = mclazz;
		    try {
		        out.append(clazz.getName() + ": trying "
				   + mclazz.getName() + " using "
				   + chelper.getClass().getName() + "\n");
		    } catch (IOException eio){}
	            $(endTrace)
		    ch = chelper.cmap.get(mclazz);
		    if (ch == null) {
		       entryMissing = true;
		       for (Class<?> c: chelper.classList) {
		          if (c.isAssignableFrom(mclazz)) {
			     ch = chelper.cmap.get(c);$(trace:endTrace)
			     chclass = c;
			     $(endTrace)
			     break;
			  }
		       }
		    }
		    if (ch != null) {$(trace:endTrace)
			try {
			    out.append(clazz.getName() + ": found "
				       + chclass.getName() + " using "
				       + chelper.getClass().getName() + "\n");
			} catch (IOException eio){}
	                $(endTrace)
			entryFound = true;
			break;
		    }
		    Class newclazz = clazz.getSuperclass();
		    if (newclazz == null ||
		        !baseType.isAssignableFrom(newclazz)){$(usesLock:endUsesLock)
		         lock.unlock();$(endUsesLock)
			 break;
		    }$(trace:endTrace)
		    try {
		         out.append(clazz.getName()
				    + ": changing search class to "
				    + newclazz.getName() + "\n");
		    } catch (IOException eio) {}
		    $(endTrace)
		    clazz = newclazz;
		    chelper = map.get(clazz);
		  }
	    }
	} finally {
	    if (entryFound) {$(usesLock:endUsesLock)
	      try {$(endUsesLock)
	        if (entryMissing) {$(usesLock:endUsesLock)
		    if (lock != this.$(wLockString)) {
		       lock.unlock();
		       lock = this.$(wLockString);
		       lock.lock();
		    }$(endUsesLock)
		    if (origChelper != null) {
		        if (origChelper.clist.size() == LIMIT) {
			  Class<?> c = origChelper.clist.remove();
			    $(trace:endTrace)
			    try {
				out.append(origClass.getName()
					   + ": uncaching " + c.getName()
					   + " using "
					   + origChelper.getClass().getName()
					   + "\n");
			    } catch (IOException eio) {}
			    $(endTrace)
			  origChelper.cmap.remove(c);
			}
			$(trace:endTrace)
			try {
			    out.append(origClass.getName() + ": caching "
				       + origMclazz.getName() + " using "
				       + origChelper.getClass().getName()
				       + "\n");
			} catch (IOException eio) {}
			$(endTrace)
			origChelper.cmap.put(origMclazz, ch);
			origChelper.clist.add(origMclazz);
		    }
	        }$(usesLock:endUsesLock)
	      } finally {
		lock.unlock();
	      }$(endUsesLock)
	      $(resultEqual)ch.call(obj, msg);
	      $(returnExpr);
	    }
	}
	throw new org.bzdev.lang.MethodNotPresentException();
    }    
}
