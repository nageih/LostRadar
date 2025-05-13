package mcjty.lostradar.radar;

import mcjty.lib.varia.EnergyTools;
import mcjty.lib.varia.IEnergyItem;
import mcjty.lib.varia.ItemCapabilityProvider;
import mcjty.lostradar.LostRadar;
import mcjty.lostradar.data.PlayerMapKnowledgeDispatcher;
import mcjty.lostradar.network.Messages;
import mcjty.lostradar.network.PacketKnowledgeToPlayer;
import mcjty.lostradar.setup.Config;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import javax.annotation.Nonnull;

public class RadarItem extends Item implements IEnergyItem {

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

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        if (oldStack.isEmpty() != newStack.isEmpty()) {
            return true;
        }
        return oldStack.getItem() != newStack.getItem();
    }

    @Override
    public boolean isBarVisible(ItemStack pStack) {
        return true;
    }

    @Override
    public boolean canBeDepleted() {
        return true;
    }

    @Override
    public int getMaxDamage(ItemStack stack) {
        return Config.RADAR_MAXENERGY.get();
    }

    @Override
    public int getDamage(ItemStack stack) {
        if (stack.getTag() == null || !stack.getTag().contains("Energy")) {
            return 0;
        }
        return Config.RADAR_MAXENERGY.get() - stack.getTag().getInt("Energy");
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, CompoundTag nbt) {
        return new ItemCapabilityProvider(stack, this);
    }


    @Override
    public long receiveEnergyL(ItemStack container, long maxReceive, boolean simulate) {
        if (container.getTag() == null) {
            container.setTag(new CompoundTag());
        }
        int energy = container.getTag().getInt("Energy");
        int energyReceived = Math.min(Config.RADAR_MAXENERGY.get() - energy, Math.min(Config.RADAR_RECEIVEPERTICK.get(), EnergyTools.unsignedClampToInt(maxReceive)));

        if (!simulate) {
            energy += energyReceived;
            container.getTag().putInt("Energy", energy);
        }
        return energyReceived;
    }

    @Override
    public long extractEnergyL(ItemStack container, long maxExtract, boolean simulate) {
        if (container.getTag() == null || !container.getTag().contains("Energy")) {
            return 0;
        }
        int energy = container.getTag().getInt("Energy");
        int energyExtracted = 0;

        if (!simulate) {
            energy -= energyExtracted;
            container.getTag().putInt("Energy", energy);
        }
        return energyExtracted;
    }

    @Override
    public long getEnergyStoredL(ItemStack container) {
        if (container.getTag() == null || !container.getTag().contains("Energy")) {
            return 0;
        }
        return container.getTag().getInt("Energy");
    }

    @Override
    public long getMaxEnergyStoredL(ItemStack container) {
        return Config.RADAR_MAXENERGY.get();
    }

    public int extractEnergyNoMax(ItemStack container, int maxExtract, boolean simulate) {
        if (container.getTag() == null || !container.getTag().contains("Energy")) {
            return 0;
        }
        int energy = container.getTag().getInt("Energy");
        int energyExtracted = Math.min(energy, maxExtract);

        if (!simulate) {
            energy -= energyExtracted;
            container.getTag().putInt("Energy", energy);
        }
        return energyExtracted;
    }

}
