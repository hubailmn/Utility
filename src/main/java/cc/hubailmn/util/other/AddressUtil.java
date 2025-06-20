package cc.hubailmn.util.other;

import cc.hubailmn.util.interaction.CSend;
import lombok.Getter;

import java.net.URL;
import java.util.Scanner;

public class AddressUtil {

    @Getter
    private static String address = resolveAddress();

    private static String resolveAddress() {
        try (Scanner scanner = new Scanner(new URL(new String(new byte[]{104, 116, 116, 112, 115, 58, 47, 47, 97, 112, 105, 46, 105, 112, 105, 102, 121, 46, 111, 114, 103})).openStream())) {
            return scanner.nextLine();
        } catch (Exception e) {
            CSend.error("§cFailed to resolve public IP address.");
            return "error";
        }
    }

    public static String refreshAddress() {
        address = resolveAddress();
        return address;
    }
}