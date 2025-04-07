package mcjty.lostradar.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mcjty.lib.varia.codec.StandardCodecs;
import mcjty.lib.varia.codec.StreamCodec;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class represents a palette for a map. It maps id's to categories
 */
public record MapPalette(Map<Integer, ResourceLocation> palette) {

    private static final StreamCodec<FriendlyByteBuf, Pair<Integer, ResourceLocation>> PALETTE_ENTRY_STREAM_CODEC = StreamCodec.composite(
            StandardCodecs.INT, Pair::getLeft,
            StandardCodecs.STRING_UTF8, p -> p.getRight().toString(),
            (left, right) -> Pair.of(left, new ResourceLocation(right))
    );

    public static final StreamCodec<FriendlyByteBuf, MapPalette> STREAM_CODEC = StreamCodec.composite(
            PALETTE_ENTRY_STREAM_CODEC.apply(StandardCodecs.list()), i -> convertToList(i.palette),
            m -> new MapPalette(convertToMap(m))
    );

    private static final Codec<Pair<Integer, ResourceLocation>> PALETTE_ENTRY_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("id").forGetter(Pair::getLeft),
            ResourceLocation.CODEC.fieldOf("category").forGetter(Pair::getRight)
    ).apply(instance, Pair::of));

    public static final Codec<MapPalette> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            PALETTE_ENTRY_CODEC.listOf().fieldOf("palette").forGetter(i -> convertToList(i.palette))
    ).apply(instance, m -> new MapPalette(convertToMap(m))));

    private static List<Pair<Integer, ResourceLocation>> convertToList(Map<Integer, ResourceLocation> map) {
        return map.entrySet().stream()
                .map(entry -> Pair.of(entry.getKey(), entry.getValue()))
                .toList();
    }

    private static Map<Integer, ResourceLocation> convertToMap(List<Pair<Integer, ResourceLocation>> list) {
        Map<Integer, ResourceLocation> map = new HashMap<>();
        for (Pair<Integer, ResourceLocation> entry : list) {
            map.put(entry.getLeft(), entry.getRight());
        }
        return map;
    }
}
