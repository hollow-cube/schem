package net.hollowcube.schem;

import net.hollowcube.schem.util.BlockConsumer;
import net.hollowcube.schem.util.Rotation;
import net.kyori.adventure.nbt.ByteArrayBinaryTag;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.IntArrayBinaryTag;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public record AxiomBlueprint(
        CompoundBinaryTag header,
        ByteArrayBinaryTag thumbnail,
        Point size,
        List<Block> palette,
        List<SectionData> sections
) implements Schematic {
    public static final int MAGIC_NUMBER = 0xAE5BB36;
    public static final int BLOCK_PALETTE_SIZE = 4096;

    public record SectionData(
            Point chunkPos,
            List<Block> palette,
            // Null means all one palette
            @Nullable IntArrayBinaryTag blockData
    ) {
    }

    @Override
    public void forEachBlock(Rotation rotation, BlockConsumer consumer) {

    }

    @Override
    public CompoundBinaryTag metadata() {
        return header;
    }

    public ByteArrayBinaryTag thumbnail() {
        return thumbnail;
    }

    @Override
    public String name() {
        return header.getString("Name");
    }

    @Override
    public String author() {
        return header.getString("Author");
    }

    @Override
    public Point size() {
        return size;
    }

    @Override
    public boolean hasBlockData() {
        return true;
    }

    @Override
    public List<Block> blockPalette() {
        return List.of();
    }

    @Override
    public ByteArrayBinaryTag blockData() {
        return SpongeSchematic.EMPTY_BYTE_ARRAY;
    }

}
