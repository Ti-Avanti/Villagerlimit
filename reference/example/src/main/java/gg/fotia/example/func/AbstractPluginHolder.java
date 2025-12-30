package gg.fotia.example.func;

import gg.fotia.example.example;

@SuppressWarnings({"unused"})
public abstract class AbstractPluginHolder extends top.mrxiaom.pluginbase.func.AbstractPluginHolder<example> {
    public AbstractPluginHolder(example plugin) {
        super(plugin);
    }

    public AbstractPluginHolder(example plugin, boolean register) {
        super(plugin, register);
    }
}
