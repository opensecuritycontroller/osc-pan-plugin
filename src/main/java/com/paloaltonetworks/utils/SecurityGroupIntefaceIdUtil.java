package com.paloaltonetworks.utils;

public class SecurityGroupIntefaceIdUtil {
    private static final Character SEPARATOR = '_';

    public static String getSecurityGroupId(String sgiId) {
        int i = sgiId.indexOf(SEPARATOR);
        return sgiId.substring(0, i);
    }

    public static String getVirtualSystemId(String sgiId) {
        int i = sgiId.indexOf(SEPARATOR);
        return sgiId.substring(i + 1);
    }

    public static String getSecurityGroupIntefaceId(String sgId, String vsId) {
        return sgId + SEPARATOR + vsId;
    }
}
