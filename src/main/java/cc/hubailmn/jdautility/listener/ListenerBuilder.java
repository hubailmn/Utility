package cc.hubailmn.jdautility.listener;

import cc.hubailmn.jdautility.listener.annotation.BotListener;
import cc.hubailmn.utility.interaction.CSend;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

@EqualsAndHashCode(callSuper = true)
@Data
public class ListenerBuilder extends ListenerAdapter {

    private String name;

    public ListenerBuilder() {
        BotListener annotation = this.getClass().getAnnotation(BotListener.class);
        if (annotation == null) {
            CSend.error("Listener class must be annotated with @BotListener.");
            return;
        }

        this.name = annotation.name();
        CSend.info("Registered listener: " + name);
    }
}
