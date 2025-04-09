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
 * This record represents a map chunk of 16x16 chunks. The chunkX and chunkZ represent the top-left chunk.
 * The data is a flattened array of 16x16 shorts where each short represents a building category ID which
 * is mapped from a palette (separate structure)
 */
public record MapChunk(int chunkX, int chunkZ, short[] data) {

    public static final StreamCodec<FriendlyByteBuf, MapChunk> STREAM_CODEC = StreamCodec.composite(
            StandardCodecs.INT, MapChunk::chunkX,
            StandardCodecs.INT, MapChunk::chunkZ,
            StandardCodecs.SHORT_ARRAY, MapChunk::data,
            MapChunk::new
    );

    public static final Codec<MapChunk> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("chunkX").forGetter(MapChunk::chunkX),
            Codec.INT.fieldOf("chunkZ").forGetter(MapChunk::chunkZ),
            Codec.list(Codec.SHORT).fieldOf("data").forGetter(d -> convertToList(d.data))
    ).apply(instance, (chunkX, chunkZ, shorts) -> {
        short[] data = new short[shorts.size()];
        for (int i = 0; i < shorts.size(); i++) {
            data[i] = shorts.get(i);
        }
        return new MapChunk(chunkX, chunkZ, data);
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
        int x = pos.x - chunkX;
        int z = pos.z - chunkZ;
        if (x < 0 || x >= 16 || z < 0 || z >= 16) {
            return -1;
        }
        int index = x + z * 16;
        return data[index];
    }
}
