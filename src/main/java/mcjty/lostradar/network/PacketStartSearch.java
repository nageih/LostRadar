package mcjty.lostradar.network;

import mcjty.lib.network.CustomPacketPayload;
import mcjty.lib.network.PlayPayloadContext;
import mcjty.lostradar.LostRadar;
import mcjty.lostradar.data.EntryPos;
import mcjty.lostradar.data.ServerMapData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public record PacketStartSearch(String category) implements CustomPacketPayload {

    public static ResourceLocation ID = new ResourceLocation(LostRadar.MODID, "startsearch");

    public static PacketStartSearch create(FriendlyByteBuf buf) {
        String category = buf.readUtf(32767);
        return new PacketStartSearch(category);
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeUtf(category);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    public void handle(PlayPayloadContext ctx) {
        ctx.workHandler().submitAsync(() -> {
            ctx.player().ifPresent(player -> {
                ServerMapData mapData = ServerMapData.getData(player.level());
                mapData.startSearch(player, category);
            });
        });
    }
}
