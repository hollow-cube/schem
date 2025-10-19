package net.hollowcube.schem.writer;

import net.hollowcube.schem.Schematic;

public interface SchematicWriter {
    String NAME = "github.com/hollow-cube/schem";

    static SchematicWriter sponge() {
        return new SpongeSchematicWriter();
    }

    static SchematicWriter structure() {
        return new StructureWriter();
    }

    byte[] write(Schematic schematic);
}
