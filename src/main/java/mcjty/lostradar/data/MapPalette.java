package mcjty.lostradar.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mcjty.lostradar.LostRadar;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * This class represents a palette for a map
 */
public record MapPalette(List<PaletteEntry> palette) {

    public record PaletteEntry(String name, int color, String translatableKey, int iconU, int iconV, List<ResourceLocation> buildings) {
    }

    public static final PaletteEntry CITY = new PaletteEntry("city", 0xAAAAAA, "lostradar.city", -1, -1, List.of());
    public static final PaletteEntry HIGHWAY = new PaletteEntry("highway", 0x000000, "lostradar.highway", -1, -1, List.of());

    private static final Codec<PaletteEntry> PALETTE_ENTRY_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("name").forGetter(PaletteEntry::name),
            Codec.INT.fieldOf("color").forGetter(PaletteEntry::color),
            Codec.STRING.fieldOf("translatableKey").forGetter(PaletteEntry::translatableKey),
            Codec.INT.fieldOf("u").forGetter(PaletteEntry::iconU),
            Codec.INT.fieldOf("v").forGetter(PaletteEntry::iconV),
            Codec.list(ResourceLocation.CODEC).fieldOf("buildings").forGetter(PaletteEntry::buildings)
    ).apply(instance, PaletteEntry::new));

    public static final Codec<MapPalette> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            PALETTE_ENTRY_CODEC.listOf().fieldOf("palette").forGetter(i -> i.palette)
    ).apply(instance, MapPalette::new));

    public static MapPalette getDefaultPalette(Level level) {
        Registry<MapPalette> registry = level.registryAccess().registryOrThrow(CustomRegistries.PALETTE_REGISTRY_KEY);
        return registry.get(new ResourceLocation(LostRadar.MODID, "default"));
    }

}
