package net.hollowcube.schem.blockpalette;

import net.minestom.server.command.builder.arguments.minecraft.ArgumentBlockState;
import net.minestom.server.instance.block.Block;

public class CommandBlockPaletteParser implements BlockPaletteParser {
    @Override
    public Block parse(String key) {
        return ArgumentBlockState.staticParse(key);
    }
}
