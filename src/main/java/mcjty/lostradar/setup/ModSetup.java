package mcjty.lostradar.setup;

import mcjty.lib.setup.DefaultModSetup;
import mcjty.lostradar.ForgeEventHandlers;
import mcjty.lostradar.compat.LostCitiesCompat;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

public class ModSetup extends DefaultModSetup {

    public void init(FMLCommonSetupEvent e) {
        MinecraftForge.EVENT_BUS.register(new ForgeEventHandlers());
    }

    @Override
    protected void setupModCompat() {
        LostCitiesCompat.setupLostCities();
    }
}
