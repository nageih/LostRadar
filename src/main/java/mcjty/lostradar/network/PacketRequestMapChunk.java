package mcjty.lostradar.network;

import mcjty.lib.network.CustomPacketPayload;
import mcjty.lib.network.PlayPayloadContext;
import mcjty.lostradar.LostRadar;
import mcjty.lostradar.data.ServerMapData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;

public record PacketRequestMapChunk(ChunkPos pos) implements CustomPacketPayload {

    public static ResourceLocation ID = new ResourceLocation(LostRadar.MODID, "requestmapchunk");

    public static PacketRequestMapChunk create(FriendlyByteBuf buf) {
        ChunkPos pos = buf.readChunkPos();
        return new PacketRequestMapChunk(pos);
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeChunkPos(pos);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    public void handle(PlayPayloadContext ctx) {
        ctx.workHandler().submitAsync(() -> {
            ctx.player().ifPresent(player -> {
                ServerMapData mapData = ServerMapData.getData(player.level());
                mapData.requestMapChunk(player.level(), pos);
            });
        });
    }
}
