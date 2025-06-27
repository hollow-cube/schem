package net.hollowcube.schem.demo;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.hollowcube.schem.Schematic;
import net.hollowcube.schem.reader.SpongeSchematicReader;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.Instant;
import java.util.List;


public class SchematicLoadTest {
    @Test
    public void testSpongeSchematicLoad() throws IOException {
        InputStream path = getClass().getClassLoader().getResourceAsStream("2x2_chunk_vert.schem");
        // load 2x2_chunk_vert_pallet.json
        InputStream palletPath = getClass().getClassLoader().getResourceAsStream("2x2_chunk_vert_palett.json");
        Assertions.assertNotNull(palletPath);
        // Read the pallet data
        List<BlockData> palletData = new Gson().fromJson(new InputStreamReader(palletPath), new TypeToken<List<BlockData>>(){}.getType());

        Assertions.assertNotNull(path);
        byte[] data = path.readAllBytes();

        SpongeSchematicReader reader = new SpongeSchematicReader();
        Schematic schematic = reader.read(data);

        Assertions.assertNotNull(schematic);

        Instant createdAt = schematic.createdAt();
        Assertions.assertNotNull(createdAt);
        Assertions.assertEquals(1751045545, createdAt.getEpochSecond());
        Assertions.assertEquals("2x2_chunk_vert", schematic.name());
        Assertions.assertEquals("utf_", schematic.author());
        Assertions.assertEquals(new Vec(32, 384, 32), schematic.size());
        Assertions.assertEquals(new Vec(-31, -133, 0), schematic.offset());
        Assertions.assertEquals(new Vec(-31, -133, 0), schematic.offset());
        Assertions.assertEquals(12, schematic.blockEntities().size());
        Assertions.assertEquals(82, schematic.blockPalette().size());

        List<BlockData> blockData = schematic.blockPalette().stream().map(BlockData::new).toList();
        Assertions.assertEquals(palletData.size(), blockData.size());
        Assertions.assertEquals(palletData, blockData);
    }

    public record BlockData(@NotNull String key, @Nullable String nbt) {
        public BlockData(Block block) {
            this(block.key().asString(), block.nbt() != null ? block.nbt().toString() : null);
        }
    }
}
