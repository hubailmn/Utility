package me.hubailmn.util.item;

import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

public class Base64Util {

    private Base64Util() {
        throw new UnsupportedOperationException("This is a utility class.");
    }


    public static String itemStackArrayToBase64(ItemStack[] items) {
        if (items == null) throw new IllegalArgumentException("ItemStack array cannot be null.");
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream)) {

            dataOutput.writeInt(items.length);
            for (ItemStack item : items) {
                dataOutput.writeObject(item);
            }

            return Base64.getEncoder().encodeToString(outputStream.toByteArray());
        } catch (IOException e) {
            throw new IllegalStateException("Unable to serialize item stacks.", e);
        }
    }

    public static ItemStack[] itemStackArrayFromBase64(String base64) {
        if (base64 == null || base64.isEmpty()) return new ItemStack[0];
        byte[] data = Base64.getDecoder().decode(base64);

        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
             BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream)) {

            int length = dataInput.readInt();
            ItemStack[] items = new ItemStack[length];

            for (int i = 0; i < length; i++) {
                items[i] = (ItemStack) dataInput.readObject();
            }

            return items;

        } catch (IOException | ClassNotFoundException e) {
            throw new IllegalStateException("Unable to deserialize item stacks.", e);
        }
    }

    public static String itemStackToBase64(ItemStack item) {
        if (item == null) throw new IllegalArgumentException("ItemStack cannot be null.");
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream)) {

            dataOutput.writeObject(item);
            return Base64.getEncoder().encodeToString(outputStream.toByteArray());

        } catch (IOException e) {
            throw new IllegalStateException("Unable to serialize ItemStack.", e);
        }
    }

    public static ItemStack itemStackFromBase64(String base64) {
        if (base64 == null || base64.isEmpty()) return null;
        byte[] data = Base64.getDecoder().decode(base64);

        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
             BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream)) {

            return (ItemStack) dataInput.readObject();

        } catch (IOException | ClassNotFoundException e) {
            throw new IllegalStateException("Unable to deserialize ItemStack.", e);
        }
    }
}
