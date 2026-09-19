package net.hollowcube.schem.writer;

import net.minestom.server.instance.block.Block;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class WriteHelpersTest {
    private static final Block OAK_LOG_X = Block.OAK_LOG.withProperty("axis", "x");

    @Test
    void writesLegacyFieldNamesBefore5006() {
        var tag = WriteHelpers.writeBlockState(OAK_LOG_X, 5005);
        assertEquals("minecraft:oak_log", tag.getString("Name"));
        assertEquals("x", tag.getCompound("Properties").getString("axis"));
        assertFalse(tag.keySet().contains("id"));
    }

    @Test
    void writesRenamedFieldNamesFrom5006() {
        var tag = WriteHelpers.writeBlockState(OAK_LOG_X, 5006);
        assertEquals("minecraft:oak_log", tag.getString("id"));
        assertEquals("x", tag.getCompound("properties").getString("axis"));
        assertFalse(tag.keySet().contains("Name"));
    }
}
