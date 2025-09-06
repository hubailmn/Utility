package cc.hubailmn.utility.jda.register;

import cc.hubailmn.utility.plugin.CSend;
import cc.hubailmn.utility.jda.commands.BotCommandBuilder;
import cc.hubailmn.utility.jda.commands.BotSubCommandBuilder;
import cc.hubailmn.utility.jda.modal.ModalBuilder;
import lombok.Getter;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.*;

@Getter
public class InstanceManager {

    private final Map<Class<? extends BotCommandBuilder>, BotCommandBuilder> commandCache = new HashMap<>();
    private final Map<Class<? extends BotSubCommandBuilder>, BotSubCommandBuilder> subCommands = new HashMap<>();
    private final Map<Class<? extends ListenerAdapter>, ListenerAdapter> listeners = new HashMap<>();
    private final Map<Class<? extends ModalBuilder>, ModalBuilder> modals = new HashMap<>();

    private <T> void addInternal(Class<? extends T> clazz, T instance, Map<Class<? extends T>, T> cacheMap) {
        if (!cacheMap.containsKey(clazz)) {
            cacheMap.put(clazz, instance);
            CSend.debug("Registered instance: " + clazz.getSimpleName());
        } else {
            CSend.warn("Warning: Instance of " + clazz.getSimpleName() + " already registered.");
        }
    }

    private <T> T getInternal(Class<? extends T> clazz, Map<Class<? extends T>, T> cacheMap) {
        return cacheMap.get(clazz);
    }

    private <T> void unregisterInternal(Class<? extends T> clazz, Map<Class<? extends T>, T> cacheMap) {
        if (cacheMap.containsKey(clazz)) {
            cacheMap.remove(clazz);
            CSend.debug("Unregistered instance: " + clazz.getSimpleName());
        } else {
            CSend.warn("No instance of " + clazz.getSimpleName() + " found to unregister.");
        }
    }

    private <T> boolean isRegisteredInternal(Class<? extends T> clazz, Map<Class<? extends T>, T> cacheMap) {
        return cacheMap.containsKey(clazz);
    }

    private <T> Set<Class<? extends T>> listRegisteredInternal(Map<Class<? extends T>, T> cacheMap) {
        return Collections.unmodifiableSet(cacheMap.keySet());
    }

    private <T> Collection<T> getAllInternal(Map<Class<? extends T>, T> cacheMap) {
        return Collections.unmodifiableCollection(cacheMap.values());
    }

    public void addCommand(Class<? extends BotCommandBuilder> clazz, BotCommandBuilder instance) {
        addInternal(clazz, instance, commandCache);
    }

    public BotCommandBuilder getCommand(Class<? extends BotCommandBuilder> clazz) {
        return getInternal(clazz, commandCache);
    }

    public void unregisterCommand(Class<? extends BotCommandBuilder> clazz) {
        unregisterInternal(clazz, commandCache);
    }

    public boolean isCommandRegistered(Class<? extends BotCommandBuilder> clazz) {
        return isRegisteredInternal(clazz, commandCache);
    }

    public Set<Class<? extends BotCommandBuilder>> listRegisteredCommands() {
        return listRegisteredInternal(commandCache);
    }

    public Collection<BotCommandBuilder> getAllCommands() {
        return getAllInternal(commandCache);
    }

    public void addSubCommand(Class<? extends BotSubCommandBuilder> clazz, BotSubCommandBuilder instance) {
        addInternal(clazz, instance, subCommands);
    }

    public BotSubCommandBuilder getSubCommand(Class<? extends BotSubCommandBuilder> clazz) {
        return getInternal(clazz, subCommands);
    }

    public void unregisterSubCommand(Class<? extends BotSubCommandBuilder> clazz) {
        unregisterInternal(clazz, subCommands);
    }

    public boolean isSubCommandRegistered(Class<? extends BotSubCommandBuilder> clazz) {
        return isRegisteredInternal(clazz, subCommands);
    }

    public Set<Class<? extends BotSubCommandBuilder>> listRegisteredSubCommands() {
        return listRegisteredInternal(subCommands);
    }

    public Collection<BotSubCommandBuilder> getAllSubCommands() {
        return getAllInternal(subCommands);
    }

    public void addListener(Class<? extends ListenerAdapter> clazz, ListenerAdapter instance) {
        addInternal(clazz, instance, listeners);
    }

    public ListenerAdapter getListener(Class<? extends ListenerAdapter> clazz) {
        return getInternal(clazz, listeners);
    }

    public void unregisterListener(Class<? extends ListenerAdapter> clazz) {
        unregisterInternal(clazz, listeners);
    }

    public boolean isListenerRegistered(Class<? extends ListenerAdapter> clazz) {
        return isRegisteredInternal(clazz, listeners);
    }

    public Set<Class<? extends ListenerAdapter>> listRegisteredListeners() {
        return listRegisteredInternal(listeners);
    }

    public Collection<ListenerAdapter> getAllListeners() {
        return getAllInternal(listeners);
    }

    public void addModal(Class<? extends ModalBuilder> clazz, ModalBuilder instance) {
        addInternal(clazz, instance, modals);
    }

    public ModalBuilder getModal(Class<? extends ModalBuilder> clazz) {
        return getInternal(clazz, modals);
    }

    public void unregisterModal(Class<? extends ModalBuilder> clazz) {
        unregisterInternal(clazz, modals);
    }

    public boolean isModalRegistered(Class<? extends ModalBuilder> clazz) {
        return isRegisteredInternal(clazz, modals);
    }

    public Set<Class<? extends ModalBuilder>> listRegisteredModals() {
        return listRegisteredInternal(modals);
    }

    public Collection<ModalBuilder> getAllModals() {
        return getAllInternal(modals);
    }

    public boolean isRegistered(Class<?> clazz) {
        return commandCache.containsKey(clazz) ||
                subCommands.containsKey(clazz) ||
                listeners.containsKey(clazz) ||
                modals.containsKey(clazz);
    }

    public void unregister(Class<?> clazz) {
        if (commandCache.containsKey(clazz)) {
            unregisterCommand((Class<? extends BotCommandBuilder>) clazz);
        } else if (subCommands.containsKey(clazz)) {
            unregisterSubCommand((Class<? extends BotSubCommandBuilder>) clazz);
        } else if (listeners.containsKey(clazz)) {
            unregisterListener((Class<? extends ListenerAdapter>) clazz);
        } else if (modals.containsKey(clazz)) {
            unregisterModal((Class<? extends ModalBuilder>) clazz);
        } else {
            CSend.warn("Class " + clazz.getSimpleName() + " not found in any cache to unregister.");
        }
    }

    public Set<Class<?>> listAllRegistered() {
        Set<Class<?>> allRegistered = new HashSet<>();
        allRegistered.addAll(commandCache.keySet());
        allRegistered.addAll(subCommands.keySet());
        allRegistered.addAll(listeners.keySet());
        allRegistered.addAll(modals.keySet());
        return Collections.unmodifiableSet(allRegistered);
    }

    public void reload() {
        CSend.info("Reloading InstanceManager caches...");

        commandCache.clear();
        subCommands.clear();
        listeners.clear();
        modals.clear();

        CSend.debug("InstanceManager caches reloaded. Instances need to be re-registered.");
    }
}