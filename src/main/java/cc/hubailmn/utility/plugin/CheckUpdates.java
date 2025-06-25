package cc.hubailmn.utility.plugin;

import cc.hubailmn.utility.BasePlugin;
import cc.hubailmn.utility.interaction.CSend;
import lombok.Getter;
import lombok.Setter;
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

    @Getter
    @Setter
    private static String latestVersion = null;

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
                            CSend.warn("Latest version tag not found in response.");
                            return;
                        }

                        if (!latestVersion.equalsIgnoreCase(pluginVersion)) {
                            CSend.info(BasePlugin.getPrefix() + "§eA new version is available: §6" + latestVersion);
                            CSend.info("§aDownload it here: §9" + releasesPage);
                            setNeedUpdate(true);
                            setLatestVersion(latestVersion);
                        }
                    }
                } else {
                    CSend.error("Request failed. HTTP " + responseCode + " - " + connection.getResponseMessage());
                }

            } catch (Exception e) {
                CSend.error("Error occurred while checking for plugin updates:");
                CSend.error(e);
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        });
    }
}
