package com.ruivo.appraisal.compat.ftbultimine;

import net.neoforged.fml.ModList;

/**
 * FTB Ultimine 是可选的联动模组。这里只做存在性判断，真正的 API 调用放在
 * {@link FTBUltimineRegistration} 里，避免模组缺席时加载到它的类型。
 */
public final class FTBUltimineCompat {

    public static final String MOD_ID = "ftbultimine";

    private FTBUltimineCompat() {
    }

    public static boolean isLoaded() {
        return ModList.get().isLoaded(MOD_ID);
    }

    public static void init() {
        if (ModList.get().isLoaded(MOD_ID)) {
            FTBUltimineRegistration.register();
        }
    }
}
