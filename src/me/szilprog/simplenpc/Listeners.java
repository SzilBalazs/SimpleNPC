package me.szilprog.simplenpc;

import io.netty.channel.*;
import net.minecraft.server.v1_16_R3.PacketPlayInUseEntity;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.lang.reflect.Field;


public class Listeners implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        for (NPC npc : Main.instance.npcs) {
            npc.addNPCPacket(e.getPlayer());
        }
    }

    @EventHandler
    public void onjoin(PlayerJoinEvent event){
        injectPlayer(event.getPlayer());
    }

    @EventHandler
    public void onleave(PlayerQuitEvent event){
        removePlayer(event.getPlayer());
    }
    private void removePlayer(Player player) {
        Channel channel = ((CraftPlayer) player).getHandle().playerConnection.networkManager.channel;
        channel.eventLoop().submit(() -> {
            channel.pipeline().remove(player.getName());
            return null;
        });
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {

        if (!event.getInventory().equals(NPCEditGUI.inv)) return;
        if (event.getCurrentItem().getItemMeta().getDisplayName().equalsIgnoreCase(ChatColor.RED + "NPC Display Name")) {
            event.getWhoClicked().closeInventory();
            NPCEditGUI gui=NPCEditGUI.playerData.get((event.getWhoClicked().getUniqueId()));
            gui.waitingMessage = WaitingMessageType.DISPLAY_NAME;
            NPCEditGUI.playerData.replace(event.getWhoClicked().getUniqueId(), gui);
        }
        else if (event.getCurrentItem().getItemMeta().getDisplayName().equalsIgnoreCase(ChatColor.RED + "NPC Skin Name")) {
            event.getWhoClicked().closeInventory();
            NPCEditGUI gui=NPCEditGUI.playerData.get((event.getWhoClicked().getUniqueId()));
            gui.waitingMessage = WaitingMessageType.SKIN_NAME;
            NPCEditGUI.playerData.replace(event.getWhoClicked().getUniqueId(), gui);
        }
        else if (event.getCurrentItem().getItemMeta().getDisplayName().equalsIgnoreCase(ChatColor.RED + "NPC Command")) {
            event.getWhoClicked().closeInventory();
            NPCEditGUI gui=NPCEditGUI.playerData.get((event.getWhoClicked().getUniqueId()));
            gui.waitingMessage = WaitingMessageType.COMMAND;
            NPCEditGUI.playerData.replace(event.getWhoClicked().getUniqueId(), gui);
        }
        else if (event.getCurrentItem().getItemMeta().getDisplayName().equalsIgnoreCase(ChatColor.RED + "NPC Cooldown")) {
            event.getWhoClicked().closeInventory();
            NPCEditGUI gui=NPCEditGUI.playerData.get((event.getWhoClicked().getUniqueId()));
            gui.waitingMessage = WaitingMessageType.COOLDOWN;
            NPCEditGUI.playerData.replace(event.getWhoClicked().getUniqueId(), gui);
        }
        event.getWhoClicked().sendMessage(ChatColor.GREEN + "Enter the value in chat! (Enter cancel if you want to cancel it.)");
        event.setCancelled(true);

    }

    @EventHandler
    public void chatEvent(AsyncPlayerChatEvent event) {
        NPCEditGUI gui=NPCEditGUI.playerData.get((event.getPlayer().getUniqueId()));
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

    private void injectPlayer(Player player) {
        ChannelDuplexHandler channelDuplexHandler = new ChannelDuplexHandler() {

            @Override
            public void channelRead(ChannelHandlerContext channelHandlerContext, Object packet) throws Exception {
                if(packet instanceof PacketPlayInUseEntity) {
                    packet = (PacketPlayInUseEntity) packet;
                    Field f = packet.getClass().getDeclaredField("a");
                    f.setAccessible(true);
                    for (NPC npc : Main.instance.npcs) {
                        if (npc.getEntityPlayer().getId() == f.getInt(packet)) {
                            npc.interactEvent((PacketPlayInUseEntity) packet, player);
                        }
                    }


                }
                super.channelRead(channelHandlerContext, packet);
            }


        };

        ChannelPipeline pipeline = ((CraftPlayer) player).getHandle().playerConnection.networkManager.channel.pipeline();
        pipeline.addBefore("packet_handler", player.getName(), channelDuplexHandler);

    }
}
