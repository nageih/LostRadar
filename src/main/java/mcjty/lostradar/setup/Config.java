package mcjty.lostradar.setup;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

public class Config {

    public static ForgeConfigSpec.IntValue SEARCH_RADIUS;

    public static void register() {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.comment("General settings").push("general");

        SEARCH_RADIUS = builder
                .comment("The radius of the search area for the radar. This is measured in multiples of 8 chunks")
                .defineInRange("searchRadius", 10, 1, 10000);

        builder.pop();

        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, builder.build());
    }

}
