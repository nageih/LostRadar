package mcjty.lostradar.network;

import mcjty.lib.network.CustomPacketPayload;
import mcjty.lib.network.PlayPayloadContext;
import mcjty.lostradar.LostRadar;
import mcjty.lostradar.data.ClientMapData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public record PacketPauseStateToClient(boolean paused) implements CustomPacketPayload {

    public static ResourceLocation ID = new ResourceLocation(LostRadar.MODID, "returnpausestate");

    public static PacketPauseStateToClient create(FriendlyByteBuf buf) {
        boolean paused = buf.readBoolean();
        return new PacketPauseStateToClient(paused);
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeBoolean(paused);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    public void handle(PlayPayloadContext ctx) {
        ctx.workHandler().submitAsync(() -> {
            ctx.player().ifPresent(player -> {
                ClientMapData clientMapData = ClientMapData.getData();
                clientMapData.setPauseState(paused);
            });
        });
    }
}
