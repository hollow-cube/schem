package net.hollowcube.schem.builder;

import net.hollowcube.schem.Schematic;
import net.kyori.adventure.nbt.BinaryTag;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.block.Block;

public interface SchematicBuilder {

    static SchematicBuilder builder(Point size) {
        return new SizedSchematicBuilder(size);
    }

    static SchematicBuilder builder() {
        return new UnboundedSchematicBuilder();
    }

    void metadata(String key, BinaryTag value);

    void block(Point point, Block block);

    default void block(int x, int y, int z, Block block) {
        block(new Vec(x, y, z), block);
    }

    void offset(Point point);

    default void offset(int x, int y, int z) {
        offset(new Vec(x, y, z));
    }

    Schematic build();

}
