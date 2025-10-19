package net.hollowcube.schem.util;

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.block.Block;

@FunctionalInterface
public interface BlockConsumer {

    void accept(Point blockPosition, Block block);

}
