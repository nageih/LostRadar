package mcjty.lostradar.network;

import mcjty.lib.network.CustomPacketPayload;
import mcjty.lib.network.PlayPayloadContext;
import mcjty.lostradar.LostRadar;
import mcjty.lostradar.data.ClientMapData;
import mcjty.lostradar.data.MapChunk;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public record PacketReturnMapChunkToClient(ResourceKey<Level> level, MapChunk chunk) implements CustomPacketPayload {

    public static ResourceLocation ID = new ResourceLocation(LostRadar.MODID, "returnmapchunk");

    public static PacketReturnMapChunkToClient create(FriendlyByteBuf buf) {
        ResourceKey<Level> level = ResourceKey.create(Registries.DIMENSION, buf.readResourceLocation());
        MapChunk chunk = MapChunk.STREAM_CODEC.decode(buf);
        return new PacketReturnMapChunkToClient(level, chunk);
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeResourceLocation(level.location());
        MapChunk.STREAM_CODEC.encode(buf, chunk);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    public void handle(PlayPayloadContext ctx) {
        ctx.workHandler().submitAsync(() -> {
            ctx.player().ifPresent(player -> {
                ClientMapData clientMapData = ClientMapData.getData();
                clientMapData.addChunk(level, chunk);
            });
        });
    }
}
