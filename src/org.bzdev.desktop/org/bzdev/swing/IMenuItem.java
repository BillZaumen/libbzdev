package org.bzdev.swing;
import javax.swing.Action;
import javax.swing.JMenuItem;
import javax.swing.Icon;
import java.util.Locale;
import java.util.ResourceBundle;
import javax.swing.JComponent;

//@exbundle org.bzdev.swing.lpack.Swing

/**
 * Menu with internationalization support.
 * The constructors take the name of a resource bundle and
 * keys used to look up menu names and icons from the bundle.
 * Changing the locale then automatically changes the icon
 * and/or menu name.
 */
public class IMenuItem extends JMenuItem {
    static String errorMsg(String key, Object... args) {
	return SwingErrorMsg.errorMsg(key, args);
    }

    String bundleName = null;
    String textKey = null;
    String iconKey = null;
    String selectedIconKey = null;
    String disabledIconKey = null;
    String disabledSelectedIconKey = null;
    String rolloverIconKey = null;
    String rolloverSelectedIconKey = null;
    String pressedIconKey = null;

    String mnemonicKey = null;


    @Override
    public void setLocale(Locale locale) {
	super.setLocale(locale);
	if (textKey != null) {
	    setTextKey(textKey);
	}
	if (iconKey != null) {
	    setIconKey(iconKey);
	}
	if (selectedIconKey != null) {
	    setSelectedIconKey(selectedIconKey);
	}
	if (disabledIconKey != null) {
	    setDisabledIconKey(disabledIconKey);
	}
	if (disabledSelectedIconKey != null) {
	    setDisabledSelectedIconKey(disabledSelectedIconKey);
	}
	if (rolloverIconKey != null) {
	    setRolloverIconKey(rolloverIconKey);
	}
	if (rolloverSelectedIconKey != null) {
	    setRolloverSelectedIconKey(rolloverSelectedIconKey);
	}
	if (pressedIconKey != null) {
	    setPressedIconKey(pressedIconKey);
	}
	if (mnemonicKey != null) {
	    setMnemonicKey(mnemonicKey);
	}
    }

    /**
     * Constructor.
     */
    public IMenuItem() {
	super();
    }

    /**
     * Constructor given an Action.
     * @param a the action that determine this menu item's appearance
     *        and  behavior
     */
    public IMenuItem(Action a) {
	super(a);
    }

    /**
     * Constructor given an Icon
     * @param icon the icon that represents this menu item.
     */
    public IMenuItem(Icon icon) {
	super(icon);
    }

    /**
     * Constructor given specified text.
     * @param text the text that labels this menu item
     */
    public IMenuItem(String text) {
	super(text);
    }

    private static String getTextFromBundle(Locale locale, String bundleName,
				     String key)
	throws IllegalArgumentException
    {
	ResourceBundle bundle = 
	    ResourceBundle.getBundle(bundleName, locale);
	if (bundle == null) {
	    throw new IllegalArgumentException
		(errorMsg("bundleNotFound", bundleName));
	}
	Object object = bundle.getObject(key);
	if (object instanceof String) return (String) object;
	return null;
    }

    /**
     * Constructor given a bundle pathname and a key for text.
     * @param bundleName the pathname of a resource bundle
     * @param key the resource bundle's key for the text labeling this
     *        menu item
     * @exception IllegalArgumentException the path name for a resource
     *        bundle pointed to a resource that does not exist
     */
    public IMenuItem(String bundleName, String key)
	throws IllegalArgumentException
    {
	this(JComponent.getDefaultLocale(), bundleName, key);
    }

    /**
     * Constructor given a locale, a bundle pathname and a key for text.
     * @param locale a locale
     * @param bundleName the pathname of a resource bundle
     * @param key the resource bundle's key for the text labeling this
     *        menu item
     * @exception IllegalArgumentException the path name for a resource
     *        bundle pointed to a resource that does not exist
     */
    public IMenuItem(Locale locale, String bundleName, String key)
	throws IllegalArgumentException
    {
	super(getTextFromBundle(locale, bundleName, key));
	super.setLocale(locale);
	this.bundleName = bundleName;
	this.textKey = key;
    }

    /**
     * Constructor given a bundle pathname, a key for text, and a key
     * for either an icon or a mnemonic.
     * @param bundleName the pathname of a resource bundle
     * @param key1 the resource bundle's key for the text labeling this
     *        menu item
     * @param key2 the resource bundle's key for either an icon or for
     *        a mnemonic
     * @exception IllegalArgumentException the path name for a resource
     *        bundle pointed to a resource that does not exist
     */
    public IMenuItem(String bundleName, String key1, String key2)
	throws IllegalArgumentException
    {
	this(JComponent.getDefaultLocale(), bundleName, key1, key2);
    }

    /**
     * Constructor given a locale, a bundle pathname, a key for text, and a key
     * for either an icon or a mnemonic.
     * The resource bundle is expected to provide an object that is
     * an Icon when key2 refers to an icon or an Integer when key2 refers
     * to a mnemonic.
     * @param locale the locale
     * @param bundleName the pathname of a resource bundle
     * @param key1 the resource bundle's key for the text labeling this
     *        menu item
     * @param key2 the resource bundle's key for either an icon or for
     *        a mnemonic
     * @exception IllegalArgumentException the path name for a resource
     *        bundle pointed to a resource that does not exist
     */
    public IMenuItem(Locale locale, String bundleName, String key1,
		     String key2)
	throws IllegalArgumentException
    {
	super(getTextFromBundle(locale, bundleName, key1), null);
	super.setLocale(locale);
	this.bundleName = bundleName;
	ResourceBundle bundle = 
	    ResourceBundle.getBundle(bundleName, locale);
	if (key2 != null) {
	    Object object = bundle.getObject(key2);
	    if (object instanceof Icon) {
		Icon icon = (Icon)object;
		super.setIcon(icon);
		iconKey = key2;
	    } else if (object instanceof Integer) {
		int mnemonic = ((Integer)object).intValue();
		super.setMnemonic(mnemonic);
		mnemonicKey = key2;
	    }
	}
    }

    @Override
    public void setText(String text) {
	textKey = null;
	super.setText(text);
    }


    /**
     * Set a menu item's text by key instead of by value.
     * @param key the key used by a resource bundle to find the
     *        text used to label this menu item
     */
    public void setTextKey(String key) {
	if (bundleName != null && textKey != null) {
	    ResourceBundle bundle = 
		ResourceBundle.getBundle(bundleName, getLocale());
	    if (bundle != null) {
		Object object = bundle.getObject(key);
		if (object != null) {
		    if (object instanceof String) {
			super.setText((String)object);
		    } else {
			throw new IllegalArgumentException
			    (errorMsg("keyValueWrongType", key));
		    }
		} else {
		    super.setText(null);
		}
	    } else {
		throw new IllegalStateException
		    (errorMsg("bundleNotFound", bundleName));
	    }
	}
	textKey = key;
    }

    @Override
    public void setIcon(Icon icon) {
	iconKey = null;
	super.setIcon(icon);
    }

    /**
     * Set a menu item's icon by key instead of by value.
     * @param key the key used by a resource bundle to find the
     *        icon for this menu item
     * @see #setIcon(Icon)
     */
    public void setIconKey(String key) {
	if (bundleName != null && key != null) {
	    ResourceBundle bundle = 
		ResourceBundle.getBundle(bundleName, getLocale());
	    if (bundle != null) {
		Object object = bundle.getObject(key);
		if (object != null) {
		    if (object instanceof Icon) {
			super.setIcon((Icon)object);
		    } else {
			throw new IllegalArgumentException
			    (errorMsg("keyValueWrongType", key));
		    }
		} else {
		    super.setIcon(null);
		}
	    } else {
		throw new IllegalStateException
		    (errorMsg("bundleNotFound", bundleName));
	    }
	}
	iconKey = key;
    }

    @Override
    public void setSelectedIcon(Icon icon) {
	selectedIconKey = null;
	super.setSelectedIcon(icon);
    }

    /**
     * Set a menu item's selected icon by key instead of by value.
     * @param key the key used by a resource bundle to find the
     *        icon for this menu item
     * @see #setSelectedIcon(Icon)
     */
    public void setSelectedIconKey(String key) {
	if (bundleName != null && key != null) {
	    ResourceBundle bundle = 
		ResourceBundle.getBundle(bundleName, getLocale());
	    if (bundle != null) {
		Object object = bundle.getObject(key);
		if (object != null) {
		    if (object instanceof Icon) {
			super.setSelectedIcon((Icon)object);
		    } else {
			throw new IllegalArgumentException
			    (errorMsg("keyValueWrongType", key));
		    }
		} else {
		    super.setSelectedIcon(null);
		}
	    } else {
		throw new IllegalStateException
		    (errorMsg("bundleNotFound", bundleName));
	    }
	}
	selectedIconKey = key;
    }

    @Override
    public void setDisabledIcon(Icon icon) {
	disabledIconKey = null;
	super.setDisabledIcon(icon);
    }

    /**
     * Set a menu item's disabled icon by key instead of by value.
     * @param key the key used by a resource bundle to find the
     *        icon for this menu item
     * @see #setDisabledIcon(Icon)
     */
    public void setDisabledIconKey(String key) {
	if (bundleName != null && key != null) {
	    ResourceBundle bundle = 
		ResourceBundle.getBundle(bundleName, getLocale());
	    if (bundle != null) {
		Object object = bundle.getObject(key);
		if (object != null) {
		    if (object instanceof Icon) {
			super.setDisabledIcon((Icon)object);
		    } else {
			throw new IllegalArgumentException
			    (errorMsg("keyValueWrongType", key));
		    }
		} else {
		    super.setDisabledIcon(null);
		}
	    } else {
		throw new IllegalStateException
		    (errorMsg("bundleNotFound", bundleName));
	    }
	}
	disabledIconKey = key;
    }

    @Override
    public void setDisabledSelectedIcon(Icon icon) {
	disabledSelectedIconKey = null;
	super.setDisabledSelectedIcon(icon);
    }

    /**
     * Set a menu item's disabled-selected icon by key instead of by value.
     * @param key the key used by a resource bundle to find the
     *        icon for this menu item
     * @see #setDisabledSelectedIcon(Icon)
     */
    public void setDisabledSelectedIconKey(String key) {
	if (bundleName != null && key != null) {
	    ResourceBundle bundle = 
		ResourceBundle.getBundle(bundleName, getLocale());
	    if (bundle != null) {
		Object object = bundle.getObject(key);
		if (object != null) {
		    if (object instanceof Icon) {
			super.setDisabledSelectedIcon((Icon)object);
		    } else {
			throw new IllegalArgumentException
			    (errorMsg("keyValueWrongType", key));
		    }
		} else {
		    super.setDisabledSelectedIcon(null);
		}
	    } else {
		throw new IllegalStateException
		    (errorMsg("bundleNotFound", bundleName));
	    }
	}
	disabledSelectedIconKey = key;
    }

    @Override
    public void setRolloverIcon(Icon icon) {
	rolloverIconKey = null;
	super.setRolloverIcon(icon);
    }

    /**
     * Set a menu item's rollover icon by key instead of by value.
     * @param key the key used by a resource bundle to find the
     *        icon for this menu item
     * @see #setRolloverIcon(Icon)
     */
    public void setRolloverIconKey(String key) {
	if (bundleName != null && key != null) {
	    ResourceBundle bundle = 
		ResourceBundle.getBundle(bundleName, getLocale());
	    if (bundle != null) {
		Object object = bundle.getObject(key);
		if (object != null) {
		    if (object instanceof Icon) {
			super.setRolloverIcon((Icon)object);
		    } else {
			throw new IllegalArgumentException
			    (errorMsg("keyValueWrongType", key));
		    }
		} else {
		    super.setRolloverIcon(null);
		}
	    } else {
		throw new IllegalStateException
		    (errorMsg("bundleNotFound", bundleName));
	    }
	}
	rolloverIconKey = key;
    }

    @Override
    public void setRolloverSelectedIcon(Icon icon) {
	rolloverSelectedIconKey = null;
	super.setRolloverSelectedIcon(icon);
    }

    /**
     * Set a menu item's rollover-selected icon by key instead of by value.
     * @param key the key used by a resource bundle to find the
     *        icon for this menu item
     * @see #setRolloverSelectedIcon(Icon)
     */
    public void setRolloverSelectedIconKey(String key) {
	if (bundleName != null && key != null) {
	    ResourceBundle bundle = 
		ResourceBundle.getBundle(bundleName, getLocale());
	    if (bundle != null) {
		Object object = bundle.getObject(key);
		if (object != null) {
		    if (object instanceof Icon) {
			super.setRolloverSelectedIcon((Icon)object);
		    } else {
			throw new IllegalArgumentException
			    (errorMsg("keyValueWrongType", key));
		    }
		} else {
		    super.setRolloverSelectedIcon(null);
		}
	    } else {
		throw new IllegalStateException
		    (errorMsg("bundleNotFound", bundleName));
	    }
	}
	rolloverSelectedIconKey = key;
    }

    @Override
    public void setPressedIcon(Icon icon) {
	super.setPressedIcon(icon);
	pressedIconKey = null;
    }

    /**
     * Set a menu item's pressed icon by key instead of by value.
     * @param key the key used by a resource bundle to find the
     *        icon for this menu item
     * @see #setPressedIcon(Icon)
     */
    public void setPressedIconKey(String key) {
	if (bundleName != null && key != null) {
	    ResourceBundle bundle = 
		ResourceBundle.getBundle(bundleName, getLocale());
	    if (bundle != null) {
		Object object = bundle.getObject(key);
		if (object != null) {
		    if (object instanceof Icon) {
			super.setPressedIcon((Icon)object);
		    } else {
			throw new IllegalArgumentException
			    (errorMsg("keyValueWrongType", key));
		    }
		} else {
		    super.setPressedIcon(null);
		}
	    } else {
		throw new IllegalStateException
		    (errorMsg("bundleNotFound", bundleName));
	    }
	}
	pressedIconKey = key;
    }


    @Override
    public void setMnemonic(int mnemonic) {
	mnemonicKey = null;
	super.setMnemonic(mnemonic);
    }

    /**
     * Set a menu item's mnemonic code by key instead of by value.
     * @param key the key used by a resource bundle to find the
     *        mnemonic code for this menu item
     * @see #setMnemonic(int)
     */
    public void setMnemonicKey(String key) {
	if (bundleName != null & key != null) {
	    ResourceBundle bundle = 
		ResourceBundle.getBundle(bundleName, getLocale());
	    if (bundle != null) {
		Object object = bundle.getObject(key);
		if (object != null) {
		    if (object instanceof Integer) {
			super.setMnemonic(((Integer)object).intValue());
		    } else {
			throw new IllegalArgumentException
			    (errorMsg("keyValueWrongType", key));
		    }
		} else {
		    throw new IllegalArgumentException
			(errorMsg("keyValueMissing", key));
		}
	    } else {
		throw new IllegalStateException
		    (errorMsg("bundleNotFound", bundleName));
	    }
	}
	mnemonicKey = key;
    }
}

//  LocalWords:  exbundle bundleNotFound pathname bundleName setIcon
//  LocalWords:  IllegalArgumentException keyValueWrongType
//  LocalWords:  setSelectedIcon setDisabledIcon setRolloverIcon
//  LocalWords:  setDisabledSelectedIcon setRolloverSelectedIcon
//  LocalWords:  setPressedIcon setMnemonic keyValueMissing
