package net.hollowcube.schem.reader;

import net.hollowcube.schem.Schematic;

import java.io.IOException;

public interface SchematicReader {

    static SchematicReader detecting() {
        return new DetectingSchematicReader();
    }

    static SchematicReader sponge() {
        return new SpongeSchematicReader();
    }

    static SchematicReader structure() {
        return new StructureReader();
    }

    static SchematicReader litematica() {
        return new LitematicaSchematicReader();
    }

    static SchematicReader axiom() {
        return new AxiomBlueprintReader();
    }

    static SchematicReader legacyMcEdit() {
        return new MCEditSchematicReader();
    }

    Schematic read(byte[] data) throws IOException;

}
