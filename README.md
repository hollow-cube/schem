# Schem

[![license](https://img.shields.io/github/license/Minestom/MinestomDataGenerator.svg)](LICENSE)

A schematic reader and writer library for Minestom. Supports the following:

| Format            | Reading | Writing |
|-------------------|---------|---------|
| Sponge (v1-3)     | ✅       | ✅       |
| Vanilla Structure | ✅       | ⚠️      |
| Litematica        | ⚠️      | ❌       |
| Axiom Blueprints  | ⚠️      | ❌       |
| Legacy MCEdit     | ✅       | ❌       |

✅ Supported. ⚠️ Partial/experimental support. ❌ Not supported.

## Install

Schem is available on [maven central](https://search.maven.org/search?q=g:dev.hollowcube%20AND%20a:schem).

```groovy
repositories {
    mavenCentral()
}

dependencies {
    implementation 'dev.hollowcube:schem:<see releases>'
}
```

## Usage

### Reading a schematic

All schematics can be read using the appropriate `SchematicReader` implementation, for example:

```java
byte[] data = loadSchematicData();

// Read specifically a sponge schematic (throwing if invalid).
Schematic schematic = SchematicReader.sponge().read(data);
// Read any supported schematic format.
Schematic schematic = SchematicReader.detecting().read(data);
```

### Writing a schematic

Similarly to reading, `SchematicWriter` provides access to write schematics.

```java
Schematic mySchematic = fetchMyLoadedSchematic();

byte[] data = SchematicWriter.sponge().write(data);
```

You may notice that `SchematicWriter` takes a generic `Schematic`, rather than a specific implementation. This exists
to allow for conversion between formats. The expected behavior is for a writer implementation to support converting
a generic `Schematic` to its own format when writing. However, this conversion may be lossy if the target format does
not support all features of the source format.

### Loading older schematics

Schematics created in older versions of Minecraft may contain data which is no longer valid on the current version.

Schem supports callbacks to upgrade this data. To start, you must implement the `GameDataProvider` interface. The from
and to versions provided correspond to the vanilla data version format. The following example shows how you can upgrade
`chain` to `iron_chain`.

```java
public class MyGameDataProviderImpl implements GameDataProvider {
    private static final int IRON_CHAIN_VERSION = 4541;

    @Override
    public int dataVersion() {
        // Must return the 'current' data version. We can delegate to Minestom.
        return MinecraftServer.DATA_VERSION;
    }

    @Override
    public String upgradeBlockState(int fromVersion, int toVersion, String blockState) {
        if (fromVersion < IRON_CHAIN_VERSION && toVersion >= IRON_CHAIN_VERSION)
            return blockState.replace("chain", "iron_chain");
        return blockState;
    }
}
```

Finally, somewhere before reading any schematics, register the provider:

```java
GameDataProvider.replaceGlobals(new MyGameDataProviderImpl());
```

### Supporting extra formats

It is valid to implement a custom `Schematic`, `SchematicReader`, or `SchematicWriter` to support additional formats.
The existing writer implementations are expected to use the `Schematic` apis for format conversion.

## Contributing

Contributions via PRs and issues are always welcome.

## License

This project is licensed under the [MIT License](LICENSE).
