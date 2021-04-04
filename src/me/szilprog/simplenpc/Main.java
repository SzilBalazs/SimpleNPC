package me.szilprog.simplenpc;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main extends JavaPlugin {

    public static Main instance;
    List<NPC> npcs = new ArrayList<>();

    @Override
    public void onEnable() {
        int pluginId = 10919;
        Metrics metrics = new Metrics(this, pluginId);
        instance=this;
        try {
            ConfigManager.loadMainConfig();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Bukkit.getPluginManager().registerEvents(new Listeners(), this);
        Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "Simple NPC Enabled!");
        for (Player p : Bukkit.getOnlinePlayers()) {
            for (NPC npc : npcs) {
                npc.addNPCPacket(p);
            }
        }
        new BukkitRunnable() {
            @Override
            public void run() {
                for (NPC npc : npcs) {
                    npc.cooldownUpdate();
                }
            }
        }.runTaskTimer(this, 0, 20);
        new BukkitRunnable() {
            @Override
            public void run() {
                for (NPC npc : npcs) {
                    if (npc.isLookClose()) {
                        for (Player player : Bukkit.getOnlinePlayers()) {
                            npc.sendLookPlayer(player);
                        }
                    }
                }
            }
        }.runTaskTimer(this, 0, 2);
    }

    @Override
    public void onDisable() {
        Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Simple NPC Disabled!");
        for (Player p : Bukkit.getOnlinePlayers()) {
            for (NPC npc : npcs) {
                npc.removeNPCPacket(p);
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (cmd.getLabel().equalsIgnoreCase("reloadnpc")) {
            if (!sender.hasPermission("npc.reload")) {
                sender.sendMessage(ChatColor.RED + "You don't have the permission to use this command!");
                return false;
            }
            for (Player p : Bukkit.getOnlinePlayers()) {
                for (NPC npc : npcs) {
                    npc.removeNPCPacket(p);
                }
            }
            npcs.clear();
            try {
                ConfigManager.loadMainConfig();
            } catch (IOException e) {
                e.printStackTrace();
            }
            for (Player p : Bukkit.getOnlinePlayers()) {
                for (NPC npc : npcs) {
                    npc.addNPCPacket(p);
                }
            }
            Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "Simple NPC Reloaded!");

        }

        // If the player (or console) uses our command correct, we can return true
        return true;
    }
}
