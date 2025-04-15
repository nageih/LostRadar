package mcjty.lostradar.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mcjty.lib.varia.codec.StandardCodecs;
import mcjty.lib.varia.codec.StreamCodec;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.ChunkPos;

import java.util.Arrays;
import java.util.List;

/**
 * This record represents a map chunk of NxN chunks. The chunkX and chunkZ represent the top-left chunk.
 * The data is a flattened array of NxN shorts where each short represents a building category ID which
 * is mapped from a palette (separate structure)
 * The biomecolors is a 16x16 flattened array of biome colors
 */
public record MapChunk(int chunkX, int chunkZ, short[] data, int[] biomeColors) {

    public static final int MAPCHUNK_SIZE = 8;
    public static final int MAPCHUNK_MASK = 0x7;

    public static final short CITY = Short.MAX_VALUE;
    public static final short HIGHWAY = Short.MAX_VALUE - 1;

    public static final StreamCodec<FriendlyByteBuf, MapChunk> STREAM_CODEC = StreamCodec.composite(
            StandardCodecs.INT, MapChunk::chunkX,
            StandardCodecs.INT, MapChunk::chunkZ,
            StandardCodecs.SHORT_ARRAY, MapChunk::data,
            StandardCodecs.INT_ARRAY, MapChunk::biomeColors,
            MapChunk::new
    );

    public static final Codec<MapChunk> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("chunkX").forGetter(MapChunk::chunkX),
            Codec.INT.fieldOf("chunkZ").forGetter(MapChunk::chunkZ),
            Codec.list(Codec.SHORT).fieldOf("data").forGetter(d -> convertToList(d.data)),
            Codec.list(Codec.INT).fieldOf("biomeColors").forGetter(d -> Arrays.stream(d.biomeColors).boxed().toList())
    ).apply(instance, (chunkX, chunkZ, shorts, biomeColors) -> {
        short[] data = new short[shorts.size()];
        for (int i = 0; i < shorts.size(); i++) {
            data[i] = shorts.get(i);
        }
        int[] bc = new int[biomeColors.size()];
        for (int i = 0; i < biomeColors.size(); i++) {
            bc[i] = biomeColors.get(i);
        }
        return new MapChunk(chunkX, chunkZ, data, bc);
    }));

    private static List<Short> convertToList(short[] shorts) {
        Short[] boxed = new Short[shorts.length];
        for (int i = 0; i < shorts.length; i++) {
            boxed[i] = shorts[i];
        }
        return Arrays.asList(boxed);
    }

    // Get the data at the given chunk position. This will return -1 if the chunk position is out of bounds for this map chunk
    public int getDataAt(ChunkPos pos) {
        int x = pos.x & MAPCHUNK_MASK;
        int z = pos.z & MAPCHUNK_MASK;
        int index = x + z * MAPCHUNK_SIZE;
        return data[index];
    }

    public int getBiomeColorAt(ChunkPos pos) {
        int x = pos.x & MAPCHUNK_MASK;
        int z = pos.z & MAPCHUNK_MASK;
        int index = x + z * MAPCHUNK_SIZE;
        return biomeColors[index];
    }
}
