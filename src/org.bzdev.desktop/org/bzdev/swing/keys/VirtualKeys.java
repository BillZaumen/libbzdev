package org.bzdev.swing.keys;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.ResourceBundle;
import org.bzdev.util.SafeFormatter;

//@exbundle org.bzdev.swing.keys.lpack.Keys

/**
 * Class to look up key events by name.
 * The class {@link java.awt.event.KeyEvent} defines a large number of
 * static int constants with names that begin with the string "VK_".
 * This class provides a single method that allows these constants
 * to be looked up at runtime by name.
 * <P>
 * One use of this class is for localization, where one might want
 * to include key names in a properties file associated with a resource
 * bundle.
 */
public class VirtualKeys {
    private static ResourceBundle exbundle = ResourceBundle.getBundle
	("org.bzdev.swing.keys.lpack.Keys");

    static String errorMsg(String key, Object... args) {
	return (new SafeFormatter()).format(exbundle.getString(key), args)
	    .toString();
    }

    static HashMap<String,Integer> map = new HashMap<>(190*2);
    static {
 	map.put("VK_0", KeyEvent.VK_0);
 	map.put("VK_1", KeyEvent.VK_1);
 	map.put("VK_2", KeyEvent.VK_2);
 	map.put("VK_3", KeyEvent.VK_3);
 	map.put("VK_4", KeyEvent.VK_4);
 	map.put("VK_5", KeyEvent.VK_5);
 	map.put("VK_6", KeyEvent.VK_6);
 	map.put("VK_7", KeyEvent.VK_7);
 	map.put("VK_8", KeyEvent.VK_8);
 	map.put("VK_9", KeyEvent.VK_9);
 	map.put("VK_A", KeyEvent.VK_A);
 	map.put("VK_ACCEPT", KeyEvent.VK_ACCEPT);
 	map.put("VK_ADD", KeyEvent.VK_ADD);
 	map.put("VK_AGAIN", KeyEvent.VK_AGAIN);
 	map.put("VK_ALL_CANDIDATES", KeyEvent.VK_ALL_CANDIDATES);
 	map.put("VK_ALPHANUMERIC", KeyEvent.VK_ALPHANUMERIC);
 	map.put("VK_ALT", KeyEvent.VK_ALT);
 	map.put("VK_ALT_GRAPH", KeyEvent.VK_ALT_GRAPH);
 	map.put("VK_AMPERSAND", KeyEvent.VK_AMPERSAND);
 	map.put("VK_ASTERISK", KeyEvent.VK_ASTERISK);
 	map.put("VK_AT", KeyEvent.VK_AT);
 	map.put("VK_B", KeyEvent.VK_B);
 	map.put("VK_BACK_QUOTE", KeyEvent.VK_BACK_QUOTE);
 	map.put("VK_BACK_SLASH", KeyEvent.VK_BACK_SLASH);
 	map.put("VK_BACK_SPACE", KeyEvent.VK_BACK_SPACE);
 	map.put("VK_BEGIN", KeyEvent.VK_BEGIN);
 	map.put("VK_BRACELEFT", KeyEvent.VK_BRACELEFT);
 	map.put("VK_BRACERIGHT", KeyEvent.VK_BRACERIGHT);
 	map.put("VK_C", KeyEvent.VK_C);
 	map.put("VK_CANCEL", KeyEvent.VK_CANCEL);
 	map.put("VK_CAPS_LOCK", KeyEvent.VK_CAPS_LOCK);
 	map.put("VK_CIRCUMFLEX", KeyEvent.VK_CIRCUMFLEX);
 	map.put("VK_CLEAR", KeyEvent.VK_CLEAR);
 	map.put("VK_CLOSE_BRACKET", KeyEvent.VK_CLOSE_BRACKET);
 	map.put("VK_CODE_INPUT", KeyEvent.VK_CODE_INPUT);
 	map.put("VK_COLON", KeyEvent.VK_COLON);
 	map.put("VK_COMMA", KeyEvent.VK_COMMA);
 	map.put("VK_COMPOSE", KeyEvent.VK_COMPOSE);
 	map.put("VK_CONTEXT_MENU", KeyEvent.VK_CONTEXT_MENU);
 	map.put("VK_CONTROL", KeyEvent.VK_CONTROL);
 	map.put("VK_CONVERT", KeyEvent.VK_CONVERT);
 	map.put("VK_COPY", KeyEvent.VK_COPY);
 	map.put("VK_CUT", KeyEvent.VK_CUT);
 	map.put("VK_D", KeyEvent.VK_D);
 	map.put("VK_DEAD_ABOVEDOT", KeyEvent.VK_DEAD_ABOVEDOT);
 	map.put("VK_DEAD_ABOVERING", KeyEvent.VK_DEAD_ABOVERING);
 	map.put("VK_DEAD_ACUTE", KeyEvent.VK_DEAD_ACUTE);
 	map.put("VK_DEAD_BREVE", KeyEvent.VK_DEAD_BREVE);
 	map.put("VK_DEAD_CARON", KeyEvent.VK_DEAD_CARON);
 	map.put("VK_DEAD_CEDILLA", KeyEvent.VK_DEAD_CEDILLA);
 	map.put("VK_DEAD_CIRCUMFLEX", KeyEvent.VK_DEAD_CIRCUMFLEX);
 	map.put("VK_DEAD_DIAERESIS", KeyEvent.VK_DEAD_DIAERESIS);
 	map.put("VK_DEAD_DOUBLEACUTE", KeyEvent.VK_DEAD_DOUBLEACUTE);
 	map.put("VK_DEAD_GRAVE", KeyEvent.VK_DEAD_GRAVE);
 	map.put("VK_DEAD_IOTA", KeyEvent.VK_DEAD_IOTA);
 	map.put("VK_DEAD_MACRON", KeyEvent.VK_DEAD_MACRON);
 	map.put("VK_DEAD_OGONEK", KeyEvent.VK_DEAD_OGONEK);
 	map.put("VK_DEAD_SEMIVOICED_SOUND", KeyEvent.VK_DEAD_SEMIVOICED_SOUND);
 	map.put("VK_DEAD_TILDE", KeyEvent.VK_DEAD_TILDE);
 	map.put("VK_DEAD_VOICED_SOUND", KeyEvent.VK_DEAD_VOICED_SOUND);
 	map.put("VK_DECIMAL", KeyEvent.VK_DECIMAL);
 	map.put("VK_DELETE", KeyEvent.VK_DELETE);
 	map.put("VK_DIVIDE", KeyEvent.VK_DIVIDE);
 	map.put("VK_DOLLAR", KeyEvent.VK_DOLLAR);
 	map.put("VK_DOWN", KeyEvent.VK_DOWN);
 	map.put("VK_E", KeyEvent.VK_E);
 	map.put("VK_END", KeyEvent.VK_END);
 	map.put("VK_ENTER", KeyEvent.VK_ENTER);
 	map.put("VK_EQUALS", KeyEvent.VK_EQUALS);
 	map.put("VK_ESCAPE", KeyEvent.VK_ESCAPE);
 	map.put("VK_EURO_SIGN", KeyEvent.VK_EURO_SIGN);
 	map.put("VK_EXCLAMATION_MARK", KeyEvent.VK_EXCLAMATION_MARK);
 	map.put("VK_F", KeyEvent.VK_F);
 	map.put("VK_F1", KeyEvent.VK_F1);
 	map.put("VK_F10", KeyEvent.VK_F10);
 	map.put("VK_F11", KeyEvent.VK_F11);
 	map.put("VK_F12", KeyEvent.VK_F12);
 	map.put("VK_F13", KeyEvent.VK_F13);
 	map.put("VK_F14", KeyEvent.VK_F14);
 	map.put("VK_F15", KeyEvent.VK_F15);
 	map.put("VK_F16", KeyEvent.VK_F16);
 	map.put("VK_F17", KeyEvent.VK_F17);
 	map.put("VK_F18", KeyEvent.VK_F18);
 	map.put("VK_F19", KeyEvent.VK_F19);
 	map.put("VK_F2", KeyEvent.VK_F2);
 	map.put("VK_F20", KeyEvent.VK_F20);
 	map.put("VK_F21", KeyEvent.VK_F21);
 	map.put("VK_F22", KeyEvent.VK_F22);
 	map.put("VK_F23", KeyEvent.VK_F23);
 	map.put("VK_F24", KeyEvent.VK_F24);
 	map.put("VK_F3", KeyEvent.VK_F3);
 	map.put("VK_F4", KeyEvent.VK_F4);
 	map.put("VK_F5", KeyEvent.VK_F5);
 	map.put("VK_F6", KeyEvent.VK_F6);
 	map.put("VK_F7", KeyEvent.VK_F7);
 	map.put("VK_F8", KeyEvent.VK_F8);
 	map.put("VK_F9", KeyEvent.VK_F9);
 	map.put("VK_FINAL", KeyEvent.VK_FINAL);
 	map.put("VK_FIND", KeyEvent.VK_FIND);
 	map.put("VK_FULL_WIDTH", KeyEvent.VK_FULL_WIDTH);
 	map.put("VK_G", KeyEvent.VK_G);
 	map.put("VK_GREATER", KeyEvent.VK_GREATER);
 	map.put("VK_H", KeyEvent.VK_H);
 	map.put("VK_HALF_WIDTH", KeyEvent.VK_HALF_WIDTH);
 	map.put("VK_HELP", KeyEvent.VK_HELP);
 	map.put("VK_HIRAGANA", KeyEvent.VK_HIRAGANA);
 	map.put("VK_HOME", KeyEvent.VK_HOME);
 	map.put("VK_I", KeyEvent.VK_I);
 	map.put("VK_INPUT_METHOD_ON_OFF", KeyEvent.VK_INPUT_METHOD_ON_OFF);
 	map.put("VK_INSERT", KeyEvent.VK_INSERT);
 	map.put("VK_INVERTED_EXCLAMATION_MARK",
		KeyEvent.VK_INVERTED_EXCLAMATION_MARK);
 	map.put("VK_J", KeyEvent.VK_J);
 	map.put("VK_JAPANESE_HIRAGANA", KeyEvent.VK_JAPANESE_HIRAGANA);
 	map.put("VK_JAPANESE_KATAKANA", KeyEvent.VK_JAPANESE_KATAKANA);
 	map.put("VK_JAPANESE_ROMAN", KeyEvent.VK_JAPANESE_ROMAN);
 	map.put("VK_K", KeyEvent.VK_K);
 	map.put("VK_KANA", KeyEvent.VK_KANA);
 	map.put("VK_KANA_LOCK", KeyEvent.VK_KANA_LOCK);
 	map.put("VK_KANJI", KeyEvent.VK_KANJI);
 	map.put("VK_KATAKANA", KeyEvent.VK_KATAKANA);
 	map.put("VK_KP_DOWN", KeyEvent.VK_KP_DOWN);
 	map.put("VK_KP_LEFT", KeyEvent.VK_KP_LEFT);
 	map.put("VK_KP_RIGHT", KeyEvent.VK_KP_RIGHT);
 	map.put("VK_KP_UP", KeyEvent.VK_KP_UP);
 	map.put("VK_L", KeyEvent.VK_L);
 	map.put("VK_LEFT", KeyEvent.VK_LEFT);
 	map.put("VK_LEFT_PARENTHESIS", KeyEvent.VK_LEFT_PARENTHESIS);
 	map.put("VK_LESS", KeyEvent.VK_LESS);
 	map.put("VK_M", KeyEvent.VK_M);
 	map.put("VK_META", KeyEvent.VK_META);
 	map.put("VK_MINUS", KeyEvent.VK_MINUS);
 	map.put("VK_MODECHANGE", KeyEvent.VK_MODECHANGE);
 	map.put("VK_MULTIPLY", KeyEvent.VK_MULTIPLY);
 	map.put("VK_N", KeyEvent.VK_N);
 	map.put("VK_NONCONVERT", KeyEvent.VK_NONCONVERT);
 	map.put("VK_NUM_LOCK", KeyEvent.VK_NUM_LOCK);
 	map.put("VK_NUMBER_SIGN", KeyEvent.VK_NUMBER_SIGN);
 	map.put("VK_NUMPAD0", KeyEvent.VK_NUMPAD0);
 	map.put("VK_NUMPAD1", KeyEvent.VK_NUMPAD1);
 	map.put("VK_NUMPAD2", KeyEvent.VK_NUMPAD2);
 	map.put("VK_NUMPAD3", KeyEvent.VK_NUMPAD3);
 	map.put("VK_NUMPAD4", KeyEvent.VK_NUMPAD4);
 	map.put("VK_NUMPAD5", KeyEvent.VK_NUMPAD5);
 	map.put("VK_NUMPAD6", KeyEvent.VK_NUMPAD6);
 	map.put("VK_NUMPAD7", KeyEvent.VK_NUMPAD7);
 	map.put("VK_NUMPAD8", KeyEvent.VK_NUMPAD8);
 	map.put("VK_NUMPAD9", KeyEvent.VK_NUMPAD9);
 	map.put("VK_O", KeyEvent.VK_O);
 	map.put("VK_OPEN_BRACKET", KeyEvent.VK_OPEN_BRACKET);
 	map.put("VK_P", KeyEvent.VK_P);
 	map.put("VK_PAGE_DOWN", KeyEvent.VK_PAGE_DOWN);
 	map.put("VK_PAGE_UP", KeyEvent.VK_PAGE_UP);
 	map.put("VK_PASTE", KeyEvent.VK_PASTE);
 	map.put("VK_PAUSE", KeyEvent.VK_PAUSE);
 	map.put("VK_PERIOD", KeyEvent.VK_PERIOD);
 	map.put("VK_PLUS", KeyEvent.VK_PLUS);
 	map.put("VK_PREVIOUS_CANDIDATE", KeyEvent.VK_PREVIOUS_CANDIDATE);
 	map.put("VK_PRINTSCREEN", KeyEvent.VK_PRINTSCREEN);
 	map.put("VK_PROPS", KeyEvent.VK_PROPS);
 	map.put("VK_Q", KeyEvent.VK_Q);
 	map.put("VK_QUOTE", KeyEvent.VK_QUOTE);
 	map.put("VK_QUOTEDBL", KeyEvent.VK_QUOTEDBL);
 	map.put("VK_R", KeyEvent.VK_R);
 	map.put("VK_RIGHT", KeyEvent.VK_RIGHT);
 	map.put("VK_RIGHT_PARENTHESIS", KeyEvent.VK_RIGHT_PARENTHESIS);
 	map.put("VK_ROMAN_CHARACTERS", KeyEvent.VK_ROMAN_CHARACTERS);
 	map.put("VK_S", KeyEvent.VK_S);
 	map.put("VK_SCROLL_LOCK", KeyEvent.VK_SCROLL_LOCK);
 	map.put("VK_SEMICOLON", KeyEvent.VK_SEMICOLON);
 	map.put("VK_SEPARATOR", KeyEvent.VK_SEPARATOR);
 	map.put("VK_SHIFT", KeyEvent.VK_SHIFT);
 	map.put("VK_SLASH", KeyEvent.VK_SLASH);
 	map.put("VK_SPACE", KeyEvent.VK_SPACE);
 	map.put("VK_STOP", KeyEvent.VK_STOP);
 	map.put("VK_SUBTRACT", KeyEvent.VK_SUBTRACT);
 	map.put("VK_T", KeyEvent.VK_T);
 	map.put("VK_TAB", KeyEvent.VK_TAB);
 	map.put("VK_U", KeyEvent.VK_U);
 	map.put("VK_UNDEFINED", KeyEvent.VK_UNDEFINED);
 	map.put("VK_UNDERSCORE", KeyEvent.VK_UNDERSCORE);
 	map.put("VK_UNDO", KeyEvent.VK_UNDO);
 	map.put("VK_UP", KeyEvent.VK_UP);
 	map.put("VK_V", KeyEvent.VK_V);
 	map.put("VK_W", KeyEvent.VK_W);
 	map.put("VK_WINDOWS", KeyEvent.VK_WINDOWS);
 	map.put("VK_X", KeyEvent.VK_X);
 	map.put("VK_Y", KeyEvent.VK_Y);
 	map.put("VK_Z", KeyEvent.VK_Z);
    }

    /**
     * Look up a virtual key code by name.
     * @param name the name of a constant starting with the string "VK_"
     *        defined by the class {@link java.awt.event.KeyEvent}
     * @return the constant defined by a static field of
     *         {@link java.awt.event.KeyEvent} with the same name
     */
    public static int lookup(String name) throws IllegalArgumentException {
	Integer key = map.get(name);
	if (key == null) {
	    throw new IllegalArgumentException
		(errorMsg("keyValueMissing", name));
	} else {
	    return key;
	}
    }
}

//  LocalWords:  exbundle VK runtime BRACELEFT BRACERIGHT ABOVEDOT
//  LocalWords:  ABOVERING CARON DOUBLEACUTE OGONEK SEMIVOICED NUM
//  LocalWords:  MODECHANGE NONCONVERT NUMPAD PRINTSCREEN QUOTEDBL
//  LocalWords:  SEPARATER keyValueMissing
