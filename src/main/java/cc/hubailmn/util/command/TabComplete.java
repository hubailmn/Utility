package cc.hubailmn.util.command;

import lombok.Getter;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Getter
public class TabComplete {
    private final CommandSender sender;
    private final Command command;
    private final String alias;
    private final String[] args;
    private final Map<Integer, List<String>> entries = new HashMap<>();

    public TabComplete(CommandSender sender, Command command, String alias, String[] args) {
        this.sender = sender;
        this.command = command;
        this.alias = alias;
        this.args = args;
    }

    public TabComplete add(int atIndex, String[] entry, Predicate<String[]> condition) {
        if (condition.test(this.args)) {
            return add(atIndex, entry);
        }
        return this;
    }

    public TabComplete add(int atIndex, String[] entry) {
        atIndex = Math.max(1, atIndex);
        entries.put(atIndex, Arrays.asList(entry));
        return this;
    }

    public List<String> build() {
        List<String> suggestions = entries.getOrDefault(args.length, Collections.emptyList());

        return suggestions.stream()
                .filter(s -> s.toLowerCase().contains(args[args.length - 1].toLowerCase()))
                .collect(Collectors.toList());
    }
}
