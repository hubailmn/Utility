package cc.hubailmn.utility.external;

import cc.hubailmn.utility.plugin.CSend;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public class VaultEconomyUtil {

    private static Economy economy;

    private static String cachedCurrencyName = "money";
    private static String cachedCurrencySymbol = "money";

    public static boolean setupEconomy() {
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
            CSend.warn("Vault is not installed.");
            return false;
        }

        RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
        if (rsp == null) return false;

        economy = rsp.getProvider();

        refreshCache();
        CSend.log("Economy provider found: " + economy.getName());

        return true;
    }

    public static void refreshCache() {
        if (economy == null) return;

        try {
            cachedCurrencyName = economy.currencyNamePlural();
            cachedCurrencySymbol = economy.getName();
        } catch (Exception e) {
            CSend.warn("Failed to refresh currency cache: " + e.getMessage());
        }
    }

    public static boolean isEconomyAvailable() {
        return economy != null;
    }

    public static double getBalance(Player player) {
        return getBalance((OfflinePlayer) player);
    }

    public static double getBalance(OfflinePlayer player) {
        return isEconomyAvailable() ? economy.getBalance(player) : 0.0;
    }

    public static boolean has(Player player, double amount) {
        return has((OfflinePlayer) player, amount);
    }

    public static boolean has(OfflinePlayer player, double amount) {
        return isEconomyAvailable() && economy.has(player, amount);
    }

    public static boolean withdraw(Player player, double amount) {
        return withdraw((OfflinePlayer) player, amount);
    }

    public static boolean withdraw(OfflinePlayer player, double amount) {
        if (!isEconomyAvailable()) return false;
        return economy.withdrawPlayer(player, amount).transactionSuccess();
    }

    public static boolean deposit(Player player, double amount) {
        return deposit((OfflinePlayer) player, amount);
    }

    public static boolean deposit(OfflinePlayer player, double amount) {
        if (!isEconomyAvailable()) return false;
        return economy.depositPlayer(player, amount).transactionSuccess();
    }

    public static String format(double amount) {
        return isEconomyAvailable() ? economy.format(amount) : String.format("%.2f", amount);
    }

    public static String getCurrencyName() {
        return cachedCurrencyName;
    }

    public static String getCurrencySymbol() {
        return cachedCurrencySymbol;
    }
}
