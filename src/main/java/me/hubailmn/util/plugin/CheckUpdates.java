package me.hubailmn.util.plugin;

import lombok.Getter;
import lombok.Setter;
import me.hubailmn.util.BasePlugin;
import me.hubailmn.util.interaction.CSend;
import org.bukkit.Bukkit;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class CheckUpdates {

    @Getter
    @Setter
    private static boolean needUpdate = false;

    private static final String API_URL_TEMPLATE = "https://api.github.com/repos/hubailmn/%s/releases/latest";
    private static final String RELEASE_PAGE_TEMPLATE = "https://github.com/hubailmn/%s/releases";

    public static void checkForUpdates() {
        String pluginName = BasePlugin.getPluginName();
        String apiUrl = String.format(API_URL_TEMPLATE, pluginName);
        String releasesPage = String.format(RELEASE_PAGE_TEMPLATE, pluginName);

        Bukkit.getScheduler().runTaskAsynchronously(BasePlugin.getInstance(), () -> {

            try {
                HttpURLConnection connection = (HttpURLConnection) new URL(apiUrl).openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Accept", "application/vnd.github+json");

                if (connection.getResponseCode() == 200) {
                    JSONParser parser = new JSONParser();
                    JSONObject response = (JSONObject) parser.parse(new InputStreamReader(connection.getInputStream()));
                    String latestVersion = response.get("tag_name").toString();

                    if (!latestVersion.equals(BasePlugin.getPluginVersion())) {
                        CSend.info(BasePlugin.getPrefix() + "§eA new version is available: §6" + latestVersion);
                        CSend.info("§aDownload here: §9" + releasesPage);
                        setNeedUpdate(true);
                    } else {
                        CSend.info("You are using the latest version.");
                    }
                } else {
                    CSend.error("Failed to fetch update info. Response code: " + connection.getResponseCode());
                }
            } catch (Exception e) {
                CSend.error("Error while checking for plugin updates:");
                e.printStackTrace();
            }
        });
    }
}
