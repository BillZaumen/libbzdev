package org.bzdev.anim2d;
import java.util.*;

//@exbundle org.bzdev.anim2d.lpack.StartupErrors

/**
 * This class is used to produce internationalized error
 * messages for startup scripts.
 */
public class StartupErrors {

    private static ResourceBundle exbundle = ResourceBundle.getBundle
	("org.bzdev.anim2d.lpack.StartupErrors");

    static String errorMsg(String key) {
	return exbundle.getString(key);
    }

    /**
     * Error message for a missing frameWidth property in the
     * specification passed to the ESP function createAnimation.
     * @return the error message
     */
    public static String missingFrameWidth() {
	return exbundle.getString("missingFrameWidth");
    }

    /**
     * Error message for a missing frameHeight property in the
     * specification passed to the ESP function createAnimation.
     * @return the error message
     */
    public static String missingFrameHeight() {
	return exbundle.getString("missingFrameHeight");
    }

    /**
     * Error message for a missing ticksPerSecond property in the
     * specification passed to the ESP function createAnimation.
     * @return the error message
     */
    public static String missingTicksPerSecond() {
	return exbundle.getString("missingTicksPerSecond");
    }

    /**
     * Error message for a missing ticksPerFrame property in the
     * specification passed to the ESP function createAnimation.
     * @return the error message
     */
    public static String missingTicksPerFrame() {
	return exbundle.getString("missingTicksPerFrame");
    }

    /**
     * Error message for a missing imageSpaceDistance property in the
     * specification passed to the ESP function createAnimation.
     * @return the error message
     */
    public static String missingImageSpaceDistance() {
	return exbundle.getString("missingImageSpaceDistance");
    }

    /**
     * Error message for a missing gcsDistance property in the
     * specification passed to the ESP function createAnimation.
     * @return the error message
     */
    public static String missingGCSDistance() {
	return exbundle.getString("missingGCSDistance");
    }
}
