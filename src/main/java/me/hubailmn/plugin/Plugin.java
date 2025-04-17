package me.hubailmn.plugin;

import me.hubailmn.util.BasePlugin;
import me.hubailmn.util.interaction.CSend;

public class Plugin extends BasePlugin {

    @Override
    public void onEnable() {
        super.onEnable();

        CSend.info("Loading " + getDescription().getName());
    }
}
