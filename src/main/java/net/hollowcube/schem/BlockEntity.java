package net.hollowcube.schem;

import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.block.Block;

public record BlockEntity(String id, Vec pos, CompoundBinaryTag data) {
    public Block apply(Block block) {
        return block.withNbt(data);
    }

    public static BlockEntity from(CompoundBinaryTag tag) {
        int[] pos = tag.getIntArray("Pos");
        int x = pos[0];
        int y = pos[1];
        int z = pos[2];

        return new BlockEntity(
                tag.getString("Id"),
                new Vec(x, y, z),
                tag.getCompound("Data")
        );
    }
}