package com.nysseus.soar;

import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.FlightAbility;
import com.projectkorra.projectkorra.airbending.AirScooter;
import com.projectkorra.projectkorra.airbending.AirSpout;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.permissions.Permission;
import org.bukkit.util.Vector;

import java.util.ArrayList;


public class Soar extends FlightAbility implements AddonAbility {
    public enum UsageType {
        SOAR, HOVER
    }
    protected UsageType usageType;

    Listener listener;
    private long cooldown;
    protected boolean FlightEnabled;
    protected boolean HoverEnabled;
    private boolean isHovering;
    private float hoverSpeed;
    private long Duration;

    private static ArrayList<Class> abilitiesToRemove = new ArrayList<>();


    public Soar(Player player, UsageType usage) {
        super(player);
        usageType = usage;

        cooldown = ConfigManager.defaultConfig.get().getLong("ExtraAbilities.Nysseus.Soar.Cooldown", 10000);
        Duration = ConfigManager.defaultConfig.get().getLong("ExtraAbilities.Nysseus.Soar.Duration", 120000);
        FlightEnabled = ConfigManager.defaultConfig.get().getBoolean("ExtraAbilities.Nysseus.Soar.FlightEnabled", true);
        HoverEnabled = ConfigManager.defaultConfig.get().getBoolean("ExtraAbilities.Nysseus.Soar.HoverEnabled", true);
        hoverSpeed = (float) ConfigManager.defaultConfig.get().getDouble("ExtraAbilities.Nysseus.Soar.HoverSpeed", 0.03);

        isHovering = false;

        if (player.getLocation().getBlock().getRelative(BlockFace.DOWN).getType().equals(Material.WATER)) {
            remove();
            return;
        }
        if (usageType.equals(UsageType.SOAR) && !FlightEnabled) {
            remove();
            return;
        }
        if (usageType.equals(UsageType.HOVER) && !HoverEnabled) {
            remove();
            return;
        }

        if (isOnGround() && usageType.equals(UsageType.HOVER)) {
            Location loc = player.getLocation();
            player.setAllowFlight(true);
            getAirbendingParticles().display(loc, 30, (Math.random()), 0.3, (Math.random()));
            playAirbendingSound(loc);
            Vector vector = new Vector(0, 2, 0);
            player.setVelocity(vector);
            Bukkit.getScheduler().runTask(ProjectKorra.plugin, () -> player.setFlying(true));
        }

        start();
    }

    private boolean isOnGround() {
        Location loc = player.getLocation();
        return loc.getBlock().getRelative(BlockFace.DOWN).getType().isSolid();
    }

    public void CancelMove(boolean override) {
        if((usageType.equals(UsageType.HOVER) || override) &&
                !(player.getGameMode().equals(GameMode.CREATIVE) || player.getGameMode().equals(GameMode.SPECTATOR))) {

            player.setFlying(false);
            player.setAllowFlight(false);
        }
        isHovering = false;
        player.setFlySpeed(0.1f);
        player.setGliding(false);
        bPlayer.addCooldown(this);
        remove();
    }


    @Override
    public void progress() {
        if (!bPlayer.canBendIgnoreBindsCooldowns(this) ||
                isWater(this.player.getLocation().getBlock()) ||
                ((System.currentTimeMillis() > this.getStartTime() + Duration) && Duration > 0) ||
                (usageType.equals(UsageType.SOAR) && !FlightEnabled) ||
                (usageType.equals(UsageType.HOVER) && !HoverEnabled)) {
            CancelMove(true);
            return;
        }

        for (Class clazz : abilitiesToRemove) {
            if (CoreAbility.getAbility(player, clazz) != null) {
                CoreAbility.getAbility(player, clazz).remove();
                if(usageType.equals(UsageType.HOVER)) {
                    player.setFlySpeed(hoverSpeed);
                    player.setAllowFlight(true);
                    player.setFlying(true);
                }
            }
        }

        if (usageType.equals(UsageType.SOAR) && player.isSneaking()) {
            Vector direction = player.getEyeLocation().getDirection();
            if (!player.isGliding()) {
                player.setGliding(true);
            }
            if (player.isFlying()) {
                player.setFlying(false);
               player.setAllowFlight(false);
            }
            player.setVelocity(direction.clone().multiply(1.5));
        }
        if (usageType.equals(UsageType.SOAR) && (System.currentTimeMillis() > this.getStartTime() + 500) && isOnGround()) {
            CancelMove(false);
        }
        if (usageType.equals(UsageType.SOAR) && !player.isSneaking()) {
            CancelMove(false);
        }

        if (usageType.equals(UsageType.SOAR)) {
            if (!player.isGliding()) {
                player.setGliding(true);
            }
        }

        if (usageType.equals(UsageType.HOVER)) {
            isHovering = true;

            player.setAllowFlight(true);
            player.setFlySpeed(hoverSpeed);
            player.setFlying(true);
        }
        if (usageType.equals(UsageType.HOVER) &&
                isOnGround() &&
                (System.currentTimeMillis() > this.getStartTime() + 500) &&
                isHovering) {
            CancelMove(false);
        }
    }

    @Override
    public boolean isSneakAbility() {
        return true;
    }

    @Override
    public boolean isHarmlessAbility() {
        return true;
    }

    @Override
    public long getCooldown() {
        return cooldown;
    }

    @Override
    public String getName() {
        return "Soar";
    }

    @Override
    public Location getLocation() {
        return null;
    }

    @Override
    public void load() {
        listener = new SoarListener();
        ProjectKorra.plugin.getServer().getPluginManager().registerEvents(listener, ProjectKorra.plugin);

        ProjectKorra.plugin.getServer().getPluginManager().addPermission(new Permission("bending.ability.Soar"));

        ConfigManager.getConfig().addDefault("ExtraAbilities.Nysseus.Soar.Cooldown", 10000);
        ConfigManager.getConfig().addDefault("ExtraAbilities.Nysseus.Soar.Duration", 120000);


        ConfigManager.getConfig().addDefault("ExtraAbilities.Nysseus.Soar.HoverSpeed", 0.03);

        ConfigManager.getConfig().addDefault("ExtraAbilities.Nysseus.Soar.HoverEnabled", true);
        ConfigManager.getConfig().addDefault("ExtraAbilities.Nysseus.Soar.FlightEnabled", true);
        ConfigManager.defaultConfig.save();

        ProjectKorra.plugin.getLogger().info(getName() + " " + getVersion() + " by " + getAuthor() + " has been successfully enabled.");

        abilitiesToRemove.add(AirScooter.class);
        abilitiesToRemove.add(AirSpout.class);
    }

    @Override
    public void stop() {
        HandlerList.unregisterAll(listener);
        ProjectKorra.plugin.getServer().getPluginManager().removePermission("bending.ability.Soar");
    }

    @Override
    public String getAuthor() {
        return "Nysseus";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public boolean isDefault() { return false; }

    @Override
    public String getDescription() {
        return "Become empty and become wind.";
    }

    @Override
    public String getInstructions() {
        return "Pressing left-click with this ability will allow you to hover. Holding sneak while in the air will propel you wherever you look.";
    }

    @Override
    public boolean isEnabled() {
        boolean result;
        if (!(ConfigManager.defaultConfig.get().getBoolean("ExtraAbilities.Nysseus.Soar.HoverEnabled", true)) &&
        !(ConfigManager.defaultConfig.get().getBoolean("ExtraAbilities.Nysseus.Soar.FlightEnabled", true))) {
            result = false;
        } else {
            result = true;
        }
        return result;
    }
}
