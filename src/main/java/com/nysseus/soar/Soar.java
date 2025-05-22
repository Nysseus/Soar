package com.nysseus.soar;

import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.FlightAbility;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.permissions.Permission;
import org.bukkit.util.Vector;


public class Soar extends FlightAbility implements AddonAbility {
    public enum UsageType {
        SOAR, HOVER
    }
    private UsageType usageType;

    Listener listener;
    private long cooldown;
    private boolean FlightEnabled;
    private boolean HoverEnabled;
    private boolean isHovering;
    private float hoverSpeed;

    public Soar(Player player, UsageType usage) {
        super(player);
        usageType = usage;

        cooldown = ConfigManager.defaultConfig.get().getLong("ExtraAbilities.Nysseus.Soar.Cooldown", 10000);
        FlightEnabled = ConfigManager.defaultConfig.get().getBoolean("ExtraAbilities.Nysseus.Soar.FlightEnabled", true);
        HoverEnabled = ConfigManager.defaultConfig.get().getBoolean("ExtraAbilities.Nysseus.Soar.HoverEnabled", true);
        hoverSpeed = (float) ConfigManager.defaultConfig.get().getDouble("ExtraAbilities.Nysseus.Soar.HoverSpeed", 0.3);

        isHovering = false;

        if (usageType.equals(UsageType.SOAR) && !FlightEnabled) {
            remove();
        }

        if (usageType.equals(UsageType.HOVER) && !HoverEnabled) {
            remove();
        }

        if (usageType.equals(UsageType.HOVER)) {
            if (isOnGround()) {
                Location loc = player.getLocation();
                getAirbendingParticles().display(loc, 30, (Math.random()), 0.3, (Math.random()));
                playAirbendingSound(loc);
                Vector vector = new Vector(0, 2, 0);
                player.setVelocity(vector);
            }
            isHovering = true;
            player.setFlying(true);
            player.setFlySpeed(hoverSpeed);
        }

        start();
    }

    private boolean isOnGround() {
        Location loc = player.getLocation();
        return loc.getBlock().getRelative(BlockFace.DOWN).getType().isSolid();
    }

    public void CancelMove() {
        if(!(player.getGameMode().equals(GameMode.CREATIVE) || player.getGameMode().equals(GameMode.SPECTATOR)) && usageType.equals(UsageType.HOVER)) {
            player.setFlying(false);
        }
        if (usageType.equals(UsageType.HOVER)) {
            isHovering = false;
        }

        bPlayer.addCooldown(this);
        remove();
    }

    @Override
    public void progress() {
        if (!bPlayer.canBendIgnoreCooldowns(this)) {
            if(!(player.getGameMode().equals(GameMode.CREATIVE) || player.getGameMode().equals(GameMode.SPECTATOR))) {
                player.setFlying(false);
            }
            remove();
            bPlayer.addCooldown(this);
            return;
        }

        if (usageType.equals(UsageType.SOAR) && !isOnGround() && player.isSneaking()) {
            Vector direction = player.getEyeLocation().getDirection();
            player.setVelocity(direction.clone().multiply(2));
        } else if (usageType.equals(UsageType.SOAR)) {
            CancelMove();
        }

        if (usageType.equals(UsageType.HOVER) &&
                isOnGround() &&
                (System.currentTimeMillis() > this.getStartTime() + 500) &&
                isHovering) {
            CancelMove();
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

        ConfigManager.getConfig().addDefault("ExtraAbilities.Nysseus.Soar.HoverSpeed", 0.3);

        ConfigManager.getConfig().addDefault("ExtraAbilities.Nysseus.Soar.HoverEnabled", true);
        ConfigManager.getConfig().addDefault("ExtraAbilities.Nysseus.Soar.FlightEnabled", true);
        ConfigManager.defaultConfig.save();

        ProjectKorra.plugin.getLogger().info(getName() + " " + getVersion() + " by " + getAuthor() + " has been successfully enabled.");
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
        return "1.0";
    }

}
