package net.hollowcube.schem.reader;

import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minestom.server.instance.block.Block;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ReadHelpersTest {
    private static final Block OAK_LOG_X = Block.OAK_LOG.withProperty("axis", "x");

    @Test
    void readsLegacyFieldNames() {
        var tag = CompoundBinaryTag.builder()
                .putString("Name", "minecraft:oak_log")
                .put("Properties", CompoundBinaryTag.builder().putString("axis", "x").build())
                .build();
        assertEquals(OAK_LOG_X, ReadHelpers.readBlockState(tag));
    }

    @Test
    void readsRenamedFieldNames() {
        var tag = CompoundBinaryTag.builder()
                .putString("id", "minecraft:oak_log")
                .put("properties", CompoundBinaryTag.builder().putString("axis", "x").build())
                .build();
        assertEquals(OAK_LOG_X, ReadHelpers.readBlockState(tag));
    }

    @Test
    void missingNameFails() {
        assertThrows(SchematicReadException.class, () -> ReadHelpers.readBlockState(CompoundBinaryTag.empty()));
    }
}
