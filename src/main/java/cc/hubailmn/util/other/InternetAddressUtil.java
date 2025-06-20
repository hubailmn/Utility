package cc.hubailmn.util.other;

import cc.hubailmn.util.interaction.CSend;
import lombok.Getter;

import java.net.URL;
import java.util.Scanner;

public class InternetAddressUtil {

    @Getter
    private static final String address = resolvePublicAddress();

    private static String resolvePublicAddress() {
        try (Scanner scanner = new Scanner(new URL("https://api.ipify.org").openStream())) {
            return scanner.nextLine();
        } catch (Exception e) {
            CSend.error("§cFailed to resolve public IP address.");
            return "error";
        }
    }
}
