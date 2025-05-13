package mcjty.lostradar.setup;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

public class Config {

    // Server side
    public static ForgeConfigSpec.IntValue SEARCH_RADIUS;
    public static ForgeConfigSpec.IntValue RADAR_MAXENERGY;
    public static ForgeConfigSpec.IntValue RADAR_RECEIVEPERTICK;

    // Client side
    public static ForgeConfigSpec.IntValue HILIGHT_R1;
    public static ForgeConfigSpec.IntValue HILIGHT_G1;
    public static ForgeConfigSpec.IntValue HILIGHT_B1;
    public static ForgeConfigSpec.IntValue HILIGHT_R2;
    public static ForgeConfigSpec.IntValue HILIGHT_G2;
    public static ForgeConfigSpec.IntValue HILIGHT_B2;

    public static void register() {
        registerServerConfigs();
        registerClientConfigs();
    }

    private static void registerServerConfigs() {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.comment("General settings").push("general");

        SEARCH_RADIUS = builder
                .comment("The radius of the search area for the radar. This is measured in multiples of 8 chunks")
                .defineInRange("searchRadius", 10, 1, 10000);
        RADAR_MAXENERGY = builder
                .comment("Maximum RF storage that the radar item can hold")
                .defineInRange("radarMaxRF", 20000, 0, Integer.MAX_VALUE);
        RADAR_RECEIVEPERTICK = builder
                .comment("RF per tick that the the radar item can receive")
                .defineInRange("radarRFPerTick", 100, 0, Integer.MAX_VALUE);

        builder.pop();

        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, builder.build());
    }

    private static void registerClientConfigs() {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.comment("General settings").push("general");

        HILIGHT_R1 = builder
                .comment("The red component of the hilight color one")
                .defineInRange("hilightR1", 255, 0, 255);
        HILIGHT_G1 = builder
                .comment("The green component of the hilight color one")
                .defineInRange("hilightG1", 255, 0, 255);
        HILIGHT_B1 = builder
                .comment("The blue component of the hilight color one")
                .defineInRange("hilightB1", 255, 0, 255);

        HILIGHT_R2 = builder
                .comment("The red component of the hilight color two")
                .defineInRange("hilightR2", 128, 0, 255);
        HILIGHT_G2 = builder
                .comment("The green component of the hilight color two")
                .defineInRange("hilightG2", 128, 0, 255);
        HILIGHT_B2 = builder
                .comment("The blue component of the hilight color two")
                .defineInRange("hilightB2", 128, 0, 255);

        builder.pop();

        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, builder.build());
    }
}
