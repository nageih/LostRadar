package mcjty.lostradar.network;

import mcjty.lib.network.CustomPacketPayload;
import mcjty.lib.network.PlayPayloadContext;
import mcjty.lostradar.LostRadar;
import mcjty.lostradar.data.ClientMapData;
import mcjty.lostradar.data.EntryPos;
import mcjty.lostradar.data.MapChunk;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public record PacketReturnSearchResultsToClient(Set<ChunkPos> positions, Set<EntryPos> searchedChunks) implements CustomPacketPayload {

    public static ResourceLocation ID = new ResourceLocation(LostRadar.MODID, "returnsearchresults");

    public static PacketReturnSearchResultsToClient create(FriendlyByteBuf buf) {
        List<ChunkPos> positions = buf.readList(FriendlyByteBuf::readChunkPos);
        List<EntryPos> searchedChunks = buf.readList(EntryPos.STREAM_CODEC::decode);
        return new PacketReturnSearchResultsToClient(new HashSet<>(positions), new HashSet<>(searchedChunks));
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeCollection(positions, FriendlyByteBuf::writeChunkPos);
        buf.writeCollection(searchedChunks, EntryPos.STREAM_CODEC::encode);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    public void handle(PlayPayloadContext ctx) {
        ctx.workHandler().submitAsync(() -> {
            ctx.player().ifPresent(player -> {
                ClientMapData clientMapData = ClientMapData.getData();
                clientMapData.setSearchResults(positions, searchedChunks);
            });
        });
    }
}
