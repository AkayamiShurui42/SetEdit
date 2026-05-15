package io.github.muntashirakon.setedit.boot;

import org.junit.Test;
import static org.junit.Assert.*;

import io.github.muntashirakon.setedit.utils.ActionResult;

import java.util.NoSuchElementException;

public class ActionItemTest {

    @Test
    public void testUnflattenFromString_ValidInput() {
        String input = "1\ttable\tname\tvalue";
        ActionItem item = ActionItem.unflattenFromString(input);
        assertEquals(1, item.action);
        assertEquals("table", item.table);
        assertEquals("name", item.name);
        assertEquals("value", item.value);
    }

    @Test(expected = NumberFormatException.class)
    public void testUnflattenFromString_InvalidAction() {
        String input = "invalid_action\ttable\tname\tvalue";
        ActionItem.unflattenFromString(input);
    }

    @Test(expected = NoSuchElementException.class)
    public void testUnflattenFromString_MissingTokens() {
        String input = "1\ttable";
        ActionItem.unflattenFromString(input);
    }

    @Test
    public void testUnflattenFromString_ValidInput_NoValue() {
        String input = "2\ttable\tname";
        ActionItem item = ActionItem.unflattenFromString(input);
        assertEquals(2, item.action); // 2 is ActionResult.TYPE_DELETE
        assertEquals("table", item.table);
        assertEquals("name", item.name);
        assertNull(item.value);
    }
}
