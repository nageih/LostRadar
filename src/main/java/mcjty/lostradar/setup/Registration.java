package mcjty.lostradar.setup;

import mcjty.lib.setup.DeferredItem;
import mcjty.lib.setup.DeferredItems;
import mcjty.lostradar.LostRadar;
import mcjty.lostradar.radar.RadarItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;

import static mcjty.lostradar.LostRadar.MODID;
import static mcjty.lostradar.LostRadar.tab;

public class Registration {

    public static final DeferredItems ITEMS = DeferredItems.create(MODID);
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public static RegistryObject<CreativeModeTab> TAB = TABS.register("lostradar", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.lostradar.main"))
            .icon(() -> new ItemStack(RADAR.get()))
            .withTabsBefore(CreativeModeTabs.SPAWN_EGGS)
            .displayItems((featureFlags, output) -> {
                LostRadar.setup.populateTab(output);
            })
            .build());

    public static final DeferredItem<RadarItem> RADAR = ITEMS.register("radar", tab(RadarItem::new));

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
        TABS.register(bus);
    }

    @NotNull
    public static Item.Properties createStandardProperties() {
        return LostRadar.setup.defaultProperties();
    }
}
