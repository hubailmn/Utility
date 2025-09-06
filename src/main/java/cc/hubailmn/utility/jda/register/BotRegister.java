package cc.hubailmn.utility.jda.register;

import cc.hubailmn.utility.BasePlugin;
import cc.hubailmn.utility.jda.BaseBot;
import cc.hubailmn.utility.jda.commands.BotCommandBuilder;
import cc.hubailmn.utility.jda.commands.BotCommandUtil;
import cc.hubailmn.utility.jda.commands.BotSubCommandBuilder;
import cc.hubailmn.utility.jda.commands.annotation.BotCommand;
import cc.hubailmn.utility.jda.commands.annotation.BotSubCommand;
import cc.hubailmn.utility.jda.listener.ListenerBuilder;
import cc.hubailmn.utility.jda.listener.annotation.BotListener;
import cc.hubailmn.utility.jda.modal.ModalBuilder;
import cc.hubailmn.utility.jda.modal.annotation.BotModal;
import cc.hubailmn.utility.plugin.CSend;
import cc.hubailmn.utility.registry.ClasspathScanner;
import lombok.Getter;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.Set;

public class BotRegister {

    @Getter
    private static final String BASE_PACKAGE = BasePlugin.getInstance().getPackageName() + ".";

    public static void commands() {
        subCommands();

        scanAndRegister(
                ClasspathScanner.getTypesAnnotatedWith(
                        BotCommand.class,
                        BASE_PACKAGE + "discord.command"
                ), "Command", clazz -> {
                    if (!BotCommandBuilder.class.isAssignableFrom(clazz)) {
                        CSend.warn(clazz.getName() + " is annotated with @BotCommand but does not extend CommandBuilder.");
                        return;
                    }

                    @SuppressWarnings("unchecked")
                    Class<? extends BotCommandBuilder> typedClass = (Class<? extends BotCommandBuilder>) clazz;

                    BotCommandBuilder commandBuilder = typedClass.getDeclaredConstructor().newInstance();
                    BaseBot.getInstance().getInstanceManager().addCommand(typedClass, commandBuilder);
                    BaseBot.getInstance().getShardManager().addEventListener(commandBuilder);
                    BotCommandUtil.addCommand(commandBuilder.getCommandData());
                });
    }

    public static void subCommands() {
        scanAndRegister(
                ClasspathScanner.getTypesAnnotatedWith(
                        BotSubCommand.class,
                        BASE_PACKAGE + "discord.command"
                ), "Sub Command", clazz -> {
                    if (!BotSubCommandBuilder.class.isAssignableFrom(clazz)) {
                        CSend.warn(clazz.getName() + " is annotated with @BotSubCommand but does not extend SubCommandBuilder.");
                        return;
                    }

                    @SuppressWarnings("unchecked")
                    Class<? extends BotSubCommandBuilder> typedClass = (Class<? extends BotSubCommandBuilder>) clazz;

                    BotSubCommandBuilder subCommandBuilder = typedClass.getDeclaredConstructor().newInstance();
                    BaseBot.getInstance().getInstanceManager().addSubCommand(typedClass, subCommandBuilder);
                    BaseBot.getInstance().getShardManager().addEventListener(subCommandBuilder);
                });
    }

    public static void listeners() {
        scanAndRegister(
                ClasspathScanner.getTypesAnnotatedWith(
                        BotListener.class,
                        BASE_PACKAGE + "discord.listener"
                ), "Event Listener", clazz -> {
                    if (!ListenerBuilder.class.isAssignableFrom(clazz)) {
                        CSend.warn(clazz.getName() + " is annotated with @BotListener but does not extend ListenerBuilder.");
                        return;
                    }

                    @SuppressWarnings("unchecked")
                    Class<? extends ListenerAdapter> typedClass = (Class<? extends ListenerAdapter>) clazz;

                    ListenerAdapter listenerInstance = typedClass.getDeclaredConstructor().newInstance();
                    BaseBot.getInstance().getInstanceManager().addListener(typedClass, listenerInstance);
                    BaseBot.getInstance().getShardManager().addEventListener(listenerInstance);
                });
    }

    public static void modals() {
        scanAndRegister(
                ClasspathScanner.getTypesAnnotatedWith(
                        BotModal.class,
                        BASE_PACKAGE + ".modal"
                ), "Modal", clazz -> {
                    if (!ModalBuilder.class.isAssignableFrom(clazz)) {
                        CSend.warn(clazz.getName() + " is annotated with @BotModal but does not extend ModalBuilder.");
                        return;
                    }

                    @SuppressWarnings("unchecked")
                    Class<? extends ModalBuilder> typedClass = (Class<? extends ModalBuilder>) clazz;

                    ModalBuilder modalInstance = typedClass.getDeclaredConstructor().newInstance();
                    BaseBot.getInstance().getInstanceManager().addModal(typedClass, modalInstance);
                    BaseBot.getInstance().getShardManager().addEventListener(modalInstance);
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