package net.hollowcube.schem;


import net.kyori.adventure.nbt.BinaryTagIO;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.IntBinaryTag;
import net.minestom.server.command.builder.arguments.minecraft.ArgumentBlockState;
import net.minestom.server.command.builder.exception.ArgumentSyntaxException;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.Map;

/**
 * Simple schematic file reader.
 */
public final class SchematicReader {
    private static final BinaryTagIO.Reader NBT_READER = BinaryTagIO.unlimitedReader();

    private SchematicReader() {
    }

    public static @NotNull Schematic read(@NotNull InputStream stream) {
        try {
            return read(NBT_READER.readNamed(stream, BinaryTagIO.Compression.GZIP));
        } catch (Exception e) {
            throw new SchematicReadException("failed to read schematic NBT", e);
        }
    }

    public static @NotNull Schematic read(@NotNull Path path) {
        try {
            return read(NBT_READER.readNamed(path, BinaryTagIO.Compression.GZIP));
        } catch (Exception e) {
            throw new SchematicReadException("failed to read schematic NBT", e);
        }
    }

    public static @NotNull Schematic read(@NotNull Map.Entry<String, CompoundBinaryTag> namedTag) {
        try {
            // If it has a Schematic tag is sponge v2 or 3
            var schematicTag = namedTag.getValue().get("Schematic");
            if (schematicTag instanceof CompoundBinaryTag schematicCompound) {
                return read(schematicCompound, schematicCompound.getInt("Version"));
            }

            // Otherwise it is hopefully v1
            return read(namedTag.getValue(), 1);
        } catch (Exception e) {
            throw new SchematicReadException("Invalid schematic file", e);
        }
    }

    private static @NotNull Schematic read(@NotNull CompoundBinaryTag tag, int version) {
        short width = tag.getShort("Width");
        short height = tag.getShort("Height");
        short length = tag.getShort("Length");

        CompoundBinaryTag metadata = tag.getCompound("Metadata");

        var offset = Vec.ZERO;
        if (metadata.keySet().contains("WEOffsetX")) {
            int offsetX = metadata.getInt("WEOffsetX");
            int offsetY = metadata.getInt("WEOffsetY");
            int offsetZ = metadata.getInt("WEOffsetZ");

            offset = new Vec(offsetX, offsetY, offsetZ);
        } //todo handle sponge Offset

        CompoundBinaryTag palette;
        byte[] blockArray;
        Integer paletteSize;
        if (version == 3) {
            var blockEntries = tag.getCompound("Blocks");
            Check.notNull(blockEntries, "Missing required field 'Blocks'");

            palette = blockEntries.getCompound("Palette");
            Check.notNull(palette, "Missing required field 'Blocks.Palette'");
            blockArray = blockEntries.getByteArray("Data");
            Check.notNull(blockArray, "Missing required field 'Blocks.Data'");
            paletteSize = palette.size();
        } else {
            palette = tag.getCompound("Palette");
            Check.notNull(palette, "Missing required field 'Palette'");
            blockArray = tag.getByteArray("BlockData");
            Check.notNull(blockArray, "Missing required field 'BlockData'");
            paletteSize = tag.getInt("PaletteMax");
            Check.notNull(paletteSize, "Missing required field 'PaletteMax'");
        }

        Block[] paletteBlocks = new Block[paletteSize];

        palette.forEach((entry) -> {
            try {
                int assigned = ((IntBinaryTag) entry.getValue()).value();
                Block block = ArgumentBlockState.staticParse(entry.getKey());
                paletteBlocks[assigned] = block;
            } catch (ArgumentSyntaxException e) {
                throw new SchematicReadException("Failed to parse block state: " + entry.getKey(), e);
            }
        });

        return new Schematic(
                new Vec(width, height, length),
                offset,
                paletteBlocks,
                blockArray
        );
    }

}
