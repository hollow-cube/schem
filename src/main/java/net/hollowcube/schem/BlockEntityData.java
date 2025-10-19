package net.hollowcube.schem;

import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minestom.server.coordinate.Point;


public record BlockEntityData(
        String id,
        Point position,
        CompoundBinaryTag data
) {
}
