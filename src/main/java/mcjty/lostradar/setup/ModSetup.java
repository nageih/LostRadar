package mcjty.lostradar.setup;

import mcjty.lib.setup.DefaultModSetup;
import mcjty.lostradar.EventHandlers;
import mcjty.lostradar.compat.LostCitiesCompat;
import mcjty.lostradar.data.PlayerMapData;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

import static mcjty.lostradar.LostRadar.MODID;

public class ModSetup extends DefaultModSetup {

    public static final ResourceLocation PLAYER_MAP_DATA_KEY = new ResourceLocation(MODID, "playermapdata");
    public static Capability<PlayerMapData> PLAYER_MAP_DATA = CapabilityManager.get(new CapabilityToken<>(){});


    public void init(FMLCommonSetupEvent e) {
        MinecraftForge.EVENT_BUS.register(new EventHandlers());
        Messages.registerMessages();
    }

    @Override
    protected void setupModCompat() {
        LostCitiesCompat.setupLostCities();
    }
}
