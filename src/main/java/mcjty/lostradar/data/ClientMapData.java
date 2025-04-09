package mcjty.lostradar.data;

import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class ClientMapData {

    private final MapPalette palette = new MapPalette(new HashMap<>());
    private final Map<EntryPos, MapChunk> mapChunks = new HashMap<>();

    private static final ClientMapData INSTANCE = new ClientMapData();

    @Nonnull
    public static ClientMapData getData() {
        return INSTANCE;
    }

    private ClientMapData() {
    }

    public void addChunk(ResourceKey<Level> level, MapChunk chunk) {
        EntryPos entryPos = new EntryPos(level, chunk.chunkX(), chunk.chunkZ());
        mapChunks.put(entryPos, chunk);
    }

    @Nullable
    public ResourceLocation getCategory(ResourceKey<Level> level, ChunkPos pos) {
        if (palette.palette().isEmpty()) {
            // @todo request palette from server
            return null;
        }
        EntryPos entryPos = EntryPos.fromChunkPos(level, pos);
        MapChunk mapChunk = mapChunks.get(entryPos);
        if (mapChunk == null) {
            // @todo request chunk from server
            return null;
        }
        int dataAt = mapChunk.getDataAt(pos);
        return palette.palette().get(dataAt);
    }

}
