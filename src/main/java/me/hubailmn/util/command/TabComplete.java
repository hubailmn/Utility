package me.hubailmn.util.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.*;
import java.util.function.Predicate;

public class TabComplete {
    private final CommandSender sender;
    private final Command command;
    private final String alias;
    private final String[] args;
    private final Map<Integer, List<String>> entries = new HashMap();

    public TabComplete(CommandSender sender, Command command, String alias, String[] args) {
        this.sender = sender;
        this.command = command;
        this.alias = alias;
        this.args = args;
    }

    public TabComplete add(int atIndex, String[] entry, Predicate<String[]> condition) {
        if (condition.test(this.args)) {
            this.add(atIndex, entry);
        }

        return this;
    }

    public TabComplete add(int atIndex, String[] entry) {
        atIndex = Math.max(1, atIndex);
        this.entries.put(atIndex, Arrays.stream(entry).toList());
        return this;
    }

    public List<String> build() {
        List<String> list = new ArrayList<>(this.entries.get(this.args.length) != null ? this.entries.get(this.args.length) : new ArrayList());
        list.removeIf((s) -> {
            return !s.toLowerCase().contains(this.args[this.args.length - 1].toLowerCase());
        });
        return list;
    }
}
