package net.hollowcube.schem.util;

import net.minestom.server.MinecraftServer;

final class NoopGameDataProvider implements GameDataProvider {
    static GameDataProvider INSTANCE = new NoopGameDataProvider();

    @Override
    public int dataVersion() {
        return MinecraftServer.DATA_VERSION;
    }
}
