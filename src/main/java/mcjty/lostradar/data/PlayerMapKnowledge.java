package mcjty.lostradar.data;

import com.mojang.serialization.DataResult;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;

import java.util.HashMap;

public class PlayerMapKnowledge {

    private MapPalette palette = new MapPalette(new HashMap<>());

    private boolean dirty = true;

    public PlayerMapKnowledge() {
    }

    public MapPalette getPalette() {
        return palette;
    }

    public void tick(ServerPlayer player) {
//        if (dirty) {
//            syncToClient(player);
//        }
    }

    public ResourceLocation clientGetCategoryAt(ResourceKey<Level> level, int chunkX, int chunkZ) {

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
        event.register(PlayerMapKnowledge.class);
    }

    public void copyFrom(PlayerMapKnowledge oldStore) {
        this.palette = oldStore.palette;
        this.dirty = true;
    }
}