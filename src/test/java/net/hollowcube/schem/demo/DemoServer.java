package net.hollowcube.schem.demo;

import net.hollowcube.schem.reader.SpongeSchematicReader;
import net.hollowcube.schem.util.Rotation;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.instance.LightingChunk;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;

public class DemoServer {
    public static void main(String[] args) {
        var server = MinecraftServer.init();

        var instances = MinecraftServer.getInstanceManager();
        var instance = instances.createInstanceContainer();
        instance.setChunkSupplier(LightingChunk::new);
        instance.setGenerator(unit -> unit.modifier().fillHeight(0, 39, Block.STONE));

        var events = MinecraftServer.getGlobalEventHandler();
        events.addListener(AsyncPlayerConfigurationEvent.class, event -> {
            event.setSpawningInstance(instance);
            event.getPlayer().setRespawnPoint(new Pos(0, 40, 0));
        });
        events.addListener(PlayerSpawnEvent.class, event -> {
            var player = event.getPlayer();

            player.setGameMode(GameMode.CREATIVE);
        });

        var commands = MinecraftServer.getCommandManager();
        commands.register(new Command("paste") {
            {
                addSyntax(this::execute, ArgumentType.StringArray("path"));
            }

            public void execute(@NotNull CommandSender sender, @NotNull CommandContext context) {
                var player = (Player) sender;

                try (var is = getClass().getResourceAsStream("/" + String.join(" ", context.<String[]>get("path")) + ".schem")) {
                    var schem = new SpongeSchematicReader().read(is.readAllBytes());
                    schem.createBatch(Rotation.NONE).apply(instance, player.getPosition(), () -> {
                        player.sendMessage("Done!");
                    });
                } catch (Exception e) {
                    player.sendMessage("Failed to paste schematic: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });

        server.start("localhost", 25565);
    }
}
