$(package)
import org.bzdev.util.ClassArraySorter;
import org.bzdev.util.ClassArraySorter.Key;
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
    public static interface Caller {
	$(baseReturnType) call($(baseType) obj$(callArgs:endCallArgs), $(baseArgType) arg$(ind)$(endCallArgs))$(callThrowables:endCallThrowables)
	    throws $(callThrowablesList:endCallThrowablesList)$(delim)$(throwable)$(endCallThrowablesList)$(endCallThrowables);
    }$(usesLock:endUsesLock)
    static $(lockTypeString) lock = new $(lockTypeString)();$(endUsesLock)
    static $(helperName) helper = new $(helperName)();
    static Map<Class,$(simpleHelperName)> map = new HashMap<Class,$(simpleHelperName)>();
    Map<Key,$(simpleHelperName).Caller> cmap = new HashMap<Key,$(simpleHelperName).Caller>();
    ClassArraySorter cas = new ClassArraySorter();
    LinkedList<Key> keyList = null;
    static final int LIMIT = $(limitFactor) * DMethodParameters.getDefaultCacheLimit();
    $(!Note: clist is used to age out old entries because cmap contains
	     permanent entries that must not be aged out, preventing
	     the use of a LinkedHashMap for this purpose.)
    LinkedList<Key> clist = new LinkedList<Key>();
    protected void addDispatch(Class[] types, $(simpleHelperName).Caller caller) {$(usesLock:endUsesLock)
	Lock lock = this.$(wLockString);$(endUsesLock)
	try {
	    Key key = new Key(types);
	    $(usesLock:endUsesLock)$(trace:endTrace)
	    Appendable out = DMethodParameters.getTracingOutput();
	    try {
	        out.append(getClass().getName() + ": adding " + key + "\n");
	    } catch (IOException eio) {}
	    $(endTrace)
	    lock.lock();$(endUsesLock)
	    cmap.put(key, caller);
	    cas.addKey(key);
	} finally {$(usesLock:endUsesLock)
	    lock.unlock();$(endUsesLock)
	}
    }
    protected void addDispatchComplete() {
	try {$(usesLock:endUsesLock)
	    lock.lock();$(endUsesLock)
	    keyList = cas.createList();
	} finally {$(usesLock:endUsesLock)
	    lock.unlock();$(endUsesLock)
        }
    }
    Class<$(baseType)> baseType = $(baseType).class;

    static Class<?>[] mbaseType = {$(baseArgTypeList:endBaseArgTypeList)$(delim)$(baseArgType).class$(endBaseArgTypeList)};

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

    protected $(baseReturnType) dispatch($(baseType) obj$(baseArgTypeList:endBaseArgTypeList), $(baseArgType) arg$(ind)$(endBaseArgTypeList))$(callThrowables:endCallThrowables)
	    throws $(callThrowablesList:endCallThrowablesList)$(delim)$(throwable)$(endCallThrowablesList)$(endCallThrowables)
    {$(usesLock:endUsesLock)
	Lock lock = this.$(rLockString);$(endUsesLock)
	Class clazz = obj.getClass();
	Class[] mtypes = {
	    $(orderList:endOrderList)((arg$(ind) == null)? $(baseArgType).class: arg$(ind).getClass())$(tdelim)$(endOrderList)
	};
       Class[] origMtypes = new Class[mtypes.length];
        System.arraycopy(mtypes,0,origMtypes,0,mtypes.length);
        Key key = new Key(mtypes);
        Key origKey = new Key(origMtypes);
	Class origClass = clazz;
	$(simpleHelperName) chelper = map.get(clazz);
        boolean entryMissing = false;
        boolean entryFound = false;
        int[] order = {
	    $(orderList:endOrderList)$(ind)$(tdelim)$(endOrderList)
	};
	int ind = order.length - 1;
	$(simpleHelperName).Caller ch = null;$(trace:endTrace)
	Appendable out = DMethodParameters.getTracingOutput();
	$(endTrace)
	try {$(usesLock:endUsesLock)
	    lock.lock();$(endUsesLock)
	    for (;;) {
                if (chelper != null) {
			$(trace:endTrace)
			Key chkey = key;
			try {
			    out.append(clazz.getName() + ":  trying "
				       + key + " using "
				       + chelper.getClass().getName()
				       + "\n");
			} catch (IOException eio) {}
			$(endTrace)
                        ch = chelper.cmap.get(key);
			if (ch == null) {
			   entryMissing = true;
			   for (Key k: chelper.keyList) {
			      if (k.isAssignableFrom(key)) {
			         ch = chelper.cmap.get(k);$(trace:endTrace)
				 chkey = k;
				 $(endTrace)
				 break;
			      }
			   }
			}
                        if (ch != null) {/*$(usesLock:endUsesLock)
			    lock.unlock();*/$(endUsesLock)$(trace:endTrace)
			    try {
			        out.append(clazz.getName() + ": found "
					   + chkey + " using "
					   + chelper.getClass().getName()
					   + "\n");
			    } catch (IOException eio) {}
			    $(endTrace)
			    entryFound = true;
			    break;
			}
	        }
	        Class newclazz = clazz.getSuperclass();
	        if (newclazz == null || !baseType.isAssignableFrom(newclazz)) {$(usesLock:endUsesLock)
		    lock.unlock();$(endUsesLock)
		    break;
		}
		$(trace:endTrace)
		try {
		    out.append(clazz.toString() + ": changing search class to "
			       + newclazz.toString() + "\n");
		} catch (IOException eio) {}
		$(endTrace)
		clazz = newclazz;
		chelper = map.get(clazz);
	    }
	} finally {
	    if (entryFound) {$(usesLock:endUsesLock)
	       try {$(endUsesLock)
	         if (entryMissing) {$(usesLock:endUsesLock)
		    if(lock != this.$(wLockString)) {
		        lock.unlock();
			lock = this.$(wLockString);
			lock.lock();
		    }$(endUsesLock)
		    chelper = map.get(origClass);
		    if (chelper != null) {
			if (chelper.clist.size() == LIMIT) {
			    Key k = chelper.clist.remove();
			    $(trace:endTrace)
			    try {
				out.append(origClass.getName()
					   + ": uncaching " + k
					   + " using "
					   + chelper.getClass().getName()
					   + "\n");
			    } catch (IOException eio) {}
			$(endTrace)
			    chelper.cmap.remove(k);
			}
			$(trace:endTrace)
			try {
			    out.append(origClass.getName() + ": caching "
				       + origKey + " using "
				       + chelper.getClass().getName() + "\n");
			} catch (IOException eio) {}
			$(endTrace)
			chelper.cmap.put(origKey, ch);
			chelper.clist.add(origKey);
		    }
	         }$(usesLock:endUsesLock)
	      } finally {
		lock.unlock();
	      }$(endUsesLock)
	      $(resultEqual)ch.call(obj$(baseArgTypeList:endBaseArgTypeList), arg$(ind)$(endBaseArgTypeList));
	      $(returnExpr);
	    }
	}
	throw new org.bzdev.lang.MethodNotPresentException();
    }
}
