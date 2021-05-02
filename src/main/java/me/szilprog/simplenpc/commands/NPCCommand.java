package me.szilprog.simplenpc.commands;

import me.szilprog.simplenpc.utils.ConfigManager;
import me.szilprog.simplenpc.npc.NPC;
import me.szilprog.simplenpc.gui.NPCEditGUI;
import me.szilprog.simplenpc.SimpleNPC;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.IOException;

public class NPCCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
		if (args.length == 0)
			sender.sendMessage(ChatColor.RED + "Invalid usage: /npc create/delete/edit/reload {name}");
		else if (sender.hasPermission("npc.admin")) {

			if (args[0].equalsIgnoreCase("create") && args.length == 2) {
				try {
					if (sender instanceof Player) {
						ConfigManager.createNPC(args[1].toLowerCase(), ((Player) sender).getLocation());
					} else {
						ConfigManager.createNPC(args[1].toLowerCase());
					}
					sendSuccesMessage(sender);

				} catch (IOException e) {
					SimpleNPC.getInstance().getLogger().warning("Error Found");
				}
			} else if (args[0].equalsIgnoreCase("delete") && args.length == 2) {
				ConfigManager.deleteNPC(args[1].toLowerCase());
				sendSuccesMessage(sender);
			} else if (args[0].equalsIgnoreCase("edit") && args.length == 2) {
				if (sender instanceof Player) {
					for (NPC npc : SimpleNPC.getInstance().getNpcs()) {
						if (npc.getId().equalsIgnoreCase(args[1])) {
							npc.sendAnimatonPacket(0);
							NPCEditGUI gui = new NPCEditGUI((Player) sender, npc);
							return true;
						}
					}
					sender.sendMessage(ChatColor.RED + "NPC not found!");
				}

			} else if (args[0].equalsIgnoreCase("reload")) {
				for (Player p : Bukkit.getOnlinePlayers()) {
					for (NPC npc : SimpleNPC.getInstance().getNpcs()) {
						npc.removeNPCPacket(p);
					}
				}
				SimpleNPC.getInstance().getNpcs().clear();
				try {
					ConfigManager.loadMainConfig();
				} catch (IOException e) {
					e.printStackTrace();
				}
				for (Player p : Bukkit.getOnlinePlayers()) {
					for (NPC npc : SimpleNPC.getInstance().getNpcs()) {
						if (npc.getLocation().getWorld().toString().equals(p.getLocation().getWorld().toString()))
							npc.addNPCPacket(p);
					}
				}
				Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "Simple NPC Reloaded!");
			} else {
				sender.sendMessage(ChatColor.RED + "Invalid usage: /npc create/delete/edit/reload {name}");
			}
		}
		return true;
	}

	public static void sendSuccesMessage(CommandSender sender) {
		sender.sendMessage(ChatColor.GREEN + "Success");
	}
}