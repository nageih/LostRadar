package mcjty.lostradar.data;

import com.mojang.serialization.DataResult;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;

import java.util.HashMap;

public class PlayerMapData {

    private MapPalette palette = new MapPalette(new HashMap<>());
    private boolean dirty = true;

    public PlayerMapData() {
    }

    public void tick(ServerPlayer player) {
        if (dirty) {
            syncToClient(player);
        }
    }

    private void syncToClient(ServerPlayer player) {
//        Networking.sendToPlayer(PacketSendPreferencesToClient.create(buffStyle, buffX, buffY, style), player);
        dirty = false;
    }

    public Tag saveNBTData() {
        DataResult<Tag> result = MapPalette.CODEC.encodeStart(NbtOps.INSTANCE, palette);
        return result.result().orElseThrow(() -> new IllegalStateException("Failed to encode palette"));
    }

    public void loadNBTData(Tag tag) {
        MapPalette.CODEC.decode(NbtOps.INSTANCE, tag)
                .resultOrPartial(error -> {
                    throw new IllegalStateException("Failed to decode palette: " + error);
                })
                .ifPresent(palette -> this.palette = new MapPalette(palette.getFirst().palette()));
        dirty = true;
    }

    public static void register(RegisterCapabilitiesEvent event) {
        event.register(PlayerMapData.class);
    }

    public void copyFrom(PlayerMapData oldStore) {
        this.palette = oldStore.palette;
        this.dirty = true;
    }
}