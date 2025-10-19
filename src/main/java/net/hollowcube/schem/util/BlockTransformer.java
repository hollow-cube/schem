package net.hollowcube.schem.util;

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.block.Block;

public interface BlockTransformer {

    Block transform(Point point, Block block);

}
