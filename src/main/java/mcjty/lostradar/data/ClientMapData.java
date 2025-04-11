package mcjty.lostradar.data;

import mcjty.lostradar.network.Messages;
import mcjty.lostradar.network.PacketRequestMapChunk;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ClientMapData {

    private final Map<EntryPos, MapChunk> mapChunks = new HashMap<>();
    private final Set<EntryPos> requestedChunks = new HashSet<>();

    private static final ClientMapData INSTANCE = new ClientMapData();

    @Nonnull
    public static ClientMapData getData() {
        return INSTANCE;
    }

    private ClientMapData() {
    }

    public void addChunk(ResourceKey<Level> level, MapChunk chunk) {
        EntryPos entryPos = new EntryPos(level, chunk.chunkX(), chunk.chunkZ());
        requestedChunks.remove(entryPos);
        mapChunks.put(entryPos, chunk);
    }

    @Nullable
    public MapPalette.PaletteEntry getPaletteEntry(Level level, ChunkPos pos) {
        EntryPos entryPos = EntryPos.fromChunkPos(level.dimension(), pos);
        MapChunk mapChunk = mapChunks.get(entryPos);
        if (mapChunk == null) {
            if (requestedChunks.contains(entryPos)) {
                // Already requested, do nothing
                return null;
            }
            Messages.sendToServer(new PacketRequestMapChunk(pos));
            requestedChunks.add(entryPos);
            return null;
        }
        int dataAt = mapChunk.getDataAt(pos);
        if (dataAt < 0) {
            return null;
        }
        PaletteCache palette = PaletteCache.getOrCreatePaletteCache(MapPalette.getDefaultPalette(level));
        return palette.getPalette().palette().get(dataAt);
    }

}
