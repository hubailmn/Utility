package cc.hubailmn.utility.jda.modal;

import cc.hubailmn.utility.jda.modal.annotation.BotModal;
import cc.hubailmn.utility.plugin.CSend;
import lombok.Getter;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.modals.Modal;

import java.util.LinkedHashMap;
import java.util.Map;

@Getter
public abstract class ModalBuilder extends ListenerAdapter {

    private final Map<String, TextInput> inputs = new LinkedHashMap<>();
    private String id;
    private String title;
    private Modal modal;

    public ModalBuilder() {
        BotModal annotation = this.getClass().getAnnotation(BotModal.class);
        if (annotation == null) {
            CSend.error("Modal class must be annotated with @BotModal.");
            return;
        }

        this.id = annotation.id();
        this.title = annotation.title();

        addInputs();
        build();
        CSend.debug("Registered modal: " + id);
    }

    protected void insertInput(String name, TextInput input) {
        inputs.put(name, input);
    }

    private void build() {
        Modal.Builder builder = Modal.create(id, title);
        for (TextInput input : inputs.values()) {
            builder.addActionRow(input);
        }
        this.modal = builder.build();
    }

    @Override
    public void onModalInteraction(ModalInteractionEvent e) {
        if (!e.getModalId().equals(getId())) return;

        try {
            handle(e);
        } catch (Exception ex) {
            CSend.error("Modal handling failed for " + id);
            CSend.error(ex);
            e.reply("❌ Something went wrong while processing the modal.").setEphemeral(true).queue();
        }
    }

    public abstract void handle(ModalInteractionEvent e);

    public abstract void addInputs();

    public Modal getBuiltModal() {
        if (modal == null) build();
        return modal;
    }
}
