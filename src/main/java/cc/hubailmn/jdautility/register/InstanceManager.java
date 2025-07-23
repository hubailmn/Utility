package cc.hubailmn.jdautility.register;

import cc.hubailmn.jdautility.commands.BotCommandBuilder;
import cc.hubailmn.jdautility.commands.BotSubCommandBuilder;
import cc.hubailmn.jdautility.modal.ModalBuilder;
import cc.hubailmn.utility.interaction.CSend;
import lombok.Getter;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.*;

public class InstanceManager {

    @Getter
    private static final Map<Class<? extends BotCommandBuilder>, BotCommandBuilder> commandCache = new HashMap<>();

    @Getter
    private static final Map<Class<? extends BotSubCommandBuilder>, BotSubCommandBuilder> subCommands = new HashMap<>();

    @Getter
    private static final Map<Class<? extends ListenerAdapter>, ListenerAdapter> listeners = new HashMap<>();

    @Getter
    private static final Map<Class<? extends ModalBuilder>, ModalBuilder> modals = new HashMap<>();

    private static <T> void addInternal(Class<? extends T> clazz, T instance, Map<Class<? extends T>, T> cacheMap) {
        if (!cacheMap.containsKey(clazz)) {
            cacheMap.put(clazz, instance);
            CSend.debug("Registered instance: " + clazz.getSimpleName());
        } else {
            CSend.warn("Warning: Instance of " + clazz.getSimpleName() + " already registered.");
        }
    }

    private static <T> T getInternal(Class<? extends T> clazz, Map<Class<? extends T>, T> cacheMap) {
        return cacheMap.get(clazz);
    }

    private static <T> void unregisterInternal(Class<? extends T> clazz, Map<Class<? extends T>, T> cacheMap) {
        if (cacheMap.containsKey(clazz)) {
            cacheMap.remove(clazz);
            CSend.debug("Unregistered instance: " + clazz.getSimpleName());
        } else {
            CSend.warn("No instance of " + clazz.getSimpleName() + " found to unregister.");
        }
    }

    private static <T> boolean isRegisteredInternal(Class<? extends T> clazz, Map<Class<? extends T>, T> cacheMap) {
        return cacheMap.containsKey(clazz);
    }

    private static <T> Set<Class<? extends T>> listRegisteredInternal(Map<Class<? extends T>, T> cacheMap) {
        return Collections.unmodifiableSet(cacheMap.keySet());
    }

    private static <T> Collection<T> getAllInternal(Map<Class<? extends T>, T> cacheMap) {
        return Collections.unmodifiableCollection(cacheMap.values());
    }

    public static void addCommand(Class<? extends BotCommandBuilder> clazz, BotCommandBuilder instance) {
        addInternal(clazz, instance, commandCache);
    }

    public static BotCommandBuilder getCommand(Class<? extends BotCommandBuilder> clazz) {
        return getInternal(clazz, commandCache);
    }

    public static void unregisterCommand(Class<? extends BotCommandBuilder> clazz) {
        unregisterInternal(clazz, commandCache);
    }

    public static boolean isCommandRegistered(Class<? extends BotCommandBuilder> clazz) {
        return isRegisteredInternal(clazz, commandCache);
    }

    public static Set<Class<? extends BotCommandBuilder>> listRegisteredCommands() {
        return listRegisteredInternal(commandCache);
    }

    public static Collection<BotCommandBuilder> getAllCommands() {
        return getAllInternal(commandCache);
    }

    public static void addSubCommand(Class<? extends BotSubCommandBuilder> clazz, BotSubCommandBuilder instance) {
        addInternal(clazz, instance, subCommands);
    }

    public static BotSubCommandBuilder getSubCommand(Class<? extends BotSubCommandBuilder> clazz) {
        return getInternal(clazz, subCommands);
    }

    public static void unregisterSubCommand(Class<? extends BotSubCommandBuilder> clazz) {
        unregisterInternal(clazz, subCommands);
    }

    public static boolean isSubCommandRegistered(Class<? extends BotSubCommandBuilder> clazz) {
        return isRegisteredInternal(clazz, subCommands);
    }

    public static Set<Class<? extends BotSubCommandBuilder>> listRegisteredSubCommands() {
        return listRegisteredInternal(subCommands);
    }

    public static Collection<BotSubCommandBuilder> getAllSubCommands() {
        return getAllInternal(subCommands);
    }

    public static void addListener(Class<? extends ListenerAdapter> clazz, ListenerAdapter instance) {
        addInternal(clazz, instance, listeners);
    }

    public static ListenerAdapter getListener(Class<? extends ListenerAdapter> clazz) {
        return getInternal(clazz, listeners);
    }

    public static void unregisterListener(Class<? extends ListenerAdapter> clazz) {
        unregisterInternal(clazz, listeners);
    }

    public static boolean isListenerRegistered(Class<? extends ListenerAdapter> clazz) {
        return isRegisteredInternal(clazz, listeners);
    }

    public static Set<Class<? extends ListenerAdapter>> listRegisteredListeners() {
        return listRegisteredInternal(listeners);
    }

    public static Collection<ListenerAdapter> getAllListeners() {
        return getAllInternal(listeners);
    }

    public static void addModal(Class<? extends ModalBuilder> clazz, ModalBuilder instance) {
        addInternal(clazz, instance, modals);
    }

    public static ModalBuilder getModal(Class<? extends ModalBuilder> clazz) {
        return getInternal(clazz, modals);
    }

    public static void unregisterModal(Class<? extends ModalBuilder> clazz) {
        unregisterInternal(clazz, modals);
    }

    public static boolean isModalRegistered(Class<? extends ModalBuilder> clazz) {
        return isRegisteredInternal(clazz, modals);
    }

    public static Set<Class<? extends ModalBuilder>> listRegisteredModals() {
        return listRegisteredInternal(modals);
    }

    public static Collection<ModalBuilder> getAllModals() {
        return getAllInternal(modals);
    }

    public static boolean isRegistered(Class<?> clazz) {
        return commandCache.containsKey(clazz) ||
                subCommands.containsKey(clazz) ||
                listeners.containsKey(clazz) ||
                modals.containsKey(clazz);
    }

    public static void unregister(Class<?> clazz) {
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

    public static Set<Class<?>> listAllRegistered() {
        Set<Class<?>> allRegistered = new HashSet<>();
        allRegistered.addAll(commandCache.keySet());
        allRegistered.addAll(subCommands.keySet());
        allRegistered.addAll(listeners.keySet());
        allRegistered.addAll(modals.keySet());
        return Collections.unmodifiableSet(allRegistered);
    }

    public static void reload() {
        CSend.info("Reloading InstanceManager caches...");

        commandCache.clear();
        subCommands.clear();
        listeners.clear();
        modals.clear();

        CSend.debug("InstanceManager caches reloaded. Instances need to be re-registered.");
    }
}