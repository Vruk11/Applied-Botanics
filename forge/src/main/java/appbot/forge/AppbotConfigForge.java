package appbot.forge;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

import appbot.AppbotConfig;
import appbot.AppbotConfig.AppBotCommonConfig;

public class AppbotConfigForge {

    private static class Common implements AppBotCommonConfig {
        public final ForgeConfigSpec.IntValue manaPerOperation;

        public Common(ForgeConfigSpec.Builder builder) {
            builder.push("data");
            manaPerOperation = builder
                    .comment("The amount of mana an export bus will transfer per operation")
                    .defineInRange("manaPerOperation", 1000, 0, Integer.MAX_VALUE);
        }

        @Override
        public int manaPerOperation() {
            return manaPerOperation.get();
        }

    }

    private static final Common COMMON;
    private static final ForgeConfigSpec COMMON_SPEC;
    static {
        final Pair<Common, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Common::new);
        COMMON_SPEC = specPair.getRight();
        COMMON = specPair.getLeft();
    }

    public static void setup() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, COMMON_SPEC);
        AppbotConfig.setCommon(COMMON);
    }
}
