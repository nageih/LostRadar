package mcjty.lostradar.network;

import mcjty.lib.network.IPayloadRegistrar;
import mcjty.lib.network.Networking;
import mcjty.lostradar.LostRadar;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.PacketDistributor;

public class Messages {

    private static IPayloadRegistrar registrar;

    public static void registerMessages() {
        registrar = Networking.registrar(LostRadar.MODID)
                .versioned("1.0")
                .optional();

        registrar.play(PacketRequestMapChunk.class, PacketRequestMapChunk::create, handler -> handler.client(PacketRequestMapChunk::handle));
        registrar.play(PacketReturnMapChunkToClient.class, PacketReturnMapChunkToClient::create, handler -> handler.client(PacketReturnMapChunkToClient::handle));
        registrar.play(PacketKnowledgeToPlayer.class, PacketKnowledgeToPlayer::create, handler -> handler.client(PacketKnowledgeToPlayer::handle));

        registrar.play(PacketStartSearch.class, PacketStartSearch::create, handler -> handler.server(PacketStartSearch::handle));
        registrar.play(PacketReturnSearchResultsToClient.class, PacketReturnSearchResultsToClient::create, handler -> handler.server(PacketReturnSearchResultsToClient::handle));
    }

    public static <T> void sendToPlayer(T packet, Player player) {
        registrar.getChannel().sendTo(packet, ((ServerPlayer)player).connection.connection, NetworkDirection.PLAY_TO_CLIENT);
    }

    public static <T> void sendToServer(T packet) {
        registrar.getChannel().sendToServer(packet);
    }

    public static <T> void sendToAllPlayers(ResourceKey<Level> level, T packet) {
        registrar.getChannel().send(PacketDistributor.DIMENSION.with(() -> level), packet);
    }
}
