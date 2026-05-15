package io.github.muntashirakon.setedit.cursor;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;

public class SortHelperTest {

    @Test
    public void testGetInstance_returnsNonNull() {
        SortHelper helper = SortHelper.getInstance(1);
        assertNotNull("getInstance should not return null", helper);
    }

    @Test
    public void testGetInstance_returnsDistinctInstances() {
        SortHelper helper1 = SortHelper.getInstance(1);
        SortHelper helper2 = SortHelper.getInstance(1);

        assertNotNull(helper1);
        assertNotNull(helper2);
        assertNotSame("getInstance should return a new instance each time", helper1, helper2);
    }
}
