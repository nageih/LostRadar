package mcjty.lostradar;

import mcjty.lostradar.data.CustomRegistries;
import mcjty.lostradar.setup.Config;
import mcjty.lostradar.setup.ModSetup;
import mcjty.lostradar.setup.Registration;
import net.minecraft.world.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.Supplier;

@Mod(LostRadar.MODID)
public class LostRadar {
    public static final String MODID = "lostradar";

    public static Logger logger = LogManager.getLogger(LostRadar.MODID);
    public static ModSetup setup = new ModSetup();

    public static LostRadar instance;

    public LostRadar() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        Dist dist = FMLEnvironment.dist;
        instance = this;

        Config.register();
        CustomRegistries.init();

        Registration.register(bus);
        bus.addListener(setup::init);
        bus.addListener(CustomRegistries::onDataPackRegistry);
    }

    public static <T extends Item> Supplier<T> tab(Supplier<T> supplier) {
        return instance.setup.tab(supplier);
    }
}
