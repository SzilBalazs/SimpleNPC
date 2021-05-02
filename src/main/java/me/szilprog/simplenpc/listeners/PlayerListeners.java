package me.szilprog.simplenpc.listeners;

import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import me.szilprog.simplenpc.SimpleNPC;
import me.szilprog.simplenpc.npc.NPC;
import net.minecraft.server.v1_16_R3.PacketPlayInUseEntity;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.lang.reflect.Field;

public class PlayerListeners implements Listener {

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		for (NPC npc : SimpleNPC.getInstance().getNpcs()) {
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

	private void injectPlayer(Player player) {
		ChannelDuplexHandler channelDuplexHandler = new ChannelDuplexHandler() {

			@Override
			public void channelRead(ChannelHandlerContext channelHandlerContext, Object packet) throws Exception {
				if (packet instanceof PacketPlayInUseEntity) {
					packet = (PacketPlayInUseEntity) packet;
					Field f = packet.getClass().getDeclaredField("a");
					f.setAccessible(true);
					for (NPC npc : SimpleNPC.getInstance().getNpcs()) {
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