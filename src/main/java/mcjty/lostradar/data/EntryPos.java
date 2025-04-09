package mcjty.lostradar.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mcjty.lib.varia.codec.StreamCodec;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

// A position of a MapChunk (16x16 array of chunks). Coordinate represents the top-left chunk
public record EntryPos(ResourceKey<Level> level, int chunkX, int chunkZ) {

    public static final Codec<EntryPos> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceKey.codec(Registries.DIMENSION).fieldOf("level").forGetter(EntryPos::level),
            Codec.INT.fieldOf("chunkX").forGetter(EntryPos::chunkX),
            Codec.INT.fieldOf("chunkZ").forGetter(EntryPos::chunkZ)
    ).apply(instance, EntryPos::new));

    public static final StreamCodec<FriendlyByteBuf, EntryPos> STREAM_CODEC = StreamCodec.of(
            (buf, entry) -> {
                buf.writeResourceLocation(entry.level().location());
                buf.writeInt(entry.chunkX());
                buf.writeInt(entry.chunkZ());
            },
            buf -> {
                ResourceKey<Level> level = ResourceKey.create(Registries.DIMENSION, buf.readResourceLocation());
                int chunkX = buf.readInt();
                int chunkZ = buf.readInt();
                return new EntryPos(level, chunkX, chunkZ);
            });

    // Convert a chunk position to an EntryPos by calculating the top-left chunk of the 16x16 area that this chunk pos is in.
    public static EntryPos fromChunkPos(ResourceKey<Level> level, ChunkPos pos) {
        int topLeftX = pos.x - (pos.x % 16);
        int topLeftZ = pos.z - (pos.z % 16);
        return new EntryPos(level, topLeftX, topLeftZ);
    }
}

