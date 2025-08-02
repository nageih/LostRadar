package mcjty.lostradar.network;

import mcjty.lib.network.CustomPacketPayload;
import mcjty.lib.network.PlayPayloadContext;
import mcjty.lostradar.LostRadar;
import mcjty.lostradar.data.ServerMapData;
import mcjty.lostradar.setup.Registration;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public record PacketStartSearch(String category, int usage) implements CustomPacketPayload {

    public static ResourceLocation ID = new ResourceLocation(LostRadar.MODID, "startsearch");

    public static PacketStartSearch create(FriendlyByteBuf buf) {
        String category = buf.readUtf(32767);
        int usage = buf.readInt();
        return new PacketStartSearch(category, usage);
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeUtf(category);
        buf.writeInt(usage);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    public void handle(PlayPayloadContext ctx) {
        ctx.workHandler().submitAsync(() -> {
            ctx.player().ifPresent(player -> {
                ServerMapData mapData = ServerMapData.getData(player.level());
                if (category.isEmpty()) {
                    mapData.stopSearch(player);
                } else if (mapData.isSearching(player)) {
                    if (mapData.isPaused(player)) {
                        mapData.unpauseSearch(player);
                    } else {
                        mapData.pauseSearch(player);
                    }
                } else {
                    if (usage > 0) {
                        int extracted = Registration.RADAR.get().extractEnergyNoMax(player.getMainHandItem(), usage, false);
                        if (extracted < usage) {
                            player.sendSystemMessage(Component.translatable("message.lostradar.notenoughenergy", usage));
                            return;
                        }
                    }
                    mapData.startSearch(player, category);
                }
            });
        });
    }
}
