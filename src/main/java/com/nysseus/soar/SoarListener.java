package com.nysseus.soar;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.airbending.Suffocate;
import com.projectkorra.projectkorra.util.MovementHandler;
import com.projectkorra.projectkorra.waterbending.blood.Bloodbending;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;

public class SoarListener implements Listener {

    @EventHandler
    public void onSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
        Soar soar = CoreAbility.getAbility(player, Soar.class);

        if (Suffocate.isBreathbent(player)) {
            event.setCancelled(true);
            return;
        }
        if (Bloodbending.isBloodbent(player) || MovementHandler.isStopped(player)) {
            event.setCancelled(true);
            return;
        }
        if (bPlayer.isChiBlocked()) {
            event.setCancelled(true);
            return;
        }
        if (soar != null && !soar.FlightEnabled) {
            event.setCancelled(true);
            return;
        }
        if (event.getPlayer().isSneaking()) return;

        if (soar == null && bPlayer.canBend(CoreAbility.getAbility(Soar.class))) {
            new Soar(player, Soar.UsageType.SOAR);
            return;
        }
        if (soar != null && soar.usageType.equals(Soar.UsageType.HOVER) && !player.isSneaking()) {
            soar.usageType = Soar.UsageType.SOAR;
        }
    }

    @EventHandler
    public void onClick(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
        Soar soar = CoreAbility.getAbility(player, Soar.class);

        if (Suffocate.isBreathbent(player)) {
            event.setCancelled(true);
            return;
        }
        if (Bloodbending.isBloodbent(player) || MovementHandler.isStopped(player)) {
            event.setCancelled(true);
            return;
        }
        if (bPlayer.isChiBlocked()) {
            event.setCancelled(true);
            return;
        }
        if (soar != null && !soar.HoverEnabled) {
            event.setCancelled(true);
            return;
        }

        if (event.getAction() != Action.LEFT_CLICK_BLOCK && event.getAction() != Action.LEFT_CLICK_AIR) return;
        if (event.getAction() == Action.LEFT_CLICK_BLOCK && event.isCancelled()) return;

        if (soar == null && bPlayer.canBend(CoreAbility.getAbility(Soar.class))) {
            new Soar(player, Soar.UsageType.HOVER);
            return;
        }
        if (soar != null && soar.usageType.equals(Soar.UsageType.HOVER)) {
            soar.CancelMove(false);
            return;
        }
        if (soar != null && soar.usageType.equals(Soar.UsageType.SOAR)) {
            soar.usageType = Soar.UsageType.HOVER;

        }
    }
    
    @EventHandler
    public void onSwitchSlot(PlayerItemHeldEvent event) {
        Player player = event.getPlayer().getPlayer();
        BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
        Soar soar = CoreAbility.getAbility(player, Soar.class);
        String newAbilityName = bPlayer.getAbilities().get(event.getNewSlot() + 1);

        if (soar == null || (soar.usageType.equals(Soar.UsageType.OFFSLOT) && !newAbilityName.equals("Soar"))) {
            return;
        }

        if (soar.usageType.equals(Soar.UsageType.OFFSLOT)) {
            soar.usageType = Soar.UsageType.HOVER;
        } else {
            soar.usageType = Soar.UsageType.OFFSLOT;
            soar.SetOffSlotTime();
        }

    }
}

