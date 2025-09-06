package cc.hubailmn.utility.command;

import cc.hubailmn.utility.interaction.player.PlayerUtil;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Getter
public class TabComplete {

    public static final List<String> ALL_MATERIALS = Collections.unmodifiableList(
            toReadableNames(Arrays.stream(Material.values())
                    .map(Material::name)
                    .collect(Collectors.toList()))
    );

    public static final List<String> ITEM_MATERIALS = Collections.unmodifiableList(
            toReadableNames(Arrays.stream(Material.values())
                    .filter(Material::isItem)
                    .map(Material::name)
                    .collect(Collectors.toList()))
    );

    public static final List<String> FOOD_MATERIALS = Collections.unmodifiableList(
            toReadableNames(Arrays.stream(Material.values())
                    .filter(Material::isEdible)
                    .map(Material::name)
                    .collect(Collectors.toList()))
    );

    public static final List<String> TOOL_MATERIALS = Collections.unmodifiableList(
            toReadableNames(Arrays.stream(Material.values())
                    .map(Material::name)
                    .filter(name -> name.endsWith("_AXE") || name.endsWith("_PICKAXE") ||
                            name.endsWith("_SHOVEL") || name.endsWith("_HOE") ||
                            name.equals("MACE") || name.endsWith("_SWORD") ||
                            name.contains("SHEARS") || name.contains("FISHING_ROD"))
                    .collect(Collectors.toList()))
    );

    public static final List<String> BLOCK_MATERIALS = Collections.unmodifiableList(
            toReadableNames(Arrays.stream(Material.values())
                    .filter(Material::isBlock)
                    .map(Material::name)
                    .collect(Collectors.toList()))
    );

    private final CommandSender sender;
    private final Command command;
    private final String alias;
    private final String[] args;
    private final Map<Integer, Set<String>> entries = new HashMap<>();
    private final String currentArg;
    private final int currentArgLength;

    public TabComplete(CommandSender sender, Command command, String alias, String[] args) {
        this.sender = sender;
        this.command = command;
        this.alias = alias;
        this.args = args;

        if (args.length > 0) {
            this.currentArg = args[args.length - 1].toLowerCase();
            this.currentArgLength = currentArg.length();
        } else {
            this.currentArg = "";
            this.currentArgLength = 0;
        }
    }

    public static List<String> toReadableNames(List<String> materialNames) {
        return materialNames.stream()
                .map(name -> Arrays.stream(name.split("_"))
                        .map(part -> part.substring(0, 1).toUpperCase() + part.substring(1).toLowerCase())
                        .collect(Collectors.joining("_")))
                .collect(Collectors.toList());
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
        entries.computeIfAbsent(atIndex, k -> new LinkedHashSet<>()).addAll(Arrays.asList(entry));
        return this;
    }

    public TabComplete add(int atIndex, Collection<String> entry) {
        if (entry == null || entry.isEmpty()) {
            return this;
        }
        atIndex = Math.max(1, atIndex);
        entries.computeIfAbsent(atIndex, k -> new LinkedHashSet<>()).addAll(entry);
        return this;
    }

    public TabComplete add(int atIndex, String entry) {
        if (entry == null || entry.isEmpty()) {
            return this;
        }
        atIndex = Math.max(1, atIndex);
        entries.computeIfAbsent(atIndex, k -> new LinkedHashSet<>()).add(entry);
        return this;
    }

    public TabComplete addIfPerm(int index, String perm, String... values) {
        if (PlayerUtil.hasPermission(sender, perm)) {
            add(index, values);
        }
        return this;
    }

    public TabComplete addIfPerm(int index, String perm, Collection<String> values) {
        if (PlayerUtil.hasPermission(sender, perm)) {
            add(index, values);
        }
        return this;
    }

    public TabComplete addPlayerNames(int index) {
        return add(index, sender.getServer().getOnlinePlayers().stream()
                .map(Player::getName)
                .collect(Collectors.toList()));
    }

    public TabComplete addPlayerIfOnline(int index, String playerName) {
        if (sender.getServer().getPlayer(playerName) != null) {
            add(index, playerName);
        }
        return this;
    }

    public TabComplete addPlayersWithPerm(int index, String perm) {
        if (PlayerUtil.hasPermission(sender, perm)) {
            List<String> names = sender.getServer().getOnlinePlayers()
                    .stream()
                    .map(Player::getName)
                    .collect(Collectors.toList());
            add(index, names);
        }
        return this;
    }

    public TabComplete addWildcardAndPlayers(int index, String wildcardPermission) {
        if (PlayerUtil.hasPermission(sender, wildcardPermission)) {
            add(index, "*");
        }
        return addPlayerNames(index);
    }

    public TabComplete addWildcardAndPlayersIfPerm(int index, String perm) {
        if (PlayerUtil.hasPermission(sender, perm)) {
            add(index, "*");
            addPlayerNames(index);
        }
        return this;
    }

    public <E extends Enum<E>> TabComplete addEnum(int index, Class<E> enumClass) {
        Objects.requireNonNull(enumClass, "Enum class cannot be null");

        Set<String> enumNames = Arrays.stream(enumClass.getEnumConstants())
                .map(e -> e.name().toLowerCase())
                .collect(Collectors.toCollection(LinkedHashSet::new));

        return add(index, enumNames);
    }

    public <E extends Enum<E>> TabComplete addEnum(int index, Class<E> enumClass, Collection<Predicate<E>> excludePredicates) {
        Objects.requireNonNull(enumClass, "Enum class cannot be null");
        Objects.requireNonNull(excludePredicates, "Exclude predicates cannot be null");

        Set<String> enumNames = Arrays.stream(enumClass.getEnumConstants())
                .filter(e -> excludePredicates.stream().noneMatch(pred -> pred.test(e)))
                .map(e -> e.name().toLowerCase())
                .collect(Collectors.toCollection(LinkedHashSet::new));

        return add(index, enumNames);
    }

    @SafeVarargs
    public final <E extends Enum<E>> TabComplete addEnumExclude(int index, Class<E> enumClass, E... excludeValues) {
        Objects.requireNonNull(enumClass, "Enum class cannot be null");

        if (excludeValues == null || excludeValues.length == 0) {
            return addEnum(index, enumClass);
        }

        Set<E> excludeSet = Set.of(excludeValues);
        Set<String> enumNames = Arrays.stream(enumClass.getEnumConstants())
                .filter(e -> !excludeSet.contains(e))
                .map(e -> e.name().toLowerCase())
                .collect(Collectors.toCollection(LinkedHashSet::new));

        return add(index, enumNames);
    }

    public <E extends Enum<E>> TabComplete addEnum(int index, Class<E> enumClass, Function<E, String> nameMapper) {
        Objects.requireNonNull(enumClass, "Enum class cannot be null");
        Objects.requireNonNull(nameMapper, "Name mapper cannot be null");

        Set<String> enumNames = Arrays.stream(enumClass.getEnumConstants())
                .map(nameMapper)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        return add(index, enumNames);
    }

    public <E extends Enum<E>> TabComplete addEnum(int index, Class<E> enumClass, Predicate<E> filter, java.util.function.Function<E, String> nameMapper) {
        Objects.requireNonNull(enumClass, "Enum class cannot be null");
        Objects.requireNonNull(filter, "Filter cannot be null");
        Objects.requireNonNull(nameMapper, "Name mapper cannot be null");

        Set<String> enumNames = Arrays.stream(enumClass.getEnumConstants())
                .filter(filter)
                .map(nameMapper)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        return add(index, enumNames);
    }

    public TabComplete addIf(int index, Predicate<CommandSender> condition, String... values) {
        if (condition.test(sender)) {
            add(index, values);
        }
        return this;
    }

    public TabComplete addMatching(int index, Collection<String> values, String filter) {
        String filterLower = filter.toLowerCase();
        Set<String> matching = values.stream()
                .filter(value -> value.toLowerCase().startsWith(filterLower))
                .collect(Collectors.toCollection(LinkedHashSet::new));
        return add(index, matching);
    }

    public TabComplete addBoolean(int index) {
        return add(index, "true", "false");
    }

    public TabComplete addBlocks(int index) {
        return add(index, BLOCK_MATERIALS);
    }

    public TabComplete addItems(int index) {
        return add(index, ITEM_MATERIALS);
    }

    public TabComplete addFood(int index) {
        return add(index, FOOD_MATERIALS);
    }

    public TabComplete addTools(int index) {
        return add(index, TOOL_MATERIALS);
    }

    public TabComplete addMaterials(int index) {
        return add(index, ALL_MATERIALS);
    }

    public List<String> build() {
        if (args.length == 0) {
            return Collections.emptyList();
        }

        Set<String> suggestions = entries.get(args.length);
        if (suggestions == null || suggestions.isEmpty()) {
            return Collections.emptyList();
        }

        if (currentArgLength == 0) {
            return new ArrayList<>(suggestions);
        }

        List<String> filtered = new ArrayList<>();
        StringUtil.copyPartialMatches(currentArg, suggestions, filtered);

        return filtered;
    }

    public List<String> buildSorted() {
        List<String> results = build();
        if (results.size() > 1) {
            results.sort(String.CASE_INSENSITIVE_ORDER);
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