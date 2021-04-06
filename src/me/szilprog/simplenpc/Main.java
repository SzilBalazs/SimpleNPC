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

    public static final int VERSION=6;
    public static Main instance;
    List<NPC> npcs = new ArrayList<>();

    @Override
    public void onEnable() {
        int pluginId = 10919;
        Metrics metrics = new Metrics(this, pluginId);
        if (instance == null) instance=this;
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
        if (cmd.getLabel().equalsIgnoreCase("npc")) {
            if (args.length == 0) sender.sendMessage(ChatColor.RED + "Invalid usage: /npc create/delete/edit/reload {name}");
            else if (sender.hasPermission("npc.admin")) {

                if (args[0].equalsIgnoreCase("create") && args.length == 2) {
                    try {
                        if (sender instanceof Player) {
                            ConfigManager.createNPC(args[1], ((Player) sender).getLocation());
                            sendSuccesMessage(sender);
                        }
                        else {
                            ConfigManager.createNPC(args[1]);
                            sendSuccesMessage(sender);
                        }

                    } catch (IOException e) {
                        getLogger().warning("Error Found");
                    }
                }
                else if (args[0].equalsIgnoreCase("delete") && args.length == 2) {
                    ConfigManager.deleteNPC(args[1]);
                    sendSuccesMessage(sender);
                }
                else if (args[0].equalsIgnoreCase("edit") && args.length == 2) {
                    if (sender instanceof Player) {
                        for (NPC npc : npcs) {
                            if (npc.getId().equalsIgnoreCase(args[1])) {
                                npc.sendAnimatonPacket(0);
                                NPCEditGUI gui = new NPCEditGUI((Player) sender, npc);
                                return true;
                            }
                        }
                        sender.sendMessage(ChatColor.RED + "NPC not found!");
                    }

                }
                else if (args[0].equalsIgnoreCase("reload")) {
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
                    sender.sendMessage(ChatColor.YELLOW + "You have to relog to use the command system");
                }
                else {
                    sender.sendMessage(ChatColor.RED + "Invalid usage: /npc create/delete/edit/reload {name}");
                }
            }
        }
        return true;
    }

    public static void sendSuccesMessage(CommandSender sender) {
        sender.sendMessage(ChatColor.GREEN + "Success");
    }
}
