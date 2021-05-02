package me.szilprog.simplenpc.listeners;

import me.szilprog.simplenpc.gui.NPCEditGUI;
import me.szilprog.simplenpc.utils.WaitingMessageType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatListener implements Listener {

	@EventHandler
	public void chatEvent(AsyncPlayerChatEvent event) {
		NPCEditGUI gui = NPCEditGUI.playerData.get((event.getPlayer().getUniqueId()));
		if (gui == null) return;
		if (gui.waitingMessage == WaitingMessageType.NONE) return;
		if (event.getMessage().equalsIgnoreCase("cancel")) {
			gui.waitingMessage = WaitingMessageType.NONE;
			NPCEditGUI.playerData.replace(event.getPlayer().getUniqueId(), gui);
			gui.openGUI();
			event.setCancelled(true);
			return;
		}
		gui.messageEvent(event.getMessage());
		event.setCancelled(true);
	}

}
