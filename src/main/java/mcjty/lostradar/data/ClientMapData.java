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
    private final Set<ChunkPos> searchResults = new HashSet<>();
    private final Set<EntryPos> searchedChunks = new HashSet<>();

    private static final ClientMapData INSTANCE = new ClientMapData();

    private String searchString = "";
    private int searchProgress = 100;
    private boolean paused = false;

    @Nonnull
    public static ClientMapData getData() {
        return INSTANCE;
    }

    private ClientMapData() {
    }

    public void cleanup() {
        mapChunks.clear();
        requestedChunks.clear();
        searchResults.clear();
        searchedChunks.clear();
        searchString = "";
        searchProgress = 100;
        paused = false;
    }

    public void clearSearchResults() {
        searchResults.clear();
        searchedChunks.clear();
        searchProgress = 100;
        paused = false;
    }

    public void setPauseState(boolean paused) {
        this.paused = paused;
    }

    public boolean isPaused() {
        return paused;
    }

    public void setSearchProgress(int progress) {
        this.searchProgress = progress;
    }

    public int getSearchProgress() {
        return searchProgress;
    }

    public void addSearchResults(Set<ChunkPos> positions, Set<EntryPos> chunks) {
        searchResults.addAll(positions);
        searchedChunks.addAll(chunks);
        paused = false;
    }

    public Set<ChunkPos> getSearchResults() {
        return searchResults;
    }

    public Set<EntryPos> getSearchedChunks() {
        return searchedChunks;
    }

    public String getSearchString() {
        return searchString;
    }

    public void setSearchString(String searchString) {
        this.searchString = searchString;
    }

    public void addChunk(ResourceKey<Level> level, MapChunk chunk) {
        EntryPos entryPos = new EntryPos(level, chunk.chunkX(), chunk.chunkZ());
        requestedChunks.remove(entryPos);
        mapChunks.put(entryPos, chunk);
    }

    public int getBiomeColor(Level level, ChunkPos pos) {
        EntryPos entryPos = EntryPos.fromChunkPos(level.dimension(), pos);
        MapChunk mapChunk = mapChunks.get(entryPos);
        if (mapChunk == null) {
            return -1;
        }
        return mapChunk.getBiomeColorAt(pos);
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
            Messages.sendToServer(new PacketRequestMapChunk(entryPos));
            requestedChunks.add(entryPos);
            return null;
        }
        int dataAt = mapChunk.getDataAt(pos);
        if (dataAt < 0) {
            return null;
        }
        if (dataAt == MapChunk.CITY) {
            return MapPalette.CITY;
        } else if (dataAt == MapChunk.HIGHWAY) {
            return MapPalette.HIGHWAY;
        } else {
            PaletteCache palette = PaletteCache.getOrCreatePaletteCache(MapPalette.getDefaultPalette(level));
            return palette.getPalette().palette().get(dataAt);
        }
    }
}
