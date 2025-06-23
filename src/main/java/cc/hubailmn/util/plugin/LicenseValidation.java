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
import java.util.function.Consumer;

public class LicenseValidation {

    @Getter
    private static final String licenseKey = ConfigUtil.getConfig(LicenseConfig.class).getKey();

    @Getter
    @Setter
    private static String LICENSE_URL = "http://ivoryhost.hubailmn.cc:10015/api/v1/license/";

    public static void sendFirstRequest() {
        validateLicenseAsync(valid -> {
            if (!valid) {
                CSend.info("§cValidation failed. Plugin will be disabled.");
                Bukkit.getScheduler().runTask(BasePlugin.getInstance(), () ->
                        Bukkit.getPluginManager().disablePlugin(BasePlugin.getInstance()));
            }
        });
    }

    public static void endLicenseSession() {
        Bukkit.getScheduler().runTaskAsynchronously(BasePlugin.getInstance(), () -> {
            try {
                String url = String.format("%send-session?key=%s&ip=%s:%d&plugin=%s",
                        LICENSE_URL, encode(licenseKey), encode(AddressUtil.getAddress()),
                        Bukkit.getServer().getPort(), encode(BasePlugin.getPluginName()));

                HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);
                connection.connect();
                connection.getInputStream().close();
            } catch (Exception e) {
                CSend.warn("§7Failed to end session.");
                CSend.error(e);
            }
        });
    }

    public static void validateLicenseAsync(Consumer<Boolean> callback) {
        Bukkit.getScheduler().runTaskAsynchronously(BasePlugin.getInstance(), () -> {
            try {
                String url = String.format("%svalidate?key=%s&ip=%s:%d&plugin=%s",
                        LICENSE_URL, encode(licenseKey), encode(AddressUtil.getAddress()),
                        Bukkit.getServer().getPort(), encode(BasePlugin.getPluginName()));

                HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);
                connection.connect();

                try (Scanner scanner = new Scanner(new InputStreamReader(connection.getInputStream()))) {
                    String response = scanner.hasNextLine() ? scanner.nextLine() : "";
                    boolean valid = "Valid.".equals(response);
                    if (!valid) CSend.info("§c" + response);
                    callback.accept(valid);
                }
            } catch (Exception e) {
                CSend.error("§cError during license validation: " + e.getMessage());
                CSend.error(e);
                callback.accept(false);
            }
        });
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
