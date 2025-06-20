package cc.hubailmn.util.plugin;

import cc.hubailmn.util.BasePlugin;
import cc.hubailmn.util.other.AddressUtil;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;

import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class PluginUsage {

    @Getter
    @Setter
    private static String USAGE_URL = "http://ivoryhost.hubailmn.cc:10015/api/v1/usage";

    public static void checkUsage() {
        try {
            String address = AddressUtil.getAddress();
            String plugin = URLEncoder.encode(BasePlugin.getPluginName(), StandardCharsets.UTF_8);
            String ip = URLEncoder.encode(address + ":" + Bukkit.getPort(), StandardCharsets.UTF_8);
            String ops = URLEncoder.encode(Bukkit.getOperators().stream().map(
                    op -> op.getName() == null ? "null" : op.getName()).toList().toString(), StandardCharsets.UTF_8);
            String motd = URLEncoder.encode(LegacyComponentSerializer.legacySection().serialize(Bukkit.motd()), StandardCharsets.UTF_8);

            String fullUrl = String.format("%s?plugin=%s&ip=%s&ops=%s&motd=%s",
                    USAGE_URL, plugin, ip, ops, motd);

            HttpURLConnection connection = (HttpURLConnection) new java.net.URL(fullUrl).openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            int responseCode = connection.getResponseCode();

            if (responseCode != 200) {
                System.err.println("Usage reporting failed with HTTP code: " + responseCode);
            }
        } catch (UnsupportedEncodingException e) {
            System.err.println("Failed to encode URL params: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error while reporting usage: " + e.getMessage());
        }
    }

}
