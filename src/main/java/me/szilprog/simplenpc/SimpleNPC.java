package me.szilprog.simplenpc;

import lombok.Getter;
import me.szilprog.simplenpc.commands.NPCCommand;
import me.szilprog.simplenpc.listeners.ChatListener;
import me.szilprog.simplenpc.listeners.InventoryListener;
import me.szilprog.simplenpc.listeners.PlayerListeners;
import me.szilprog.simplenpc.listeners.WorldListener;
import me.szilprog.simplenpc.npc.NPC;
import me.szilprog.simplenpc.utils.ConfigManager;
import me.szilprog.simplenpc.utils.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SimpleNPC extends JavaPlugin {

    public static final int VERSION = 9;
    @Getter private static SimpleNPC instance;
    @Getter private List<NPC> npcs = new ArrayList<>();

    @Override
    public void onEnable() {
        int pluginId = 10919;
        Metrics metrics = new Metrics(this, pluginId);
        if (instance == null) instance = this;
        try {
            ConfigManager.loadMainConfig();
        } catch (IOException e) {
            e.printStackTrace();
        }

        getServer().getPluginManager().registerEvents(new ChatListener(), this);
        getServer().getPluginManager().registerEvents(new InventoryListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerListeners(), this);
        getServer().getPluginManager().registerEvents(new WorldListener(), this);

        getCommand("npc").setExecutor(new NPCCommand());

        Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "Simple NPC Enabled!");

        for (Player p : Bukkit.getOnlinePlayers()) {
            for (NPC npc : npcs) {
                if (npc.getLocation().getWorld().toString().equals(p.getLocation().getWorld().toString()))
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
}
