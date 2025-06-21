package cc.hubailmn.util.other;

import cc.hubailmn.util.BasePlugin;
import cc.hubailmn.util.interaction.CSend;
import lombok.Getter;
import org.bukkit.Bukkit;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class AddressUtil {

    @Getter
    private static volatile String address = "unknown";

    public static void initAsyncFetch(Runnable onSuccess) {
        if (address != null && !address.equals("unknown")) return;

        Bukkit.getScheduler().runTaskAsynchronously(BasePlugin.getInstance(), () -> {
            String result = resolveAddress();
            if (!"error".equals(result)) {
                address = result;
                if (onSuccess != null) onSuccess.run();
            }
        });
    }

    private static String resolveAddress() {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(new String(new byte[]{104, 116, 116, 112, 115, 58, 47, 47, 97, 112, 105, 46, 105, 112, 105, 102, 121, 46, 111, 114, 103}));
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(3000);
            connection.setReadTimeout(3000);
            connection.setRequestMethod("GET");

            try (Scanner scanner = new Scanner(connection.getInputStream())) {
                return scanner.nextLine();
            }
        } catch (IOException e) {
            CSend.error("§cFailed to resolve public IP address: " + e.getMessage());
            return "error";
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    public static void refreshAsync(Runnable onSuccess) {
        address = "unknown";
        initAsyncFetch(onSuccess);
    }

}