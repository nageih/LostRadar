package mcjty.lostradar.setup;

import mcjty.lib.network.IPayloadRegistrar;
import mcjty.lib.network.Networking;
import mcjty.lostradar.LostRadar;
import mcjty.lostradar.radar.PacketRequestMap;
import mcjty.lostradar.radar.PacketReturnMapToClient;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkDirection;

public class Messages {

    private static IPayloadRegistrar registrar;

    public static void registerMessages() {
        registrar = Networking.registrar(LostRadar.MODID)
                .versioned("1.0")
                .optional();

        registrar.play(PacketRequestMap.class, PacketRequestMap::create, handler -> handler.client(PacketRequestMap::handle));
        registrar.play(PacketReturnMapToClient.class, PacketReturnMapToClient::create, handler -> handler.server(PacketReturnMapToClient::handle));
    }

    public static <T> void sendToPlayer(T packet, Player player) {
        registrar.getChannel().sendTo(packet, ((ServerPlayer)player).connection.connection, NetworkDirection.PLAY_TO_CLIENT);
    }

    public static <T> void sendToServer(T packet) {
        registrar.getChannel().sendToServer(packet);
    }
}
