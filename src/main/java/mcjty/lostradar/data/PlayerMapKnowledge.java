package mcjty.lostradar.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
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

    private final Set<ResourceLocation> knownCategories = new HashSet<>();

    private static final Codec<Set<ResourceLocation>> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.list(ResourceLocation.CODEC).fieldOf("categories").forGetter(resourceLocations -> new ArrayList<>(resourceLocations))
    ).apply(instance, resourceLocations -> new HashSet<>(resourceLocations)));

    public PlayerMapKnowledge() {
    }

    public Set<ResourceLocation> getKnownCategories() {
        return knownCategories;
    }

    public ResourceLocation clientGetCategoryAt(Level level, ChunkPos pos) {
        ClientMapData data = ClientMapData.getData();
        return data.getCategory(level.dimension(), pos);
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
}