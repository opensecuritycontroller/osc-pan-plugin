package com.paloaltonetworks.utils;

/**
 * Convert between OSC SG id and Panorama-Side Security Group Tag.
 */
public class TagToSGIdUtil {

    // TODO: property?
    static final String PREFIX = "OSC_SecurityGroup_";

    /**
     * Convert from Panorama-Side Security Group Tag to OSC SG id.
     */
    public static final String securityGroupId(String sgTag) {
        return sgTag.substring(PREFIX.length());
    }

    /**
     * Convert from OSC SG id to Panorama-Side Security Group Tag.
     */
    public static String securityGroupTag(String sgId) {
        return PREFIX + sgId;
    }
}
