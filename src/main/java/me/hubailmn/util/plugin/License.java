package me.hubailmn.util.plugin;

import lombok.Getter;
import lombok.Setter;
import me.hubailmn.util.BasePlugin;
import me.hubailmn.util.config.ConfigUtil;
import me.hubailmn.util.config.file.LicenseConfig;
import me.hubailmn.util.interaction.CSend;
import org.bukkit.Bukkit;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class License {

    @Getter
    @Setter
    private static String LICENSE_URL = "https://<IP>:<PORT>/";

    @Getter
    private static final String licenseKey = ConfigUtil.getConfig(LicenseConfig.class).getKey();

    @Getter
    private static final String ipAddress = resolvePublicIP();

    private static String resolvePublicIP() {
        try (Scanner scanner = new Scanner(new URL("https://api.ipify.org").openStream())) {
            return scanner.nextLine();
        } catch (Exception e) {
            CSend.error("§cFailed to resolve public IP address.");
            return "error";
        }
    }

    public static void sendFirstRequest() {
        Bukkit.getScheduler().runTaskAsynchronously(BasePlugin.getInstance(), () -> {
            if (isLicenseInvalid()) {
                CSend.info("§cLicense validation failed. Plugin will be disabled.");
                Bukkit.getPluginManager().disablePlugin(BasePlugin.getInstance());
            }
        });
    }

    public static void checkLicense() {
        if (isLicenseInvalid()) {
            CSend.error("§cLicense check failed. Plugin will be disabled.");
            Bukkit.getPluginManager().disablePlugin(BasePlugin.getInstance());
        }
    }

    public static void endLicenseSession() {
        try {
            String url = String.format("%send-session?key=%s&ip=%s:%d&plugin=%s", LICENSE_URL, encode(licenseKey), encode(ipAddress), Bukkit.getServer().getPort(), encode(BasePlugin.getPluginName()));

            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
            connection.getInputStream().close();
        } catch (Exception e) {
            CSend.warn("§7Failed to end license session.");
            CSend.error(e);
        }
    }

    private static boolean isLicenseInvalid() {
        try {
            String url = String.format("%svalidate-license?key=%s&ip=%s:%d&plugin=%s", LICENSE_URL, encode(licenseKey), encode(ipAddress), Bukkit.getServer().getPort(), encode(BasePlugin.getPluginName()));

            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");
            connection.connect();

            try (Scanner scanner = new Scanner(new InputStreamReader(connection.getInputStream()))) {
                String response = scanner.hasNextLine() ? scanner.nextLine() : "";
                boolean valid = "Valid License!".equals(response);
                if (!valid) CSend.info("§c" + response);
                return !valid;
            }
        } catch (Exception e) {
            CSend.error("§cError occurred during license validation: " + e.getMessage());
            CSend.error(e);
            return true;
        }
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
