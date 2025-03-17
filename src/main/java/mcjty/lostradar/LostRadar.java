package mcjty.lostradar;

import mcjty.lostradar.data.CustomRegistries;
import mcjty.lostradar.setup.Config;
import mcjty.lostradar.setup.ModSetup;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(LostRadar.MODID)
public class LostRadar {
    public static final String MODID = "lostradar";

    public static Logger logger = LogManager.getLogger(LostRadar.MODID);
    public static ModSetup setup = new ModSetup();

    public static LostRadar instance;

    public LostRadar() {
        instance = this;
        Config.register();
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, Config.SERVER_CONFIG);
        CustomRegistries.init();

        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.addListener(setup::init);
        bus.addListener(CustomRegistries::onDataPackRegistry);
    }
}
