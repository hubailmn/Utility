package cc.hubailmn.util.interaction;

import lombok.Getter;
import cc.hubailmn.util.interaction.player.PlayerSoundUtil;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public final class SoundPreset {

    private SoundPreset() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static void play(Player player, SoundType soundType) {
        if (soundType != null) {
            PlayerSoundUtil.playSound(player, soundType.getSound(), soundType.getVolume(), soundType.getPitch());
        }
    }

    @Getter
    public enum SoundType {
        CONFIRM(Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1F, 1F),
        SUCCESS(Sound.UI_TOAST_CHALLENGE_COMPLETE, 1F, 1.2F),
        DENY(Sound.BLOCK_NOTE_BLOCK_BASS, 1F, 0.5F),
        ERROR(Sound.BLOCK_ANVIL_LAND, 1F, 0.8F),
        CANCEL(Sound.BLOCK_NOTE_BLOCK_BELL, 1F, 0.3F),
        WARNING(Sound.ENTITY_VILLAGER_NO, 1F, 1.2F),
        ALERT(Sound.ENTITY_ENDERMAN_SCREAM, 0.8F, 1.2F),
        NOTIFY(Sound.BLOCK_NOTE_BLOCK_PLING, 1F, 1.5F),
        PING(Sound.UI_BUTTON_CLICK, 1F, 1.6F),
        CLICK(Sound.UI_BUTTON_CLICK, 1F, 1F),
        OPEN_MENU(Sound.BLOCK_CHEST_OPEN, 1F, 1F),
        CLOSE_MENU(Sound.BLOCK_CHEST_CLOSE, 1F, 1F),
        OPEN_ENDER_CHEST(Sound.BLOCK_ENDER_CHEST_OPEN, 1F, 1F),
        CLOSE_ENDER_CHEST(Sound.BLOCK_ENDER_CHEST_CLOSE, 1F, 1F),

        PICKUP_ITEM(Sound.ENTITY_ITEM_PICKUP, 1F, 1F),
        PICKUP_BARREL(Sound.BLOCK_BARREL_OPEN, 1F, 1F),

        PLAYER_HURT(Sound.ENTITY_PLAYER_HURT, 1F, 1F),
        CRITICAL_HIT(Sound.ENTITY_PLAYER_ATTACK_CRIT, 1F, 1.5F),
        ARROW_HIT(Sound.ENTITY_ARROW_HIT_PLAYER, 1F, 1F),
        STRONG_ENEMY(Sound.ENTITY_WITHER_SPAWN, 1F, 1F),
        THREAT_PASSING(Sound.ENTITY_PHANTOM_SWOOP, 1F, 1.2F),
        VICTORY(Sound.UI_TOAST_CHALLENGE_COMPLETE, 1F, 1.5F),

        PORTAL_ENTER(Sound.BLOCK_PORTAL_TRAVEL, 1F, 1F),
        PORTAL_TRIGGER(Sound.BLOCK_PORTAL_TRIGGER, 1F, 1F),
        TELEPORT(Sound.ENTITY_ENDERMAN_TELEPORT, 1F, 1.2F),
        RAIN(Sound.WEATHER_RAIN, 1F, 1F),
        GRASS_BREAK(Sound.BLOCK_GRASS_BREAK, 1F, 1F),
        LEAVES_BREAK(Sound.BLOCK_AZALEA_LEAVES_BREAK, 1F, 1F),
        CAVE(Sound.AMBIENT_CAVE, 1F, 1F),
        ENDER_DRAGON_GROWL(Sound.ENTITY_ENDER_DRAGON_GROWL, 1F, 1.5F),

        COUNTDOWN(Sound.BLOCK_NOTE_BLOCK_PLING, 1F, 2F),
        GAME_TRIGGER(Sound.BLOCK_BEEHIVE_ENTER, 1F, 1F),
        SLIME_JUMP(Sound.ENTITY_SLIME_JUMP, 1F, 1F),
        TIMER_START(Sound.ENTITY_TNT_PRIMED, 1F, 1F),

        INVENTORY_CLOSE(Sound.BLOCK_BARREL_CLOSE, 1F, 1F),
        ARMOR_EQUIP_GENERIC(Sound.ITEM_ARMOR_EQUIP_GENERIC, 1F, 1F),
        ARMOR_EQUIP_RARE(Sound.ITEM_ARMOR_EQUIP_DIAMOND, 1F, 1.5F),
        EXPERIENCE_ORB(Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1F, 1F),
        LEVEL_UP(Sound.ENTITY_PLAYER_LEVELUP, 1F, 1.2F),
        TOAST_IN(Sound.UI_TOAST_IN, 1F, 1.2F),
        TOAST_OUT(Sound.UI_TOAST_OUT, 1F, 1.2F),
        PAGE_FLIP(Sound.ITEM_BOOK_PAGE_TURN, 1F, 1F),
        CRAFTING_USE(Sound.UI_CARTOGRAPHY_TABLE_TAKE_RESULT, 1F, 1F),

        TRADE_SUCCESS(Sound.ENTITY_VILLAGER_YES, 1F, 1.2F),
        TRADE_AMBIENCE(Sound.ENTITY_WANDERING_TRADER_AMBIENT, 1F, 1.2F),

        MUSIC_DISC(Sound.MUSIC_DISC_PIGSTEP, 1F, 1F),
        ANIMAL_AURA(Sound.ENTITY_PARROT_AMBIENT, 1F, 1.5F),

        DRAMATIC_DEATH(Sound.ENTITY_ELDER_GUARDIAN_DEATH, 1F, 1F),
        EVENT_HORN(Sound.EVENT_RAID_HORN, 1F, 1.2F);

        private final Sound sound;
        private final float volume;
        private final float pitch;

        SoundType(Sound sound, float volume, float pitch) {
            this.sound = sound;
            this.volume = volume;
            this.pitch = pitch;
        }
    }
}