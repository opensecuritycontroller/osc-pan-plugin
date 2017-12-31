package com.paloaltonetworks.utils;

/**
 * Convert between OSC VS id and Panorama-Side Device Group name..
 */
public class VsToDevGroupNameUtil {

    // TODO: property?
    static final String PREFIX = "OSC_DeviceGroup_";

    public static final String vsId(String tag) {
        if (tag.startsWith(PREFIX)) {
            return tag.substring(PREFIX.length());
        } else {
            return "";
        }
    }

    /**
     * Convert from OSC VS id to Panorama-Side Device Group name.
     */
    public static String devGroupName(Long vsId) {
        return PREFIX + vsId;
    }

    /**
     * Convert from Panorama-Side Device Group name OSC VS Id.
     */
    public static String devGroupName(String vsId) {
        return PREFIX + vsId;
    }
}
