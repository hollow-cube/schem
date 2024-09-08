package net.hollowcube.schem;

import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;

public record BlockEntity(String id, Point point, CompoundBinaryTag trimmedTag) {

    public static BlockEntity fromV3(CompoundBinaryTag blockTag) {
        int[] position = blockTag.getIntArray("Pos");
        return new BlockEntity(blockTag.getString("Id"), new Vec(position[0], position[1], position[2]), blockTag.getCompound("Data"));
    }

    public static BlockEntity fromV1(CompoundBinaryTag entityCompound) {
        int[] position = entityCompound.getIntArray("Pos");
        String id = entityCompound.getString("Id");
        CompoundBinaryTag stripped = CompoundBinaryTag
                .builder()
                .put(entityCompound)
                .remove("Pos")
                .remove("Id")
                .remove("Extra").build();
        return new BlockEntity(id, new Vec(position[0], position[1], position[2]), stripped);
    }
}
