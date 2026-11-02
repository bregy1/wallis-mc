package wallis_ultimate.gametest;

import com.mojang.authlib.GameProfile;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import wallis_ultimate.WallisUltimate;
import wallis_ultimate.combat.Trinkets;
import wallis_ultimate.entity.RegularZombie;
import wallis_ultimate.registry.ModEntities;
import wallis_ultimate.registry.ModItems;
import wallis_ultimate.survival.Downed;

/** Checks for CMBT-003: the 20 trinket slots and what the Chapter 1 trinkets do. */
@GameTestHolder(WallisUltimate.MOD_ID)
@PrefixGameTestTemplate(false)
public final class TrinketGameTests {
    private static final String PLATFORM = "platform";
    /** These change the time of day, so each gets its own batch (batches run one after another). */
    private static final String NIGHT = "trinket_night";
    private static final String DAY = "trinket_day";
    private static final Map<String, Integer> SLOTS = Map.of("head", 2, "necklace", 2, "back", 1, "body", 2, "belt", 2,
            "hands", 2, "bracelet", 2, "ring", 4, "charm", 3);

    private TrinketGameTests() {
    }

    private static Player mockPlayer(GameTestHelper helper, String name, BlockPos relativePos) {
        BlockPos pos = helper.absolutePos(relativePos);
        Player player = new Player(helper.getLevel(), pos, 0.0F, new GameProfile(UUID.randomUUID(), name)) {
            @Override
            public boolean isSpectator() {
                return false;
            }

            @Override
            public boolean isCreative() {
                return false;
            }
        };
        player.moveTo(Vec3.atBottomCenterOf(pos));
        return player;
    }

    private static ICuriosItemHandler curios(Player player) {
        return CuriosApi.getCuriosInventory(player).orElseThrow(() -> new IllegalStateException("no Curios inventory"));
    }

    private static void equip(Player player, String slot, Item trinket) {
        curios(player).setEquippedCurio(slot, 0, new ItemStack(trinket));
    }

    /** Q-140: exactly the agreed layout, 20 slots in all. */
    @GameTest(template = PLATFORM)
    public static void playersHaveTheTwentySlotLayout(GameTestHelper helper) {
        Player player = mockPlayer(helper, "tk_slots", new BlockPos(2, 2, 2));
        Map<String, ICurioStacksHandler> slots = curios(player).getCurios();
        int total = 0;
        for (Map.Entry<String, Integer> expected : SLOTS.entrySet()) {
            ICurioStacksHandler handler = slots.get(expected.getKey());
            if (handler == null || handler.getSlots() != expected.getValue()) {
                helper.fail(expected.getKey() + " has " + (handler == null ? "no" : handler.getSlots()) + " slots, expected " + expected.getValue());
            }
            total += handler.getSlots();
        }
        if (total != 20 || slots.size() != SLOTS.size()) {
            helper.fail(total + " slots of types " + slots.keySet());
        }
        helper.succeed();
    }

    @GameTest(template = PLATFORM)
    public static void attributeTrinketsGrantTheirAttribute(GameTestHelper helper) {
        Player player = mockPlayer(helper, "tk_attr", new BlockPos(2, 2, 2));
        assertModifier(helper, player, ModItems.JOGGING_HEADBAND.get(), "head", Attributes.MOVEMENT_SPEED, 0.10);
        assertModifier(helper, player, ModItems.BRASS_KNUCKLES.get(), "hands", Attributes.ATTACK_DAMAGE, 1.0);
        assertModifier(helper, player, ModItems.THICK_FLANNEL.get(), "body", Attributes.ARMOR, 2.0);
        helper.succeed();
    }

    private static void assertModifier(GameTestHelper helper, Player player, Item trinket, String slot,
            Attribute attribute, double amount) {
        var modifiers = ((ICurioItem) trinket).getAttributeModifiers(new SlotContext(slot, player, 0, false, true), UUID.randomUUID(), new ItemStack(trinket)).get(attribute);
        if (modifiers.size() != 1 || modifiers.iterator().next().getAmount() != amount) {
            helper.fail(trinket + " grants " + modifiers);
        }
    }

    /** SURV-001: the Spare Clip adds shots to the downed pistol. */
    @GameTest(template = PLATFORM)
    public static void theSpareClipAddsPistolShots(GameTestHelper helper) {
        Player plain = mockPlayer(helper, "tk_noclip", new BlockPos(1, 2, 1));
        Player clip = mockPlayer(helper, "tk_clip", new BlockPos(5, 2, 5));
        equip(clip, "belt", ModItems.SPARE_CLIP.get());
        Downed.goDown(plain);
        Downed.goDown(clip);
        if (Downed.ammo(plain) != Downed.PISTOL_SHOTS || Downed.ammo(clip) != Downed.PISTOL_SHOTS + Trinkets.SPARE_CLIP_SHOTS) {
            helper.fail("ammo without clip " + Downed.ammo(plain) + ", with clip " + Downed.ammo(clip));
        }
        helper.succeed();
    }

    /** ENEMY-003: at night the Rabbit Foot makes enemies about 10% weaker. */
    @GameTest(template = PLATFORM, batch = NIGHT)
    public static void atNightTheRabbitFootWeakensEnemies(GameTestHelper helper) {
        helper.getLevel().setDayTime(18000);
        helper.runAfterDelay(2, () -> {
            float[] amounts = rabbitFootDamage(helper);
            if (Math.abs(amounts[0] - 10.0F * Trinkets.RABBIT_FOOT_DAMAGE_DEALT) > 0.001F
                    || Math.abs(amounts[1] - 10.0F * Trinkets.RABBIT_FOOT_DAMAGE_TAKEN) > 0.001F) {
                helper.fail("dealt " + amounts[0] + ", taken " + amounts[1]);
            }
            helper.killAllEntities();
            helper.succeed();
        });
    }

    @GameTest(template = PLATFORM, batch = DAY)
    public static void byDayTheRabbitFootDoesNothing(GameTestHelper helper) {
        helper.getLevel().setDayTime(1000);
        helper.runAfterDelay(2, () -> {
            float[] amounts = rabbitFootDamage(helper);
            if (amounts[0] != 10.0F || amounts[1] != 10.0F) {
                helper.fail("dealt " + amounts[0] + ", taken " + amounts[1]);
            }
            helper.killAllEntities();
            helper.succeed();
        });
    }

    /** Damage for a 10-point hit by a player with the Rabbit Foot on a zombie, and the other way round. */
    private static float[] rabbitFootDamage(GameTestHelper helper) {
        Player player = mockPlayer(helper, "tk_foot", new BlockPos(2, 2, 2));
        equip(player, "charm", ModItems.GRANNYS_RABBIT_FOOT.get());
        RegularZombie zombie = helper.spawn(ModEntities.ZOMBIE.get(), new BlockPos(5, 2, 5));
        LivingHurtEvent dealt = new LivingHurtEvent(zombie, helper.getLevel().damageSources().playerAttack(player), 10.0F);
        LivingHurtEvent taken = new LivingHurtEvent(player, helper.getLevel().damageSources().mobAttack(zombie), 10.0F);
        MinecraftForge.EVENT_BUS.post(dealt);
        MinecraftForge.EVENT_BUS.post(taken);
        return new float[] {dealt.getAmount(), taken.getAmount()};
    }
}
