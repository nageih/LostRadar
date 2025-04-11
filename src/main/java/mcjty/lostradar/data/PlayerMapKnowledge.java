package mcjty.lostradar.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mcjty.lostcities.api.ILostChunkInfo;
import mcjty.lostcities.api.ILostCityInformation;
import mcjty.lostradar.compat.LostCitiesCompat;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class PlayerMapKnowledge {

    private final Set<String> knownCategories = new HashSet<>();

    private static final Codec<Set<String>> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.list(Codec.STRING).fieldOf("categories").forGetter(s -> new ArrayList<>(s))
    ).apply(instance, HashSet::new));

    public PlayerMapKnowledge() {
    }

    public Set<String> getKnownCategories() {
        return knownCategories;
    }

    public Tag saveNBTData() {
        DataResult<Tag> result = CODEC.encodeStart(NbtOps.INSTANCE, knownCategories);
        return result.result().orElseThrow(() -> new IllegalStateException("Failed to encode palette"));
    }

    public void loadNBTData(Tag tag) {
        CODEC.decode(NbtOps.INSTANCE, tag)
                .resultOrPartial(error -> {
                    throw new IllegalStateException("Failed to decode palette: " + error);
                })
                .ifPresent(palette -> {
                    this.knownCategories.clear();
                    this.knownCategories.addAll(palette.getFirst());
                });
    }

    public void copyFrom(PlayerMapKnowledge oldStore) {
        this.knownCategories.clear();
        this.knownCategories.addAll(oldStore.knownCategories);
    }

    public static void register(RegisterCapabilitiesEvent event) {
        event.register(PlayerMapKnowledge.class);
    }

    public void tick(ServerPlayer player) {
        ILostCityInformation lostInfo = LostCitiesCompat.lostCities.getLostInfo(player.level());
        if (lostInfo != null) {
            ChunkPos pos = player.chunkPosition();
            ResourceKey<Level> dimension = player.level().dimension();
            ILostChunkInfo chunkInfo = lostInfo.getChunkInfo(pos.x, pos.z);
            if (chunkInfo != null) {
                PaletteCache cache = PaletteCache.getOrCreatePaletteCache(MapPalette.getDefaultPalette(player.level()));
                ResourceLocation buildingId = chunkInfo.getBuildingId();
                if (buildingId != null) {
                    MapPalette.PaletteEntry entry = cache.getEntryForBuilding(buildingId);
                    if (entry != null) {
                        knownCategories.add(entry.name());
                    }
                }
            }
        }
    }
}