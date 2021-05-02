package me.szilprog.simplenpc.listeners;

import me.szilprog.simplenpc.SimpleNPC;
import me.szilprog.simplenpc.npc.NPC;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;

public class WorldListener implements Listener {

	@EventHandler
	public void onWorldChange(PlayerChangedWorldEvent event) {
		for (NPC npc : SimpleNPC.getInstance().getNpcs()) {
			npc.removeNPCPacket(event.getPlayer());
			if (npc.getLocation().getWorld().toString().equals(event.getPlayer().getLocation().getWorld().toString()))
				npc.addNPCPacket(event.getPlayer());
		}
	}

}
