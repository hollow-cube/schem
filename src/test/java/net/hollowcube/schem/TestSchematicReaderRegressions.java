package net.hollowcube.schem;

import net.minestom.server.coordinate.Vec;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TestSchematicReaderRegressions {

    @Test
    public void testReadFail1_20_1() {
        var schem = assertReadSchematic("/regression/1_20_1_read_fail.schem");
        assertEquals(new Vec(15, 16, 20), schem.size());
    }

//    @Test
//    public void testSpongeV1() {
//        var schem = assertReadSchematic("/regression/sponge_1.schem");
//        assertEquals(new Vec(217, 70, 173), schem.size());
//    }

    private @NotNull Schematic assertReadSchematic(@NotNull String path) {
        try (var is = getClass().getResourceAsStream(path)) {
            assertNotNull(is, "Failed to load resource: " + path);
            return SchematicReader.read(is);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
