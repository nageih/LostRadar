package mcjty.lostradar.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

public class ServerMapData extends SavedData {

    public static final String NAME = "LostRadarData";

    public final MapPalette palette = new MapPalette(new HashMap<>());
    public final Map<EntryPos, MapChunk> mapChunks = new HashMap<>();

    private static final Codec<Pair<EntryPos, MapChunk>> PAIR_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            EntryPos.CODEC.fieldOf("entryPos").forGetter(Pair::getLeft),
            MapChunk.CODEC.fieldOf("mapChunk").forGetter(Pair::getRight)
    ).apply(instance, Pair::of));

    private static final Codec<Map<EntryPos, MapChunk>> MAP_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            PAIR_CODEC.listOf().fieldOf("mapChunks").forGetter(m -> m.entrySet().stream()
                    .map(entry -> Pair.of(entry.getKey(), entry.getValue()))
                    .toList())
    ).apply(instance, (chunks) -> {
        Map<EntryPos, MapChunk> map = new HashMap<>();
        for (Pair<EntryPos, MapChunk> entry : chunks) {
            map.put(entry.getLeft(), entry.getRight());
        }
        return map;
    }));

    @Nonnull
    public static ServerMapData getData(Level world) {
        if (world.isClientSide) {
            throw new RuntimeException("Don't access this client-side!");
        }
        DimensionDataStorage storage = ((ServerLevel)world).getDataStorage();
        return storage.computeIfAbsent(ServerMapData::new, ServerMapData::new, NAME);
    }

    public ServerMapData() {
    }

    public ServerMapData(CompoundTag tag) {
        load(tag);
    }

    private void load(CompoundTag nbt) {
        MapPalette.CODEC.decode(NbtOps.INSTANCE, nbt.get("palette"))
                .resultOrPartial(error -> {
                    throw new IllegalStateException("Failed to decode palette: " + error);
                })
                .ifPresent(palette -> this.palette.palette().putAll(palette.getFirst().palette()));
        MAP_CODEC.decode(NbtOps.INSTANCE, nbt.get("chunks"))
                .resultOrPartial(error -> {
                    throw new IllegalStateException("Failed to decode map chunks: " + error);
                })
                .ifPresent(chunks -> this.mapChunks.putAll(chunks.getFirst()));
    }

    @Override
    public CompoundTag save(CompoundTag nbt) {
        Tag paletteTag = MapPalette.CODEC.encodeStart(NbtOps.INSTANCE, palette).result().orElseThrow();
        Tag mapTag = MAP_CODEC.encodeStart(NbtOps.INSTANCE, mapChunks).result().orElseThrow();
        nbt.put("palette", paletteTag);
        nbt.put("chunks", mapTag);
        return nbt;
    }
}
