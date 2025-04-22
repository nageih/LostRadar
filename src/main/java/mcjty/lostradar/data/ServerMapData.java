package mcjty.lostradar.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mcjty.lostcities.api.ILostChunkInfo;
import mcjty.lostcities.api.ILostCityInformation;
import mcjty.lostradar.compat.LostCitiesCompat;
import mcjty.lostradar.network.Messages;
import mcjty.lostradar.network.PacketReturnMapChunkToClient;
import mcjty.lostradar.network.PacketReturnSearchResultsToClient;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.common.Tags;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ServerMapData {

    private final Map<EntryPos, MapChunk> mapChunks = new HashMap<>();

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

    private static final ServerMapData INSTANCE = new ServerMapData();

    @Nonnull
    public static ServerMapData getData() {
        return INSTANCE;
    }

    public void cleanup() {
        mapChunks.clear();
    }

    private ServerMapData() {
    }

    public void requestMapChunk(Level level, EntryPos pos) {
        if (level.isClientSide) {
            throw new RuntimeException("Don't access this client-side!");
        }
        MapChunk mapChunk = mapChunks.get(pos);
        System.out.println("SERVER: get request for " + pos + " and we got " + mapChunk);
        if (mapChunk == null) {
            mapChunk = calculateMapChunk(level, pos);
            System.out.println("SERVER: calculated for " + pos + " and we got " + mapChunk);
        }
        if (mapChunk != null) {
            // Send the map chunk to all clients
            PacketReturnMapChunkToClient packet = new PacketReturnMapChunkToClient(level.dimension(), mapChunk);
            Messages.sendToAllPlayers(level.dimension(), packet);
        }
    }

    private MapChunk getMapChunk(Level level, EntryPos pos) {
        MapChunk mapChunk = mapChunks.get(pos);
        if (mapChunk == null) {
            mapChunk = calculateMapChunk(level, pos);
        }
        return mapChunk;
    }

    public void startSearch(Player player, String category) {
        // @todo make async
        Level level = player.level();
        Set<ChunkPos> result = new HashSet<>();
        Set<EntryPos> searchedChunks = new HashSet<>();
        EntryPos pos = EntryPos.fromChunkPos(level.dimension(), new ChunkPos(player.blockPosition()));
        for (int x = -10 ; x <= 10 ; x++) {
            for (int z = -10 ; z <= 10 ; z++) {
                EntryPos entryPos = pos.offset(x, z);
                searchedChunks.add(entryPos);
                MapChunk mapChunk = getMapChunk(level, entryPos);
                if (mapChunk != null) {
                    findCategory(level, mapChunk, category, result);
                }
            }
        }
        // Send the result to the player
        Messages.sendToPlayer(new PacketReturnSearchResultsToClient(result, searchedChunks), player);
    }

    // Given a map chunk and a category, scan the map chunk and return the set of chunk positions that match the category
    private void findCategory(Level level, MapChunk mapChunk, String category, Set<ChunkPos> result) {
        PaletteCache palette = PaletteCache.getOrCreatePaletteCache(MapPalette.getDefaultPalette(level));
        for (int x = 0; x < MapChunk.MAPCHUNK_SIZE; x++) {
            for (int z = 0; z < MapChunk.MAPCHUNK_SIZE; z++) {
                int dataAt = mapChunk.getDataAt(new ChunkPos(mapChunk.chunkX() + x, mapChunk.chunkZ() + z));
                if (dataAt != -1) {
                    MapPalette.PaletteEntry entry = palette.getEntryForIndex(dataAt);
                    if (entry != null && category.equals(entry.name())) {
                        result.add(new ChunkPos(mapChunk.chunkX() + x, mapChunk.chunkZ() + z));
                    }
                }
            }
        }
    }

    private MapChunk calculateMapChunk(Level level, EntryPos pos) {
        ILostCityInformation info = LostCitiesCompat.lostCities.getLostInfo(level);
        if (info != null) {
            PaletteCache cache = PaletteCache.getOrCreatePaletteCache(MapPalette.getDefaultPalette(level));
            int defaultEntry = cache.getDefaultEntry();
            short[] data = new short[MapChunk.MAPCHUNK_SIZE * MapChunk.MAPCHUNK_SIZE];
            int[] biomeColors = new int[MapChunk.MAPCHUNK_SIZE * MapChunk.MAPCHUNK_SIZE];
            for (int x = 0; x < MapChunk.MAPCHUNK_SIZE; x++) {
                for (int z = 0; z < MapChunk.MAPCHUNK_SIZE; z++) {
                    int dataAt = -1;
                    ILostChunkInfo chunk = info.getChunkInfo(pos.chunkX() + x, pos.chunkZ() + z);
                    if (chunk != null) {
                        ResourceLocation buildingId = chunk.getBuildingId();
                        if (buildingId != null) {
                            int index = cache.getIndexForBuilding(buildingId);
                            if (index != -1) {
                                dataAt = (short) index;
                            } else {
                                dataAt = (short) defaultEntry;
                            }
                        } else if (chunk.getMaxHighwayLevel() != -1) {
                            dataAt = MapChunk.HIGHWAY;
                        } else if (chunk.isCity()) {
                            dataAt = MapChunk.CITY;
                        }
                    }
                    data[x + z * MapChunk.MAPCHUNK_SIZE] = (short) dataAt;
                    // @todo use getAverageBiomeColor
                    int biomeColor = getBiomeColor(level, pos, x, 8, z, 8);
                    biomeColors[x + z * MapChunk.MAPCHUNK_SIZE] = biomeColor;
                }
            }
            MapChunk mapChunk = new MapChunk(pos.chunkX(), pos.chunkZ(), data, biomeColors);
            mapChunks.put(pos, mapChunk);
            return mapChunk;
        }
        return null;
    }

    // For four points in a chunk we calculate the biome color and then return the color that occurs most
    private static int getAverageBiomeColor(Level level, EntryPos pos, int x, int z) {
        int []color = new int[4];
        color[0] = getBiomeColor(level, pos, x, 4, z, 4);
        color[1] = getBiomeColor(level, pos, x, 4, z, 12);
        color[2] = getBiomeColor(level, pos, x, 12, z, 4);
        color[3] = getBiomeColor(level, pos, x, 12, z, 12);
        int[] count = new int[4];
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if (color[i] == color[j]) {
                    count[i]++;
                }
            }
        }
        int max = 0;
        int maxIndex = 0;
        for (int i = 0; i < 4; i++) {
            if (count[i] > max) {
                max = count[i];
                maxIndex = i;
            }
        }
        return color[maxIndex];
    }

    private static int getBiomeColor(Level level, EntryPos pos, int x, int offsetX, int z, int offsetZ) {
        Holder<Biome> biome = level.getBiome(new BlockPos(((pos.chunkX() + x) << 4) + offsetX, 65, ((pos.chunkZ() + z) << 4) + offsetZ));
        // Biome colors: pastel blue for ocean, green for forests, brown for mountains, yellow for deserts
        int biomeColor = 0x00ff00;
        if (biome.containsTag(BiomeTags.IS_OCEAN) || biome.containsTag(BiomeTags.IS_RIVER) || biome.containsTag(BiomeTags.IS_BEACH)) {
            biomeColor = 0x0000ff;
        } else if (biome.containsTag(BiomeTags.IS_MOUNTAIN)) {
            biomeColor = 0x8b4513;
        } else if (biome.containsTag(Tags.Biomes.IS_DESERT) || biome.containsTag(BiomeTags.IS_BADLANDS)) {
            biomeColor = 0xffff00;
        } else if (biome.containsTag(BiomeTags.IS_FOREST)) {
            biomeColor = 0x006400;
        } else if (biome.containsTag(Tags.Biomes.IS_PLAINS)) {
            biomeColor = 0x00ff00;
        }
        return biomeColor;
    }
}
