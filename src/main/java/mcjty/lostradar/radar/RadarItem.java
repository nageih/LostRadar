package mcjty.lostradar.radar;

import mcjty.lostradar.LostRadar;
import net.minecraft.world.item.Item;

public class RadarItem extends Item {

    public RadarItem() {
        super(LostRadar.setup.defaultProperties().stacksTo(1).defaultDurability(1));
    }
}
