package cc.hubailmn.util.plugin;

import cc.hubailmn.util.BasePlugin;
import cc.hubailmn.util.config.ConfigUtil;
import cc.hubailmn.util.config.file.LicenseConfig;
import cc.hubailmn.util.interaction.CSend;
import cc.hubailmn.util.other.AddressUtil;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class LicenseValidation {

    @Getter
    private static final String licenseKey = ConfigUtil.getConfig(LicenseConfig.class).getKey();

    @Getter
    @Setter
    private static String LICENSE_URL = new String(new byte[]{104, 116, 116, 112, 58, 47, 47, 105, 118, 111, 114, 121, 104, 111, 115, 116, 46, 104, 117, 98, 97, 105, 108, 109, 110, 46, 99, 99, 58, 49, 48, 48, 49, 53, 47, 97, 112, 105, 47, 118, 49, 47, 108, 105, 99, 101, 110, 115, 101, 47});

    public static void sendFirstRequest() {
        Bukkit.getScheduler().runTaskAsynchronously(BasePlugin.getInstance(), () -> {
            if (isLicenseInvalid()) {
                CSend.info("§cValidation failed. Plugin will be disabled.");
                Bukkit.getPluginManager().disablePlugin(BasePlugin.getInstance());
            }
        });
    }

    public static void checkLicense() {
        if (isLicenseInvalid()) {
            CSend.info("§cValidation failed. Plugin will be disabled.");
            Bukkit.getPluginManager().disablePlugin(BasePlugin.getInstance());
        }
    }

    public static void endLicenseSession() {
        try {
            String url = String.format("%send-session?key=%s&ip=%s:%d&plugin=%s", LICENSE_URL, encode(licenseKey), encode(AddressUtil.getAddress()), Bukkit.getServer().getPort(), encode(BasePlugin.getPluginName()));

            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
            connection.getInputStream().close();
        } catch (Exception e) {
            CSend.warn("§7Failed to end session.");
            CSend.error(e);
        }
    }

    private static boolean isLicenseInvalid() {
        try {
            String url = String.format("%svalidate?key=%s&ip=%s:%d&plugin=%s", LICENSE_URL, encode(licenseKey), encode(AddressUtil.getAddress()), Bukkit.getServer().getPort(), encode(BasePlugin.getPluginName()));

            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");
            connection.connect();

            try (Scanner scanner = new Scanner(new InputStreamReader(connection.getInputStream()))) {
                String response = scanner.hasNextLine() ? scanner.nextLine() : "";
                boolean valid = "Valid.".equals(response);
                if (!valid) CSend.info("§c" + response);
                return !valid;
            }
        } catch (Exception e) {
            CSend.error("§cError occurred during validation: " + e.getMessage());
            CSend.error(e);
            return true;
        }
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
