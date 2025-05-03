package mcjty.lostradar.network;

import mcjty.lib.network.CustomPacketPayload;
import mcjty.lib.network.PlayPayloadContext;
import mcjty.lostradar.LostRadar;
import mcjty.lostradar.data.PlayerMapKnowledgeDispatcher;
import mcjty.lostradar.radar.GuiRadar;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Set;

public record PacketKnowledgeToPlayer(Set<String> knownCategories) implements CustomPacketPayload {

    public static ResourceLocation ID = new ResourceLocation(LostRadar.MODID, "knowledgetoplayer");

    public static PacketKnowledgeToPlayer create(FriendlyByteBuf buf) {
        List<String> categories = buf.readList(FriendlyByteBuf::readUtf);
        return new PacketKnowledgeToPlayer(Set.copyOf(categories));
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeCollection(knownCategories, FriendlyByteBuf::writeUtf);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    public void handle(PlayPayloadContext ctx) {
        ctx.workHandler().submitAsync(() -> {
            ctx.player().ifPresent(player -> {
                PlayerMapKnowledgeDispatcher.getPlayerMapKnowledge(player).ifPresent(handler -> {
                    Set<String> set = handler.getKnownCategories();
                    set.clear();
                    set.addAll(knownCategories);
                    GuiRadar.refresh();
                });
            });
        });
    }
}
