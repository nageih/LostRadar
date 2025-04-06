package mcjty.lostradar.data;

import mcjty.lostradar.setup.ModSetup;
import net.minecraft.core.Direction;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PlayerMapDataDispatcher implements ICapabilityProvider, INBTSerializable<Tag> {

    public static LazyOptional<PlayerMapData> getPlayerMapData(Player player) {
        return player.getCapability(ModSetup.PLAYER_MAP_DATA);
    }

    private final PlayerMapData data = createProperties();
    private final LazyOptional<PlayerMapData> propertiesCap = LazyOptional.of(() -> data);

    private <T> PlayerMapData createProperties() {
        return new PlayerMapData();
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap) {
        if (cap == ModSetup.PLAYER_MAP_DATA) {
            return propertiesCap.cast();
        }
        return LazyOptional.empty();
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        return getCapability(cap);
    }

    @Override
    public Tag serializeNBT() {
        return data.saveNBTData();
    }

    @Override
    public void deserializeNBT(Tag nbt) {
        data.loadNBTData(nbt);
    }

}
