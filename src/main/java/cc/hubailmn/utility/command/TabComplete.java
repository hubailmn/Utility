package cc.hubailmn.utility.command;

import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Getter
public class TabComplete {
    public static final List<String> ALL_MATERIAL_NAMES = Arrays.stream(Material.values())
            .map(Material::name)
            .toList();
    public static final List<String> ITEM_MATERIAL_NAMES = Arrays.stream(Material.values())
            .filter(Material::isItem)
            .map(Material::name)
            .toList();
    public static final List<String> FOOD_MATERIALS = Arrays.stream(Material.values())
            .filter(Material::isEdible)
            .map(Material::name)
            .toList();
    public static final List<String> TOOL_MATERIALS = Arrays.stream(Material.values())
            .filter(m -> m.name().endsWith("_AXE")
                    || m.name().endsWith("_PICKAXE")
                    || m.name().endsWith("_SHOVEL")
                    || m.name().endsWith("_HOE")
                    || m.name().equals("MACE")
                    || m.name().endsWith("_SWORD")
                    || m.name().contains("SHEARS")
                    || m.name().contains("FISHING_ROD")
            )
            .map(Material::name)
            .toList();
    public static final List<String> BLOCK_MATERIALS = Arrays.stream(Material.values())
            .filter(Material::isBlock)
            .map(Material::name)
            .toList();
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

    public static List<String> toReadableNames(List<String> materialNames) {
        return materialNames.stream()
                .map(name -> Arrays.stream(name.split("_"))
                        .map(part -> part.substring(0, 1).toUpperCase() + part.substring(1).toLowerCase())
                        .collect(Collectors.joining("_")))
                .toList();
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

    public TabComplete addIfPerm(int index, String perm, String... values) {
        if (sender.hasPermission(perm)) {
            add(index, values);
        }
        return this;
    }

    public TabComplete addIfPerm(int index, String perm, List<String> values) {
        if (sender.hasPermission(perm)) {
            add(index, values);
        }
        return this;
    }

    public TabComplete addPlayerNames(int index) {
        return add(index, sender.getServer().getOnlinePlayers().stream().map(Player::getName).toList());
    }

    public TabComplete addPlayerIfOnline(int index, String playerName) {
        if (sender.getServer().getPlayer(playerName) != null) {
            add(index, playerName);
        }
        return this;
    }

    public TabComplete addPlayersWithPerm(int index, String perm) {
        if (sender.hasPermission(perm)) {
            List<String> names = sender.getServer().getOnlinePlayers()
                    .stream()
                    .map(Player::getName)
                    .toList();
            add(index, names);
        }
        return this;
    }

    public TabComplete addWildcardAndPlayers(int index, String wildcardPermission) {
        if (sender.hasPermission(wildcardPermission)) {
            add(index, "*");
        }
        return addPlayerNames(index);
    }

    public TabComplete addWildcardAndPlayersIfPerm(int index, String perm) {
        if (sender.hasPermission(perm)) {
            add(index, "*");
            add(index, sender.getServer().getOnlinePlayers().stream().map(Player::getName).toList());
        }
        return this;
    }

    public <E extends Enum<E>> TabComplete addEnum(int index, Class<E> enumClass) {
        for (E e : enumClass.getEnumConstants()) {
            add(index, e.name().toLowerCase());
        }
        return this;
    }

    public TabComplete addIf(int index, Predicate<CommandSender> condition, String... values) {
        if (condition.test(sender)) {
            add(index, values);
        }
        return this;
    }

    public TabComplete addMatching(int index, List<String> values, String filter) {
        for (String value : values) {
            if (value.toLowerCase().startsWith(filter.toLowerCase())) {
                add(index, value);
            }
        }
        return this;
    }

    public TabComplete addBoolean(int index) {
        return add(index, "true", "false");
    }

    public TabComplete addBlocks(int index) {
        return add(index, toReadableNames(BLOCK_MATERIALS));
    }

    public TabComplete addItems(int index) {
        return add(index, toReadableNames(ITEM_MATERIAL_NAMES));
    }

    public TabComplete addFood(int index) {
        return add(index, toReadableNames(FOOD_MATERIALS));
    }

    public TabComplete addTools(int index) {
        return add(index, toReadableNames(TOOL_MATERIALS));
    }

    public TabComplete addMaterials(int index) {
        return add(index, toReadableNames(ALL_MATERIAL_NAMES));
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

    public TabComplete debug() {
        System.out.println("TabComplete[" + alias + "] args=" + String.join(" ", args));
        entries.forEach((k, v) -> System.out.println("Index " + k + ": " + v));
        return this;
    }

    public void clear() {
        entries.clear();
    }

    public void clear(int index) {
        entries.remove(Math.max(1, index));
    }
}