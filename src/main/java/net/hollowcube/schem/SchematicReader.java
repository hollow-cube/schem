package net.hollowcube.schem;


import net.hollowcube.schem.blockpalette.BlockPaletteParser;
import net.hollowcube.schem.blockpalette.CommandBlockPaletteParser;
import net.kyori.adventure.nbt.*;
import net.minestom.server.command.builder.exception.ArgumentSyntaxException;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;

/**
 * Simple schematic file reader.
 */
public final class SchematicReader {
    private static final BinaryTagIO.Reader NBT_READER = BinaryTagIO.unlimitedReader();

    private BlockPaletteParser paletteParser = new CommandBlockPaletteParser();

    public SchematicReader withBlockPaletteParser(BlockPaletteParser parser) {
        this.paletteParser = parser;
        return this;
    }

    public @NotNull Schematic read(@NotNull InputStream stream) {
        try {
            return read(NBT_READER.readNamed(stream, BinaryTagIO.Compression.GZIP));
        } catch (Exception e) {
            throw new SchematicReadException("failed to read schematic NBT", e);
        }
    }

    public @NotNull Schematic read(@NotNull Path path) {
        try {
            return read(NBT_READER.readNamed(path, BinaryTagIO.Compression.GZIP));
        } catch (Exception e) {
            throw new SchematicReadException("failed to read schematic NBT", e);
        }
    }

    public @NotNull Schematic read(@NotNull Map.Entry<String, CompoundBinaryTag> namedTag) {
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

    private @NotNull Schematic read(@NotNull CompoundBinaryTag tag, int version) {
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
        ListBinaryTag blockEntitiesList = null;
        byte[] blockArray;
        int paletteSize;

        if (version == 3) {
            var blockEntries = tag.getCompound("Blocks");
            Check.notNull(blockEntries, "Missing required field 'Blocks'");

            palette = blockEntries.getCompound("Palette");
            Check.notNull(palette, "Missing required field 'Blocks.Palette'");

            paletteSize = palette.size();
            blockEntitiesList = blockEntries.getList("BlockEntities", BinaryTagTypes.COMPOUND);

            blockArray = blockEntries.getByteArray("Data");
            Check.notNull(blockArray, "Missing required field 'Blocks.Data'");
        } else {
            palette = tag.getCompound("Palette");
            Check.notNull(palette, "Missing required field 'Palette'");

            blockArray = tag.getByteArray("BlockData");
            Check.notNull(blockArray, "Missing required field 'BlockData'");

            paletteSize = tag.getInt("PaletteMax");
            Check.notNull(paletteSize, "Missing required field 'PaletteMax'");
        }

        if (blockEntitiesList == null) {
            blockEntitiesList = ListBinaryTag.empty();
        }

        BlockEntity[] blockEntities = blockEntitiesList.stream()
                .map((binaryTag) -> {
                    Check.isTrue(binaryTag instanceof CompoundBinaryTag, "BlockEntity is not a CompoundBinaryTag");

                    return BlockEntity.from((CompoundBinaryTag) binaryTag);
                })
                .toArray(BlockEntity[]::new);

        Block[] paletteBlocks = new Block[paletteSize];

        palette.forEach((entry) -> {
            try {
                int assigned = ((IntBinaryTag) entry.getValue()).value();
                Block block = paletteParser.parse(entry.getKey());

                paletteBlocks[assigned] = block;
            } catch (ArgumentSyntaxException e) {
                throw new SchematicReadException("Failed to parse block state: " + entry.getKey(), e);
            }
        });

        return new Schematic(
                new Vec(width, height, length),
                offset,
                paletteBlocks,
                blockArray,
                blockEntities
        );
    }
}
