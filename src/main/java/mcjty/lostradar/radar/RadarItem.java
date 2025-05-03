package mcjty.lostradar.radar;

import mcjty.lostradar.LostRadar;
import mcjty.lostradar.data.PlayerMapKnowledgeDispatcher;
import mcjty.lostradar.network.Messages;
import mcjty.lostradar.network.PacketKnowledgeToPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;

public class RadarItem extends Item {

    public RadarItem() {
        super(LostRadar.setup.defaultProperties().stacksTo(1).defaultDurability(1));
    }

    @Override
    @Nonnull
    public InteractionResultHolder<ItemStack> use(@Nonnull Level world, @Nonnull Player player, @Nonnull InteractionHand hand) {
        if (world.isClientSide) {
            GuiRadar.open();
        } else {
            // Send knowledge data to the client
            PlayerMapKnowledgeDispatcher.getPlayerMapKnowledge(player).ifPresent(handler -> {
                Messages.sendToPlayer(new PacketKnowledgeToPlayer(handler.getKnownCategories()), player);
            });
        }
        return super.use(world, player, hand);
    }

}
