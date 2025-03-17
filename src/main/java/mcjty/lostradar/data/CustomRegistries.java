package mcjty.lostradar.data;

import net.minecraftforge.registries.DataPackRegistryEvent;

public class CustomRegistries {

//    public static final ResourceKey<Registry<MobSettings>> BUILDING_REGISTRY_KEY = ResourceKey.createRegistryKey(new ResourceLocation(LostSouls.MODID, "buildings"));
//    public static final DeferredRegister<MobSettings> BUILDING_DEFERRED_REGISTER = DeferredRegister.create(BUILDING_REGISTRY_KEY, LostSouls.MODID);

    public static void init() {
//        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
//        BUILDING_DEFERRED_REGISTER.register(bus);
    }

    public static void onDataPackRegistry(DataPackRegistryEvent.NewRegistry event) {
//        event.dataPackRegistry(BUILDING_REGISTRY_KEY, MobSettings.CODEC);
    }
}
