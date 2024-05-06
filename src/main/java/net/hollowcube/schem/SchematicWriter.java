package net.hollowcube.schem;

import net.kyori.adventure.nbt.BinaryTagIO;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class SchematicWriter {

    public static byte @NotNull [] write(@NotNull Schematic schematic) {
        CompoundBinaryTag.Builder schematicNBT = CompoundBinaryTag.builder();
        schematicNBT.putInt("Version", 2);
        schematicNBT.putInt("DataVersion", MinecraftServer.DATA_VERSION);

        Point size = schematic.size();
        schematicNBT.putShort("Width", (short) size.x());
        schematicNBT.putShort("Height", (short) size.y());
        schematicNBT.putShort("Length", (short) size.z());

        Point offset = schematic.offset();
        CompoundBinaryTag.Builder schematicMetadata = CompoundBinaryTag.builder();
        schematicMetadata.putInt("WEOffsetX", offset.blockX());
        schematicMetadata.putInt("WEOffsetY", offset.blockY());
        schematicMetadata.putInt("WEOffsetZ", offset.blockZ());

        schematicNBT.put("Metadata", schematicMetadata.build());

        schematicNBT.putByteArray("BlockData", schematic.blocks());
        Block[] blocks = schematic.palette();

        schematicNBT.putInt("PaletteMax", blocks.length);

        CompoundBinaryTag.Builder palette = CompoundBinaryTag.builder();
        for (int i = 0; i < blocks.length; i++) {
            if (blocks[i] == null) blocks[i] = Block.AIR;
            palette.putInt(BlockUtil.toStateString(blocks[i]), i);
        }
        schematicNBT.put("Palette", palette.build());

        var out = new ByteArrayOutputStream();
        try {
            BinaryTagIO.writer().writeNamed(Map.entry("Schematic", schematicNBT.build()), out, BinaryTagIO.Compression.GZIP);
        } catch (IOException e) {
            // No exceptions when writing to a byte array
            throw new RuntimeException(e);
        }

        return out.toByteArray();
    }

    public static void write(@NotNull Schematic schematic, @NotNull Path schemPath) throws IOException {
        Files.write(schemPath, write(schematic));
    }
}
