package com.xy.easymagic;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.SPacketSetExperience;

/**
 * Experience point maths for the reroll cost.
 *
 * <p>Vanilla never lowers {@code experienceTotal} when levels are spent: enchanting
 * ({@code onEnchant}) and the anvil ({@code addExperienceLevel(-n)}) only change
 * {@code experienceLevel}. Reading or rebuilding the bar from {@code experienceTotal} therefore
 * hands back every level spent since the player last died, which is how a reroll appeared to
 * add XP (GitHub issue #2). Everything here works from the level and bar progress the player
 * actually has.
 */
public final class XpUtil {

    private XpUtil() {
    }

    /** Total points needed to reach {@code level} from zero (the 1.8+ curve 1.12.2 uses). */
    public static int pointsForLevel(int level) {
        if (level <= 0) return 0;
        if (level <= 16) return level * level + 6 * level;
        if (level <= 31) return (int) (2.5 * level * level - 40.5 * level + 360);
        return (int) (4.5 * level * level - 162.5 * level + 2220);
    }

    /** Points the player can spend right now: whole levels plus the partly filled bar. */
    public static int currentPoints(EntityPlayer player) {
        return pointsForLevel(player.experienceLevel)
                + Math.round(player.experience * player.xpBarCap());
    }

    /** Takes {@code points} off the bar, dropping levels as needed, and syncs the client. */
    public static void removePoints(EntityPlayerMP player, int points) {
        int remaining = Math.max(0, currentPoints(player) - points);
        player.experienceTotal = Math.max(0, player.experienceTotal - points);
        player.experienceLevel = 0;
        player.experience = 0.0F;
        while (remaining > 0) {
            int cap = player.xpBarCap();
            if (remaining >= cap) {
                remaining -= cap;
                player.experienceLevel++;
            } else {
                player.experience = (float) remaining / (float) cap;
                break;
            }
        }
        // EntityPlayerMP only resends XP when experienceTotal changes, which it may not (already 0).
        player.connection.sendPacket(new SPacketSetExperience(
                player.experience, player.experienceTotal, player.experienceLevel));
    }
}
