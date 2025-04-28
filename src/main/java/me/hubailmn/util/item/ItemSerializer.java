package me.hubailmn.util.item;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class ItemSerializer {

    private static final Gson gson = new Gson();

    private ItemSerializer() {
        throw new UnsupportedOperationException("This is a utility class.");
    }

    public static String toBase64Array(ItemStack[] items) {
        if (items == null) return null;
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

    public static ItemStack[] fromBase64Array(String base64) {
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

    public static String toBase64(ItemStack item) {
        if (item == null) return null;
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream)) {

            dataOutput.writeObject(item);
            return Base64.getEncoder().encodeToString(outputStream.toByteArray());

        } catch (IOException e) {
            throw new IllegalStateException("Unable to serialize ItemStack.", e);
        }
    }

    public static ItemStack fromBase64(String base64) {
        if (base64 == null || base64.isEmpty()) return null;
        byte[] data = Base64.getDecoder().decode(base64);

        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
             BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream)) {

            return (ItemStack) dataInput.readObject();

        } catch (IOException | ClassNotFoundException e) {
            throw new IllegalStateException("Unable to deserialize ItemStack.", e);
        }
    }

    public static String mapToJson(Map<Integer, ItemStack> quickBuy) {
        Map<Integer, String> serialized = new HashMap<>();
        for (Map.Entry<Integer, ItemStack> entry : quickBuy.entrySet()) {
            serialized.put(entry.getKey(), ItemSerializer.toBase64(entry.getValue()));
        }
        return gson.toJson(serialized);
    }

    public static Map<Integer, ItemStack> mapFromJson(String json) {
        if (json == null || json.isEmpty()) {
            return new HashMap<>();
        }
        Type type = new TypeToken<Map<Integer, String>>() {
        }.getType();
        Map<Integer, String> serialized = gson.fromJson(json, type);
        Map<Integer, ItemStack> quickBuy = new HashMap<>();
        for (Map.Entry<Integer, String> entry : serialized.entrySet()) {
            quickBuy.put(entry.getKey(), ItemSerializer.fromBase64(entry.getValue()));
        }
        return quickBuy;
    }
}
