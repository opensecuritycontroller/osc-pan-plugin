package com.paloaltonetworks.utils;

import static com.paloaltonetworks.utils.TagToSGIdUtil.PREFIX;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class TagToSGIdUtilTest {

    private static final String TEST_ID = "testSGId";

    @Test
    public void testGetTag_WithNonNullId_ShouldYieldTag() {
        assertEquals(PREFIX + TEST_ID, TagToSGIdUtil.getSecurityGroupTag(TEST_ID));
    }

    @Test
    public void testGetId_WithNonNullTag_ShouldYieldId() {
        assertEquals(TEST_ID, TagToSGIdUtil.getSecurityGroupId(PREFIX + TEST_ID));
    }
}
