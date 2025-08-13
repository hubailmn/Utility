package cc.hubailmn.utility.item;

import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public final class ItemStackSerializer {

    private ItemStackSerializer() {
        throw new UnsupportedOperationException("This is a utility class.");
    }

    public static byte[] toBytes(ItemStack[] items) {
        if (items == null) items = new ItemStack[0];
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try (GZIPOutputStream gzipOutputStream = new GZIPOutputStream(byteArrayOutputStream);
             BukkitObjectOutputStream boos = new BukkitObjectOutputStream(gzipOutputStream)) {
            boos.writeInt(items.length);
            for (ItemStack item : items) {
                boos.writeObject(item);
            }
            boos.flush();
            gzipOutputStream.finish();
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static ItemStack[] fromBytes(byte[] bytes) {
        if (bytes == null || bytes.length == 0) return new ItemStack[0];
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
             GZIPInputStream gzipInputStream = new GZIPInputStream(byteArrayInputStream);
             BukkitObjectInputStream bois = new BukkitObjectInputStream(gzipInputStream)) {
            int len = bois.readInt();
            ItemStack[] items = new ItemStack[len];
            for (int i = 0; i < len; i++) {
                items[i] = (ItemStack) bois.readObject();
            }
            return items;
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] singleToBytes(ItemStack item) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try (GZIPOutputStream gzipOutputStream = new GZIPOutputStream(byteArrayOutputStream);
             BukkitObjectOutputStream boos = new BukkitObjectOutputStream(gzipOutputStream)) {
            boos.writeObject(item);
            boos.flush();
            gzipOutputStream.finish();
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static ItemStack singleFromBytes(byte[] bytes) {
        if (bytes == null || bytes.length == 0) return null;
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
             GZIPInputStream gzipInputStream = new GZIPInputStream(byteArrayInputStream);
             BukkitObjectInputStream bois = new BukkitObjectInputStream(gzipInputStream)) {
            return (ItemStack) bois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] mapToBytes(Map<Integer, ItemStack> map) {
        if (map == null) map = new HashMap<>();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try (GZIPOutputStream gzipOutputStream = new GZIPOutputStream(byteArrayOutputStream);
             BukkitObjectOutputStream boos = new BukkitObjectOutputStream(gzipOutputStream)) {
            boos.writeInt(map.size());
            for (Map.Entry<Integer, ItemStack> entry : map.entrySet()) {
                boos.writeInt(entry.getKey());
                boos.writeObject(entry.getValue());
            }
            boos.flush();
            gzipOutputStream.finish();
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Map<Integer, ItemStack> mapFromBytes(byte[] bytes) {
        Map<Integer, ItemStack> map = new HashMap<>();
        if (bytes == null || bytes.length == 0) return map;
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
             GZIPInputStream gzipInputStream = new GZIPInputStream(byteArrayInputStream);
             BukkitObjectInputStream bois = new BukkitObjectInputStream(gzipInputStream)) {
            int size = bois.readInt();
            for (int i = 0; i < size; i++) {
                int key = bois.readInt();
                ItemStack value = (ItemStack) bois.readObject();
                map.put(key, value);
            }
            return map;
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
