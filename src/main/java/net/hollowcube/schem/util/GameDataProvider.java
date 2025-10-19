package net.hollowcube.schem.util;

import net.kyori.adventure.nbt.CompoundBinaryTag;

import java.util.Objects;

/**
 * DataProvider provides info about the current minecraft game version, as well as upgrading data if needed.
 *
 * <p>This is optional and a default implementation is provided which always returns the data version which the library
 * was created. Note that this is not necessarily always perfectly in sync and it is recommended to provide your own
 * provider even if it only provides the data version number.</p>
 */
public interface GameDataProvider {
    int DATA_VERSION_UNKNOWN = 0;

    static GameDataProvider provider() {
        return NoopGameDataProvider.INSTANCE;
    }

    static void replaceGlobals(GameDataProvider provider) {
        NoopGameDataProvider.INSTANCE = Objects.requireNonNull(provider, "game data provider");
    }

    //todo make note that 0 can be passed to upgrade in case the data version is unknown

    /**
     * Returns the current data version number.
     */
    int dataVersion();

    default String upgradeBlockState(int fromVersion, int toVersion, String blockState) {
        return blockState;
    }

    default CompoundBinaryTag upgradeBlockEntity(int fromVersion, int toVersion, String id, CompoundBinaryTag data) {
        return data;
    }

    default CompoundBinaryTag upgradeEntity(int fromVersion, int toVersion, CompoundBinaryTag data) {
        return data;
    }
}
