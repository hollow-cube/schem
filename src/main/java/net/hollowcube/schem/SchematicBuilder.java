package net.hollowcube.schem;

import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.block.Block;
import net.minestom.server.network.NetworkBuffer;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class SchematicBuilder {

    // Point -> Block, a missing value is air
    private final Map<Point, Block> blockSet = new ConcurrentHashMap<>();

    private Point offset = Vec.ZERO;

    public void addBlock(double x, double y, double z, @NotNull Block block) {
        addBlock(new Vec(x, y, z), block);
    }

    public void addBlock(@NotNull Point point, @NotNull Block block) {
        var pos = CoordinateUtil.floor(point);
        blockSet.put(pos, Objects.requireNonNull(block));
    }

    public void setOffset(double x, double y, double z) {
        setOffset(new Vec(x, y, z));
    }

    public void setOffset(@NotNull Point point) {
        this.offset = point;
    }

    public @NotNull Schematic build() {
        if (blockSet.isEmpty()) {
            return Schematic.EMPTY;
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

        // Write each block to the output buffer
        // Initial buffer size assumes that we have a palette less than 127
        // so each block is one byte. If the palette is larger, we will resize
        NetworkBuffer blockBytes = NetworkBuffer.resizableBuffer(blockCount + 4);
        for (int i = 0; i < blockCount; i++) {
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
                NetworkBuffer.VAR_INT.write(blockBytes, 0);
                continue;
            }

            int blockId;
            if (!paletteMap.containsKey(block)) {
                blockId = paletteMap.size();
                paletteMap.put(block, paletteMap.size());
            } else {
                blockId = paletteMap.getInt(block);
            }

            NetworkBuffer.VAR_INT.write(blockBytes, blockId);
        }

        var palette = new Block[paletteMap.size()];
        for (var entry : paletteMap.object2IntEntrySet()) {
            palette[entry.getIntValue()] = entry.getKey();
        }

        byte[] out = blockBytes.extractBytes(ignored -> {});

        return new Schematic(size, offset, palette, out);
    }
}
