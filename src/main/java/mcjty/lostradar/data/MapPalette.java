package mcjty.lostradar.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mcjty.lib.varia.codec.StandardCodecs;
import mcjty.lib.varia.codec.StreamCodec;
import net.minecraft.network.FriendlyByteBuf;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class represents a palette for a map. It maps id's to categories
 */
public record MapPalette(Map<Integer, String> palette) {

    private static final StreamCodec<FriendlyByteBuf, Pair<Integer, String>> PALETTE_ENTRY_STREAM_CODEC = StreamCodec.composite(
            StandardCodecs.INT, Pair::getLeft,
            StandardCodecs.STRING_UTF8, Pair::getRight,
            Pair::of
    );

    public static final StreamCodec<FriendlyByteBuf, MapPalette> STREAM_CODEC = StreamCodec.composite(
            PALETTE_ENTRY_STREAM_CODEC.apply(StandardCodecs.list()), i -> convertToList(i.palette),
            m -> new MapPalette(convertToMap(m))
    );

    private static final Codec<Pair<Integer, String>> PALETTE_ENTRY_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("id").forGetter(Pair::getLeft),
            Codec.STRING.fieldOf("category").forGetter(Pair::getRight)
    ).apply(instance, Pair::of));

    public static final Codec<MapPalette> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            PALETTE_ENTRY_CODEC.listOf().fieldOf("palette").forGetter(i -> convertToList(i.palette))
    ).apply(instance, m -> new MapPalette(convertToMap(m))));

    private static List<Pair<Integer, String>> convertToList(Map<Integer, String> map) {
        return map.entrySet().stream()
                .map(entry -> Pair.of(entry.getKey(), entry.getValue()))
                .toList();
    }

    private static Map<Integer, String> convertToMap(List<Pair<Integer, String>> list) {
        Map<Integer, String> map = new HashMap<>();
        for (Pair<Integer, String> entry : list) {
            map.put(entry.getLeft(), entry.getRight());
        }
        return map;
    }
}
