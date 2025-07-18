package net.hollowcube.schem.builder;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.hollowcube.schem.BlockEntityData;
import net.hollowcube.schem.Schematic;
import net.hollowcube.schem.SpongeSchematic;
import net.hollowcube.schem.old.CoordinateUtil;
import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.ByteArrayBinaryTag;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import static net.hollowcube.schem.old.CoordinateUtil.blockIndex;

public class UnboundedSchematicBuilder implements SchematicBuilder {
    // Point -> Block, a missing value is air
    private final Map<Point, Block> blockSet = new ConcurrentHashMap<>();
    private final CompoundBinaryTag.Builder metadata = CompoundBinaryTag.builder();

    private Point offset = Vec.ZERO;

    @Override
    public void metadata(@NotNull String key, @NotNull BinaryTag value) {
        metadata.put(key, value);
    }

    @Override
    public void block(@NotNull Point point, @NotNull Block block) {
        blockSet.put(CoordinateUtil.floor(point), Objects.requireNonNull(block));
    }

    @Override
    public void offset(@NotNull Point point) {
        this.offset = point;
    }

    @Override
    public @NotNull Schematic build() {
        if (blockSet.isEmpty()) {
            return Schematic.empty();
        }

        Point min = blockSet.keySet().stream().findFirst().get();
        Point max = min;
        for (Point point : blockSet.keySet()) {
            min = CoordinateUtil.min(min, point);
            max = CoordinateUtil.max(max, point);
        }

        var size = max.sub(min).add(1);
        // This means the offset its always from the min corner, not zero
        offset = offset.add(min);
        var blockCount = size.blockX() * size.blockY() * size.blockZ();

        // Map of Block -> Palette ID
        Object2IntMap<Block> paletteMap = new Object2IntArrayMap<>();

        // We always keep air as palette block zero, since it is likely the vast
        // majority of blocks, so we want to ensure that it is one byte.
        if (blockSet.containsValue(Block.AIR)) {
            paletteMap.put(Block.AIR, 0);
        }

        var blockEntities = new Int2ObjectArrayMap<BlockEntityData>();

        // Write each block to the output buffer
        // Initial buffer size assumes that we have a palette less than 127
        // so each block is one byte. If the palette is larger, we will resize
        var blockBytes = ByteBuffer.allocate(blockCount + 4);
        for (int i = 0; i < blockCount; i++) {

            // Resize array if it is too small
            if (blockBytes.remaining() <= 3) {
                byte[] oldBytes = blockBytes.array();
                blockBytes = ByteBuffer.allocate(blockBytes.capacity() * 2);
                blockBytes.put(oldBytes);
            }

            int index = i, width = size.blockX(), length = size.blockZ();
            int y = index / (width * length);
            int remainder = index - (y * width * length);
            int z = remainder / width;
            int x = remainder - z * width;

            // Write the block in XZY order
            var blockPos = new Vec(x, y, z
//                    (int) (i % size.blockX()),
//                    (int) (i / (size.blockX() * size.blockZ())),
//                    (int) ((i / size.blockX()) % size.blockZ())
            ).add(min);
            var block = blockSet.get(blockPos);

            if (block == null) {
                // Block not set, write an air value
                writeVarInt(blockBytes, 0);
                continue;
            }

            // Write block palette index
            int blockId;
            if (!paletteMap.containsKey(block)) {
                blockId = paletteMap.size();
                paletteMap.put(block, paletteMap.size());
            } else {
                blockId = paletteMap.getInt(block);
            }
            writeVarInt(blockBytes, blockId);

            // Write block entity
            var blockHandler = block.handler();
            if (blockHandler != null) {
                var blockEntityId = blockHandler.getKey();
                var blockEntityData = Objects.requireNonNullElse(block.nbt(), CompoundBinaryTag.empty());
                blockEntities.put(blockIndex(size, x, y, z), new BlockEntityData(blockEntityId, new Vec(x, y, z), blockEntityData));
            }
        }

        var palette = new Block[paletteMap.size()];
        for (var entry : paletteMap.object2IntEntrySet()) {
            palette[entry.getIntValue()] = entry.getKey();
        }

        var out = new byte[blockBytes.position()];
        blockBytes.flip().get(out);

        return new SpongeSchematic(
                metadata.build(), size, offset,
                List.of(palette), ByteArrayBinaryTag.byteArrayBinaryTag(out),
                List.of(), SpongeSchematic.EMPTY_BYTE_ARRAY,
                blockEntities, List.of()
        );
    }

    private static void writeVarInt(ByteBuffer buf, int value) {
        if ((value & (0xFFFFFFFF << 7)) == 0) {
            buf.put((byte) value);
        } else if ((value & (0xFFFFFFFF << 14)) == 0) {
            buf.putShort((short) ((value & 0x7F | 0x80) << 8 | (value >>> 7)));
        } else if ((value & (0xFFFFFFFF << 21)) == 0) {
            buf.put((byte) (value & 0x7F | 0x80));
            buf.put((byte) ((value >>> 7) & 0x7F | 0x80));
            buf.put((byte) (value >>> 14));
        } else if ((value & (0xFFFFFFFF << 28)) == 0) {
            buf.putInt((value & 0x7F | 0x80) << 24 | (((value >>> 7) & 0x7F | 0x80) << 16)
                    | ((value >>> 14) & 0x7F | 0x80) << 8 | (value >>> 21));
        } else {
            buf.putInt((value & 0x7F | 0x80) << 24 | ((value >>> 7) & 0x7F | 0x80) << 16
                    | ((value >>> 14) & 0x7F | 0x80) << 8 | ((value >>> 21) & 0x7F | 0x80));
            buf.put((byte) (value >>> 28));
        }
    }
}
