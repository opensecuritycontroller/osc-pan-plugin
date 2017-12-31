package com.paloaltonetworks.utils;

import static com.paloaltonetworks.utils.VsToDevGroupNameUtil.*;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class VsToDevGroupNameUtilTest {

    @SuppressWarnings("boxing")
    private static final Long TEST_ID = 12345678L;

    @Test
    public void testGetTag_WithNonNullLongId_ShouldYieldTag() {
        assertEquals(PREFIX + TEST_ID, devGroupName(TEST_ID));
    }

    @Test
    public void testGetTag_WithNonNullStringId_ShouldYieldTag() {
        assertEquals(PREFIX + TEST_ID, devGroupName(TEST_ID.toString()));
    }

    @Test
    public void testGetId_WithNonNullTag_ShouldYieldId() {
        assertEquals(TEST_ID.toString(), vsId(PREFIX + TEST_ID));
    }
}
