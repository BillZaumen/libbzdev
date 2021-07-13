package org.bzdev.util;
import java.security.*;

/**
 * Permission to allow an instance of ExpressionParser to be created.
 * This permission is checked in the constructors
 * {@link ExpressionParser#ExpressionParser(Class...)}
), and the constructor
 * {@link ExpressionParser#ExpressionParser(Class[],Class[],Class[],Class[],Class[])}
 * when any of its arguments is a array whose length is larger than 0.
 *
 */
public class ExpressionParserPermission extends BasicPermission {
    /**
     * Constructor.
     * @param name the fully-qualified name org.bzdev.util.ExpressionParser
     */
    public ExpressionParserPermission(String name) {
        super(name);
    }
    /**
     * Constructor with actions.
     * @param name the fully-qualified name org.bzdev.util.ExpressionParser
     * @param actions a parameter that is ignored in this case although
     *        the constructor is needed
     */
    public ExpressionParserPermission(String name, String actions) {
        super(name, actions);
    }
}

//  LocalWords:  ExpressionParser
