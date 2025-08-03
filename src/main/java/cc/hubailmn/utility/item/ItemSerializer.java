package cc.hubailmn.utility.item;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class ItemSerializer {

    private static final Gson gson = new Gson();

    private ItemSerializer() {
        throw new UnsupportedOperationException("This is a utility class.");
    }

    public static String toBase64Array(ItemStack[] items) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);

            dataOutput.writeInt(items.length);

            for (ItemStack item : items) {
                dataOutput.writeObject(item);
            }

            dataOutput.close();
            return Base64Coder.encodeLines(outputStream.toByteArray());
        } catch (Exception e) {
            throw new IllegalStateException("Unable to serialize item stacks.", e);
        }
    }

    public static ItemStack[] fromBase64Array(String base64) {
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(base64));
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
            ItemStack[] items = new ItemStack[dataInput.readInt()];

            for (int i = 0; i < items.length; i++) {
                items[i] = (ItemStack) dataInput.readObject();
            }

            dataInput.close();
            return items;
        } catch (ClassNotFoundException e) {
            try {
                throw new IOException("Unable to deserialize item stacks.", e);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String toBase64(ItemStack item) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        BukkitObjectOutputStream dataOutput;
        try {
            dataOutput = new BukkitObjectOutputStream(outputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
            dataOutput.writeObject(item);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
            dataOutput.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return Base64Coder.encodeLines(outputStream.toByteArray());
    }

    public static ItemStack fromBase64(String base64) {
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(base64));
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);

            ItemStack item = (ItemStack) dataInput.readObject();
            dataInput.close();
            return item;
        } catch (ClassNotFoundException | IOException e) {
            throw new IllegalArgumentException("Error deserializing ItemStack from Base64.", e);
        }
    }

    public static String mapToJson(Map<Integer, ItemStack> data) {
        Map<Integer, String> serialized = new HashMap<>();
        for (Map.Entry<Integer, ItemStack> entry : data.entrySet()) {
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
        Map<Integer, ItemStack> dataList = new HashMap<>();
        for (Map.Entry<Integer, String> entry : serialized.entrySet()) {
            dataList.put(entry.getKey(), ItemSerializer.fromBase64(entry.getValue()));
        }
        return dataList;
    }
}
