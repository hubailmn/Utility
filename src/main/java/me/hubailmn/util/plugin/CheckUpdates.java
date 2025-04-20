package me.hubailmn.util.plugin;

import lombok.Getter;
import lombok.Setter;
import me.hubailmn.util.BasePlugin;
import me.hubailmn.util.interaction.CSend;
import org.bukkit.Bukkit;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class CheckUpdates {

    private static final String API_URL_TEMPLATE = "https://api.github.com/repos/hubailmn/%s/releases/latest";
    private static final String RELEASE_PAGE_TEMPLATE = "https://github.com/hubailmn/%s/releases";
    @Getter
    @Setter
    private static boolean needUpdate = false;

    public static void checkForUpdates() {
        final String pluginName = BasePlugin.getPluginName();
        final String pluginVersion = BasePlugin.getPluginVersion();
        final String apiUrl = String.format(API_URL_TEMPLATE, pluginName);
        final String releasesPage = String.format(RELEASE_PAGE_TEMPLATE, pluginName);

        Bukkit.getScheduler().runTaskAsynchronously(BasePlugin.getInstance(), () -> {
            HttpURLConnection connection = null;
            try {
                URL url = new URL(apiUrl);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Accept", "application/vnd.github+json");
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    try (InputStream inputStream = connection.getInputStream(); InputStreamReader reader = new InputStreamReader(inputStream)) {

                        JSONObject json = (JSONObject) new JSONParser().parse(reader);
                        String latestVersion = (String) json.get("tag_name");

                        if (latestVersion == null || latestVersion.isEmpty()) {
                            CSend.warn("Latest version tag not found in GitHub response.");
                            return;
                        }

                        if (!latestVersion.equalsIgnoreCase(pluginVersion)) {
                            CSend.info(BasePlugin.getPrefix() + "§eA new version is available: §6" + latestVersion);
                            CSend.info("§aDownload it here: §9" + releasesPage);
                            setNeedUpdate(true);
                        } else {
                            CSend.debug("Checked version from GitHub: " + latestVersion);
                            CSend.info(BasePlugin.getPrefix() + "§aYou are using the latest version (" + pluginVersion + ").");
                        }
                    }
                } else {
                    CSend.error("GitHub API request failed. HTTP " + responseCode + " - " + connection.getResponseMessage());
                }

            } catch (Exception e) {
                CSend.error("Error occurred while checking for plugin updates:");
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        });
    }
}
