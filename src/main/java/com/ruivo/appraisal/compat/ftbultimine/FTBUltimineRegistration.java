package com.ruivo.appraisal.compat.ftbultimine;

import dev.ftb.mods.ftbultimine.api.rightclick.RegisterRightClickHandlerEvent;

public final class FTBUltimineRegistration {

    private FTBUltimineRegistration() {
    }

    public static void register() {
        RegisterRightClickHandlerEvent.REGISTER.register(dispatcher ->
                dispatcher.registerHandler(new MagnifyingGlassRightClickHandler()));
    }
}
