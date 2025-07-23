package cc.hubailmn.jdautility.register;

import cc.hubailmn.jdautility.BaseBot;
import cc.hubailmn.jdautility.commands.BotCommandBuilder;
import cc.hubailmn.jdautility.commands.BotCommandUtil;
import cc.hubailmn.jdautility.commands.BotSubCommandBuilder;
import cc.hubailmn.jdautility.commands.annotation.BotCommand;
import cc.hubailmn.jdautility.commands.annotation.BotSubCommand;
import cc.hubailmn.jdautility.listener.ListenerBuilder;
import cc.hubailmn.jdautility.listener.annotation.BotListener;
import cc.hubailmn.jdautility.modal.ModalBuilder;
import cc.hubailmn.jdautility.modal.annotation.BotModal;
import cc.hubailmn.utility.BasePlugin;
import cc.hubailmn.utility.interaction.CSend;
import cc.hubailmn.utility.registry.ReflectionsUtil;
import lombok.Getter;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.reflections.Reflections;

import java.util.Set;

public class BotRegister {

    @Getter
    private static final String BASE_PACKAGE = BasePlugin.getPackageName() + ".";

    public static void commands() {
        subCommands();
        Reflections reflections = ReflectionsUtil.build(
                BASE_PACKAGE + "discord.command"
        );

        Set<Class<?>> classes = reflections.getTypesAnnotatedWith(BotCommand.class);
        scanAndRegister(classes, "Command", clazz -> {
            if (!BotCommandBuilder.class.isAssignableFrom(clazz)) {
                CSend.warn(clazz.getName() + " is annotated with @BotCommand but does not extend CommandBuilder.");
                return;
            }

            @SuppressWarnings("unchecked")
            Class<? extends BotCommandBuilder> typedClass = (Class<? extends BotCommandBuilder>) clazz;

            BotCommandBuilder commandBuilder = typedClass.getDeclaredConstructor().newInstance();
            InstanceManager.addCommand(typedClass, commandBuilder);
            BaseBot.getShardManager().addEventListener(commandBuilder);
            BotCommandUtil.addCommand(commandBuilder.getCommandData());
        });
    }

    public static void subCommands() {
        Reflections reflections = ReflectionsUtil.build(
                BASE_PACKAGE + "discord.command"
        );

        Set<Class<?>> classes = reflections.getTypesAnnotatedWith(BotSubCommand.class);
        scanAndRegister(classes, "Sub Command", clazz -> {
            if (!BotSubCommandBuilder.class.isAssignableFrom(clazz)) {
                CSend.warn(clazz.getName() + " is annotated with @BotSubCommand but does not extend SubCommandBuilder.");
                return;
            }

            @SuppressWarnings("unchecked")
            Class<? extends BotSubCommandBuilder> typedClass = (Class<? extends BotSubCommandBuilder>) clazz;

            BotSubCommandBuilder subCommandBuilder = typedClass.getDeclaredConstructor().newInstance();
            InstanceManager.addSubCommand(typedClass, subCommandBuilder);
            BaseBot.getShardManager().addEventListener(subCommandBuilder);
        });
    }

    public static void listeners() {
        Reflections reflections = ReflectionsUtil.build(
                BASE_PACKAGE + "discord.listener"
        );

        Set<Class<?>> classes = reflections.getTypesAnnotatedWith(BotListener.class);
        scanAndRegister(classes, "Event Listener", clazz -> {
            if (!ListenerBuilder.class.isAssignableFrom(clazz)) {
                CSend.warn(clazz.getName() + " is annotated with @BotListener but does not extend ListenerBuilder.");
                return;
            }

            @SuppressWarnings("unchecked")
            Class<? extends ListenerAdapter> typedClass = (Class<? extends ListenerAdapter>) clazz;

            ListenerAdapter listenerInstance = typedClass.getDeclaredConstructor().newInstance();
            InstanceManager.addListener(typedClass, listenerInstance);
            BaseBot.getShardManager().addEventListener(listenerInstance);
        });
    }

    public static void modals() {
        Reflections reflections = ReflectionsUtil.build(
                BASE_PACKAGE + ".modal"
        );

        Set<Class<?>> classes = reflections.getTypesAnnotatedWith(BotModal.class);
        scanAndRegister(classes, "Modal", clazz -> {
            if (!ModalBuilder.class.isAssignableFrom(clazz)) {
                CSend.warn(clazz.getName() + " is annotated with @BotModal but does not extend ModalBuilder.");
                return;
            }

            @SuppressWarnings("unchecked")
            Class<? extends ModalBuilder> typedClass = (Class<? extends ModalBuilder>) clazz;

            ModalBuilder modalInstance = typedClass.getDeclaredConstructor().newInstance();
            InstanceManager.addModal(typedClass, modalInstance);
            BaseBot.getShardManager().addEventListener(modalInstance);
        });
    }

    private static <T> void scanAndRegister(Set<Class<? extends T>> classes, String label, RegistryAction action) {
        if (classes.isEmpty()) {
            CSend.debug("No " + label + "s found to register.");
            return;
        }

        for (Class<?> clazz : classes) {
            try {
                action.execute(clazz);
            } catch (Exception e) {
                CSend.error("Failed to register " + label + ": " + clazz.getSimpleName() + " - " + e.getMessage());
                CSend.error(e);
            }
        }
    }

    @FunctionalInterface
    private interface RegistryAction {
        void execute(Class<?> clazz) throws Exception;
    }
}