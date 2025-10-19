package net.hollowcube.schem.writer;

import net.hollowcube.schem.Schematic;

public interface SchematicWriter {
    String NAME = "github.com/hollow-cube/schem";

    byte[] write(Schematic schematic);
}
