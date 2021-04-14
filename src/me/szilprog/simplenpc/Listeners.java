package me.szilprog.simplenpc;

import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import net.minecraft.server.v1_16_R3.PacketPlayInUseEntity;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.IOException;
import java.lang.reflect.Field;


public class Listeners implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        for (NPC npc : Main.instance.npcs) {
            if (npc.getLocation().getWorld().toString().equals(e.getPlayer().getLocation().getWorld().toString()))
                npc.addNPCPacket(e.getPlayer());
        }
    }

    @EventHandler
    public void onjoin(PlayerJoinEvent event) {
        injectPlayer(event.getPlayer());
    }

    @EventHandler
    public void onleave(PlayerQuitEvent event) {
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
                for (NPC npc : Main.instance.npcs) {
                    npc.removeNPCPacket(p);
                }
            }
            Main.instance.npcs.clear();
            try {
                ConfigManager.loadMainConfig();
            } catch (IOException e) {
                e.printStackTrace();
            }
            for (Player p : Bukkit.getOnlinePlayers()) {
                for (NPC npc : Main.instance.npcs) {
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
            }.runTask(Main.instance);
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

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        for (NPC npc : Main.instance.npcs) {
            npc.removeNPCPacket(event.getPlayer());
            if (npc.getLocation().getWorld().toString().equals(event.getPlayer().getLocation().getWorld().toString()))
                npc.addNPCPacket(event.getPlayer());
        }
    }

    private void injectPlayer(Player player) {
        ChannelDuplexHandler channelDuplexHandler = new ChannelDuplexHandler() {

            @Override
            public void channelRead(ChannelHandlerContext channelHandlerContext, Object packet) throws Exception {
                if (packet instanceof PacketPlayInUseEntity) {
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
