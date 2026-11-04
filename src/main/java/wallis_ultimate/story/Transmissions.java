package wallis_ultimate.story;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.network.PacketDistributor;
import wallis_ultimate.WallisUltimate;
import wallis_ultimate.network.CaptionPacket;
import wallis_ultimate.network.ModNetwork;

/**
 * Lines that reach a player wherever they are, over the radio: Lurleen reacting to what the team does (STORY-003), and the
 * horde warning "transmitted from Lurleen" (ENEMY-006). The text comes from the lang file, so it follows the clean-language
 * option. Server side only.
 */
public final class Transmissions {
    public static final int TICKS_PER_CHARACTER = 2;
    public static final int MIN_TICKS = 60;
    public static final int EXTRA_TICKS = 40;

    private Transmissions() {
    }

    /** The lang key of a line in {@link ChapterOneTalks#TRANSMISSIONS}. */
    public static String key(String name) {
        return "story." + WallisUltimate.MOD_ID + ".transmission." + name;
    }

    /** Lurleen says one of the lines in {@link ChapterOneTalks#TRANSMISSIONS} over the radio to one player. */
    public static void fromLurleen(ServerPlayer player, String name) {
        int length = ChapterOneTalks.TRANSMISSIONS.getOrDefault(name, "").length();
        caption(player, Speaker.LURLEEN, key(name), Math.max(MIN_TICKS, EXTRA_TICKS + length * TICKS_PER_CHARACTER));
        player.playNotifySound(SoundEvents.NOTE_BLOCK_HAT.value(), SoundSource.VOICE, 0.6F, 1.6F);
    }

    /** For horde nights (ENEMY-006, Q-157): call this for every player the horde is coming for, when it starts. */
    public static void hordeStarting(ServerPlayer player) {
        fromLurleen(player, "horde_starting");
    }

    /** One line on the HUD, in a cutscene or over the radio. */
    public static void caption(ServerPlayer player, Speaker speaker, String textKey, int ticks) {
        ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                new CaptionPacket(speaker.nameKey(), speaker.color(), textKey, ticks));
    }
}
