package net.hollowcube.schem.util;

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;

public interface BlockTransformer {

    Block transform(@NotNull Point point, @NotNull Block block);

}
