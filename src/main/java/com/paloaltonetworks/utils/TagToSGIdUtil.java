package com.paloaltonetworks.utils;

public class TagToSGIdUtil {

    // TODO: property?
    static final String PREFIX = "OSC_SecurityGroup_";

    public static final String getSecurityGroupId(String tag) {
        return tag.substring(PREFIX.length());
    }

    public static String getSecurityGroupTag(String name) {
        return PREFIX + name;
    }
}
