package net.hollowcube.schem.reader;

import net.hollowcube.schem.Schematic;

import java.io.IOException;

public interface SchematicReader {

    Schematic read(byte[] data) throws IOException;

}
