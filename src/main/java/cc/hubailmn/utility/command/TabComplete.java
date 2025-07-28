package cc.hubailmn.utility.command;

import lombok.Getter;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;
import java.util.function.Predicate;

@Getter
public class TabComplete {
    private final CommandSender sender;
    private final Command command;
    private final String alias;
    private final String[] args;
    private final TreeMap<Integer, List<String>> entries = new TreeMap<>();

    private String currentArg;
    private int currentArgLength;

    public TabComplete(CommandSender sender, Command command, String alias, String[] args) {
        this.sender = sender;
        this.command = command;
        this.alias = alias;
        this.args = args;
        if (args.length > 0) {
            this.currentArg = args[args.length - 1].toLowerCase();
            this.currentArgLength = currentArg.length();
        }
    }

    public TabComplete add(int atIndex, String[] entry, Predicate<String[]> condition) {
        if (condition != null && !condition.test(this.args)) {
            return this;
        }
        return add(atIndex, entry);
    }

    public TabComplete add(int atIndex, String... entry) {
        if (entry == null || entry.length == 0) {
            return this;
        }
        atIndex = Math.max(1, atIndex);
        entries.computeIfAbsent(atIndex, k -> new ArrayList<>(entry.length)).addAll(List.of(entry));
        return this;
    }

    public TabComplete add(int atIndex, List<String> entry) {
        if (entry == null || entry.isEmpty()) {
            return this;
        }
        atIndex = Math.max(1, atIndex);
        entries.computeIfAbsent(atIndex, k -> new ArrayList<>(entry.size())).addAll(entry);
        return this;
    }

    public TabComplete add(int atIndex, String entry) {
        if (entry == null || entry.isEmpty()) {
            return this;
        }
        atIndex = Math.max(1, atIndex);
        entries.computeIfAbsent(atIndex, k -> new ArrayList<>()).add(entry);
        return this;
    }

    public List<String> build() {
        if (args.length == 0) {
            return Collections.emptyList();
        }

        List<String> suggestions = entries.get(args.length);
        if (suggestions == null || suggestions.isEmpty()) {
            return Collections.emptyList();
        }

        if (currentArgLength == 0) {
            return new ArrayList<>(suggestions);
        }

        List<String> filtered = new ArrayList<>(suggestions.size());
        for (String suggestion : suggestions) {
            if (suggestion.toLowerCase().startsWith(currentArg)) {
                filtered.add(suggestion);
            }
        }

        return filtered;
    }

    public List<String> buildSorted() {
        List<String> results = build();
        if (results.size() > 1) {
            Collections.sort(results);
        }
        return results;
    }

    public void clear() {
        entries.clear();
    }

    public void clear(int index) {
        entries.remove(Math.max(1, index));
    }
}