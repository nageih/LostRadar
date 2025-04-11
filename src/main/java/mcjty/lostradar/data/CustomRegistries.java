package mcjty.lostradar.data;

import mcjty.lostradar.LostRadar;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DataPackRegistryEvent;
import net.minecraftforge.registries.DeferredRegister;

public class CustomRegistries {

    public static final ResourceKey<Registry<MapPalette>> PALETTE_REGISTRY_KEY = ResourceKey.createRegistryKey(new ResourceLocation(LostRadar.MODID, "radar"));
    public static final DeferredRegister<MapPalette> PALETTE_DEFERRED_REGISTER = DeferredRegister.create(PALETTE_REGISTRY_KEY, LostRadar.MODID);

    public static void init() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        PALETTE_DEFERRED_REGISTER.register(bus);
    }

    public static void onDataPackRegistry(DataPackRegistryEvent.NewRegistry event) {
        event.dataPackRegistry(PALETTE_REGISTRY_KEY, MapPalette.CODEC);
    }
}
