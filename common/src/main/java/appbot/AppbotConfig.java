package appbot;

import dev.architectury.injectables.annotations.ExpectPlatform;

public class AppbotConfig {

    public interface AppBotCommonConfig {
        int manaPerOperation();
    }

    public static AppBotCommonConfig CommonConfig = null;

    public static void setCommon(AppBotCommonConfig config)  {
        CommonConfig = config;
    }
}
