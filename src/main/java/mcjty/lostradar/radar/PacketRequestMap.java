package mcjty.lostradar.radar;

import mcjty.lib.network.CustomPacketPayload;
import mcjty.lib.network.PlayPayloadContext;
import mcjty.lostradar.LostRadar;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public record PacketRequestMap(BlockPos pos) implements CustomPacketPayload {

    public static ResourceLocation ID = new ResourceLocation(LostRadar.MODID, "requestmap");

    public static PacketRequestMap create(FriendlyByteBuf buf) {
        BlockPos pos = buf.readBlockPos();
        return new PacketRequestMap(pos);
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    public void handle(PlayPayloadContext ctx) {
        ctx.workHandler().submitAsync(() -> {
            ctx.player().ifPresent(player -> {
//                BlockEntity te = player.level().getBlockEntity(pos);
//                if (te instanceof AbstractSignTileEntity sign) {
//                    sign.setLines(lines);
//                    sign.setBackColor(backColor);
//                    sign.setTextColor(textColor);
//                    sign.setBright(bright);
//                    sign.setLarge(large);
//                    sign.setTransparent(transparent);
//                    sign.setTextureType(textureType);
//                    sign.setIconIndex(imageIndex);
//                }
            });
        });
    }
}
