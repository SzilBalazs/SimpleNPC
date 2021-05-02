package me.szilprog.simplenpc.listeners;

import me.szilprog.simplenpc.SimpleNPC;
import me.szilprog.simplenpc.gui.NPCEditGUI;
import me.szilprog.simplenpc.npc.NPC;
import me.szilprog.simplenpc.utils.ConfigManager;
import me.szilprog.simplenpc.utils.WaitingMessageType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.IOException;

public class InventoryListener implements Listener {

	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) throws IOException {

		if (NPCEditGUI.playerData.get(event.getWhoClicked().getUniqueId()) == null) return;
		if (!event.getInventory().equals(NPCEditGUI.playerData.get(event.getWhoClicked().getUniqueId()).getInventory()))
			return;
		if (event.getCurrentItem() == null) return;
		if (event.getCurrentItem().getItemMeta().getDisplayName().equalsIgnoreCase(ChatColor.RED + "NPC Display Name")) {
			event.getWhoClicked().closeInventory();
			NPCEditGUI gui = NPCEditGUI.playerData.get((event.getWhoClicked().getUniqueId()));
			gui.waitingMessage = WaitingMessageType.DISPLAY_NAME;
			NPCEditGUI.playerData.replace(event.getWhoClicked().getUniqueId(), gui);
		} else if (event.getCurrentItem().getItemMeta().getDisplayName().equalsIgnoreCase(ChatColor.RED + "NPC Skin Name")) {
			event.getWhoClicked().closeInventory();
			NPCEditGUI gui = NPCEditGUI.playerData.get((event.getWhoClicked().getUniqueId()));
			gui.waitingMessage = WaitingMessageType.SKIN_NAME;
			NPCEditGUI.playerData.replace(event.getWhoClicked().getUniqueId(), gui);
		} else if (event.getCurrentItem().getItemMeta().getDisplayName().equalsIgnoreCase(ChatColor.RED + "NPC Command")) {
			event.getWhoClicked().closeInventory();
			NPCEditGUI gui = NPCEditGUI.playerData.get((event.getWhoClicked().getUniqueId()));
			gui.waitingMessage = WaitingMessageType.COMMAND;
			NPCEditGUI.playerData.replace(event.getWhoClicked().getUniqueId(), gui);
		} else if (event.getCurrentItem().getItemMeta().getDisplayName().equalsIgnoreCase(ChatColor.RED + "NPC Cooldown")) {
			event.getWhoClicked().closeInventory();
			NPCEditGUI gui = NPCEditGUI.playerData.get((event.getWhoClicked().getUniqueId()));
			gui.waitingMessage = WaitingMessageType.COOLDOWN;
			NPCEditGUI.playerData.replace(event.getWhoClicked().getUniqueId(), gui);
		} else if (event.getCurrentItem().getItemMeta().getDisplayName().equalsIgnoreCase(ChatColor.RED + "NPC Permission")) {
			if (event.isRightClick()) {
				NPCEditGUI gui = NPCEditGUI.playerData.get((event.getWhoClicked().getUniqueId()));
				gui.config.set("npc.permission.permission", "Disabled");
				try {
					gui.config.save(ConfigManager.getConfigFile(gui.npc.getId()));
				} catch (IOException e) {

				}
				NPCEditGUI.playerData.replace(event.getWhoClicked().getUniqueId(), gui);
				gui.openGUI();
				event.setCancelled(true);
				return;
			}
			event.getWhoClicked().closeInventory();
			NPCEditGUI gui = NPCEditGUI.playerData.get((event.getWhoClicked().getUniqueId()));
			gui.waitingMessage = WaitingMessageType.PERMISSION;
			NPCEditGUI.playerData.replace(event.getWhoClicked().getUniqueId(), gui);
		} else if (event.getCurrentItem().getItemMeta().getDisplayName().equalsIgnoreCase(ChatColor.RED + "NPC Permission Message")) {
			event.getWhoClicked().closeInventory();
			NPCEditGUI gui = NPCEditGUI.playerData.get((event.getWhoClicked().getUniqueId()));
			gui.waitingMessage = WaitingMessageType.PMESSAGE;
			NPCEditGUI.playerData.replace(event.getWhoClicked().getUniqueId(), gui);
		} else if (event.getCurrentItem().getItemMeta().getDisplayName().equalsIgnoreCase(ChatColor.RED + "NPC Look Player")) {
			NPCEditGUI gui = NPCEditGUI.playerData.get((event.getWhoClicked().getUniqueId()));
			gui.config.set("npc.look.lookPlayer", !gui.config.getBoolean("npc.look.lookPlayer"));
			try {
				gui.config.save(ConfigManager.getConfigFile(gui.npc.getId()));
			} catch (IOException e) {

			}
			NPCEditGUI.playerData.replace(event.getWhoClicked().getUniqueId(), gui);
			gui.openGUI();
			event.setCancelled(true);
			return;
		} else if (event.getCurrentItem().getItemMeta().getDisplayName().equalsIgnoreCase(ChatColor.RED + "Reload NPCs")) {
			event.setCancelled(true);
			NPCEditGUI gui = NPCEditGUI.playerData.get((event.getWhoClicked().getUniqueId()));

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
			return;
		} else if (event.getCurrentItem().getItemMeta().getDisplayName().equalsIgnoreCase(ChatColor.RED + "Delete NPC")) {
			event.getWhoClicked().closeInventory();
			new BukkitRunnable() {
				@Override
				public void run() {
					Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "npc delete {0}".replace("{0}", NPCEditGUI.playerData.get((event.getWhoClicked().getUniqueId())).npc.getId()));
					Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "npc reload");
				}
			}.runTask(SimpleNPC.getInstance());
			event.setCancelled(true);
			return;
		} else if (event.getCurrentItem().getItemMeta().getDisplayName().equalsIgnoreCase(ChatColor.RED + "Teleport here NPC")) {
			NPCEditGUI gui = NPCEditGUI.playerData.get((event.getWhoClicked().getUniqueId()));
			FileConfiguration config = ConfigManager.getNPCConfig(gui.npc.getId());
			Location loc = event.getWhoClicked().getLocation();
			config.set("npc.loc.x", loc.getX());
			config.set("npc.loc.y", loc.getY());
			config.set("npc.loc.z", loc.getZ());
			config.set("npc.loc.yaw", loc.getYaw());
			config.set("npc.loc.pitch", loc.getPitch());
			config.set("npc.loc.world", Bukkit.getWorlds().indexOf(loc.getWorld()));
			config.save(ConfigManager.getConfigFile(gui.npc.getId()));
			event.setCancelled(true);
			return;
		}
		event.getWhoClicked().sendMessage(ChatColor.GREEN + "Enter the value in chat! (Enter cancel if you want to cancel it.)");
		event.setCancelled(true);

	}

}
