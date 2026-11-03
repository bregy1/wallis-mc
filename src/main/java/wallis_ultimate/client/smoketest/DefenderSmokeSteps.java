package wallis_ultimate.client.smoketest;

import static wallis_ultimate.client.smoketest.ClientSmokeTest.await;
import static wallis_ultimate.client.smoketest.ClientSmokeTest.check;
import static wallis_ultimate.client.smoketest.ClientSmokeTest.command;
import static wallis_ultimate.client.smoketest.ClientSmokeTest.log;
import static wallis_ultimate.client.smoketest.ClientSmokeTest.screenshot;
import static wallis_ultimate.client.smoketest.ClientSmokeTest.step;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import wallis_ultimate.entity.Defender;
import wallis_ultimate.entity.Dropship;
import wallis_ultimate.entity.RegularZombie;
import wallis_ultimate.survival.Garrisons;
import wallis_ultimate.worldgen.Blueprint;
import wallis_ultimate.worldgen.DistrictSites;
import wallis_ultimate.worldgen.SafeSpot;

/**
 * SURV-011 in a real client, at Earl's Salvage: its guards are at their posts, zombies coming to the gate get shot, lost
 * guards are replaced from the air with parachutes, mining turns the guards on the player, and what got broken comes back.
 */
final class DefenderSmokeSteps {
    private static final String SPOT = "earls_salvage";
    private static final int SPOT_GUARDS = 10;
    private static final int ZOMBIES = 14;
    private static final int ZOMBIE_DISTANCE = 10;
    private static final int FIGHT_TICKS = 2400;
    private static final int LOST_GUARDS = 4;
    private static final int FAST_REINFORCEMENT_TICKS = 100;

    private static Vec3 home;
    private static BlockPos mined;
    private static BlockState minedState;
    private static int zombiesAtStart;

    private DefenderSmokeSteps() {
    }

    static List<ClientSmokeTest.Step> steps() {
        return List.of(
                step("go to the safe spot", 40, player -> {
                    home = player.position();
                    command("difficulty normal");
                    command("gamerule doMobSpawning false");
                    command("time set noon");
                    command("effect give @s minecraft:resistance infinite 4 true");
                    BlockPos arrival = spot().arrival();
                    command(String.format("tp @s %.2f %d %.2f 180 0", arrival.getX() + 0.5, arrival.getY(), arrival.getZ() + 0.5));
                    Garrisons.setReinforceDelayForTests(FAST_REINFORCEMENT_TICKS);
                }),
                await("the guards take their posts", 400, player -> guards().size() >= SPOT_GUARDS),
                step("check the guards", 20, player -> {
                    check("a safe spot has its guards when someone arrives", guards().size() == SPOT_GUARDS, guards().size() + " guards");
                    check("they carry rifles", guards().stream().allMatch(guard -> !guard.getMainHandItem().isEmpty()), "");
                    Minecraft.getInstance().options.hideGui = true;
                    Defender guard = guards().get(0);
                    command("gamemode spectator");
                    command(String.format("tp @s %.2f %.2f %.2f 180 10", guard.getX() + 0.5, guard.getY() + 1.2, guard.getZ() + 4.0));
                }),
                step("picture of a guard", 2, player -> screenshot("defender-post")),
                step("send zombies to the gate", 30, player -> {
                    // South of the signpost: that's where a safe spot's way in is, so they can see each other.
                    BlockPos signpost = spot().signpost();
                    for (int i = 0; i < ZOMBIES; i++) {
                        int x = signpost.getX() - ZOMBIES / 4 + i / 2;
                        int z = signpost.getZ() + ZOMBIE_DISTANCE + i % 2 * 2;
                        int y = server().getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
                        command(String.format("summon wallis_ultimate:zombie %d %d %d {Tags:[\"smoketest\"]}", x, y, z));
                    }
                    zombiesAtStart = ZOMBIES;
                    command(String.format("tp @s %d %d %d 0 30", signpost.getX(), signpost.getY() + 14, signpost.getZ() - 4));
                }),
                await("the fight gets going", 300, player -> zombiesHurtOrDead() >= 3),
                step("picture of the fight", 2, player -> {
                    screenshot("defender-fight");
                    check("defenders shoot the zombies coming at them", zombiesHurtOrDead() >= 3, zombiesHurtOrDead() + " zombies hit or dead");
                    check("zombies go for the defenders", zombies().stream().anyMatch(zombie -> zombie.getTarget() instanceof Defender) || zombies().isEmpty(), "");
                }),
                // Guards spread along the walls, so only a few can see any one spot: this takes about a minute.
                await("the fight is over", FIGHT_TICKS, player -> zombies().size() <= 2),
                step("check who won, and lose some guards", 10, player -> {
                    check("a safe spot holds against a group of zombies", zombies().size() <= 2, zombies().size() + " of " + zombiesAtStart + " zombies left");
                    log("guards left after the fight: " + guards().size());
                    // If a fight ever stalls, this says where: a zombie nobody can reach shows up here.
                    BlockPos center = spot().center();
                    zombies().forEach(zombie -> log("zombie left at " + zombie.blockPosition().subtract(center) + " from the center, target " + zombie.getTarget()));
                    command("kill @e[type=wallis_ultimate:zombie,distance=..200]");
                    command("kill @e[type=wallis_ultimate:defender,limit=" + LOST_GUARDS + ",sort=random,distance=..200]");
                }),
                await("the dropship arrives", 500, player -> dropship().findAny().isPresent()),
                step("look up", 80, player -> {
                    Dropship ship = dropship().findAny().orElseThrow();
                    BlockPos center = spot().center();
                    command(String.format("tp @s %d %d %d %.1f -25", center.getX() - 26, center.getY() + 18, center.getZ() - 26, -45.0));
                    log("dropship at " + ship.blockPosition().toShortString());
                }),
                await("it's over the safe spot", 400, player -> dropship().anyMatch(ship -> ship.distanceToSqr(Vec3.atCenterOf(spot().center())) < 900.0)),
                step("picture of the dropship", 2, player -> screenshot("defender-dropship")),
                await("troops jump out", 300, player -> guards().stream().anyMatch(Defender::isParachuting)),
                step("watch them come down", 30, player -> {
                    Defender trooper = guards().stream().filter(Defender::isParachuting).findFirst().orElseThrow();
                    command(String.format("tp @s %.2f %.2f %.2f 180 -5", trooper.getX(), trooper.getY() - 2.0, trooper.getZ() + 12.0));
                }),
                step("picture of the parachutes", 2, player -> screenshot("defender-parachute")),
                await("the guards are back to full strength", 900, player -> guards().size() >= SPOT_GUARDS && guards().stream().noneMatch(Defender::isParachuting)),
                step("check the reinforcements, then mine something", 10, player -> {
                    check("reinforcements replace lost guards", guards().size() == SPOT_GUARDS, guards().size() + " guards");
                    Minecraft.getInstance().options.hideGui = false;
                    command("gamemode survival");
                    BlockPos arrival = spot().arrival();
                    command(String.format("tp @s %.2f %d %.2f 180 0", arrival.getX() + 0.5, arrival.getY(), arrival.getZ() + 0.5));
                }),
                step("mine a block of the safe spot", 40, player -> {
                    ServerLevel level = server();
                    SafeSpot spot = spot();
                    mined = closestBuiltBlock(level, spot);
                    minedState = level.getBlockState(mined);
                    ServerPlayer serverPlayer = level.getServer().getPlayerList().getPlayer(player.getUUID());
                    level.getServer().execute(() -> serverPlayer.gameMode.destroyBlock(mined));
                }),
                await("the guards turn on the player", 400, player -> guards().stream().anyMatch(guard -> guard.getTarget() != null && guard.getTarget().getUUID().equals(player.getUUID()))),
                step("check the guards are angry, and make up", 10, player -> {
                    check("mining in a safe spot turns its guards on the player",
                            guards().stream().anyMatch(guard -> guard.getTarget() != null && guard.getTarget().getUUID().equals(player.getUUID())), "");
                    check("the player is told", ClientSmokeTest.chatContains("You break it"), "");
                    ServerPlayer serverPlayer = server().getServer().getPlayerList().getPlayer(player.getUUID());
                    server().getServer().execute(() -> {
                        Garrisons.forgive(serverPlayer);
                        guards().forEach(guard -> guard.setTarget(null));
                    });
                    command("gamemode spectator");
                }),
                await("a guard repairs what got mined", 900, player -> server().getBlockState(mined).is(minedState.getBlock())),
                step("check the repair, and go back", 20, player -> {
                    check("guards repair what got broken", server().getBlockState(mined).is(minedState.getBlock()), mined.toShortString());
                    Garrisons.setReinforceDelayForTests(Garrisons.REINFORCE_DELAY_TICKS);
                    command("gamemode survival");
                    command("difficulty peaceful");
                    command("gamerule doMobSpawning true");
                    command(String.format("tp @s %.2f %.2f %.2f", home.x, home.y, home.z));
                }));
    }

    private static SafeSpot spot() {
        return DistrictSites.get(server()).spot(SPOT).orElseThrow();
    }

    private static ServerLevel server() {
        return Minecraft.getInstance().getSingleplayerServer().overworld();
    }

    private static AABB area() {
        return new AABB(spot().center()).inflate(96.0, 96.0, 96.0);
    }

    /** From the integrated server: the client isn't told about targets or health. */
    private static List<Defender> guards() {
        return server().getEntitiesOfClass(Defender.class, area(), guard -> SPOT.equals(guard.spotId()));
    }

    private static List<RegularZombie> zombies() {
        return server().getEntitiesOfClass(RegularZombie.class, area(), zombie -> zombie.getTags().contains("smoketest"));
    }

    private static int zombiesHurtOrDead() {
        List<RegularZombie> alive = zombies();
        return zombiesAtStart - alive.size() + (int) alive.stream().filter(zombie -> zombie.getHealth() < zombie.getMaxHealth()).count();
    }

    private static Stream<Dropship> dropship() {
        Iterable<Entity> all = Minecraft.getInstance().level.entitiesForRendering();
        return StreamSupport.stream(all.spliterator(), false).filter(Dropship.class::isInstance).map(Dropship.class::cast);
    }

    /** The block of the build nearest to where travellers arrive, and not a chest or the signpost. */
    private static BlockPos closestBuiltBlock(ServerLevel level, SafeSpot spot) {
        Blueprint blueprint = DistrictSites.blueprintOf(spot.id());
        BlockPos best = null;
        double bestDistance = Double.MAX_VALUE;
        for (Map.Entry<BlockPos, BlockState> block : blueprint.blocks().entrySet()) {
            BlockPos at = spot.origin().offset(block.getKey());
            double distance = at.distSqr(spot.arrival());
            BlockState now = level.getBlockState(at);
            if (distance < bestDistance && distance > 1.0 && !block.getValue().isAir() && block.getValue().is(now.getBlock())
                    && now.getDestroySpeed(level, at) >= 0.0F && !blueprint.lootTables().containsKey(block.getKey())) {
                best = at;
                bestDistance = distance;
            }
        }
        return best;
    }
}
