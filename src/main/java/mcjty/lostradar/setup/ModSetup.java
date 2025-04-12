package mcjty.lostradar.setup;

import mcjty.lib.setup.DefaultModSetup;
import mcjty.lostradar.EventHandlers;
import mcjty.lostradar.compat.LostCitiesCompat;
import mcjty.lostradar.data.PlayerMapKnowledge;
import mcjty.lostradar.network.Messages;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

import static mcjty.lostradar.LostRadar.MODID;

public class ModSetup extends DefaultModSetup {

    public static final ResourceLocation PLAYER_KNOWLEDGE_KEY = new ResourceLocation(MODID, "playermapknowledge");
    public static Capability<PlayerMapKnowledge> PLAYER_KNOWLEDGE = CapabilityManager.get(new CapabilityToken<>(){});


    public void init(FMLCommonSetupEvent e) {
        super.init(e);
        MinecraftForge.EVENT_BUS.register(new EventHandlers());
        Messages.registerMessages();
    }

    @Override
    protected void setupModCompat() {
        LostCitiesCompat.setupLostCities();
    }
}
