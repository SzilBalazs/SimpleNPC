package me.szilprog.simplenpc;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.minecraft.server.v1_16_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_16_R3.CraftServer;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class NPC {

    private EntityPlayer npc=null;
    private String name;
    private String skin;
    private Location loc;
    private String command;
    private HashMap<UUID, Integer> cooldownManager= new HashMap<UUID, Integer>();
    private int cooldown;
    public static Plugin plugin;

    public NPC(Location loc, String name, String skinUsername, String command, int cooldown) {
        this.name = name;
        this.loc = loc;
        this.skin = skinUsername;
        this.command = command;
        this.cooldown = cooldown;

        createNPC();
    }

    public void createNPC() {
        MinecraftServer nmsServer = ((CraftServer) Bukkit.getServer()).getServer();
        WorldServer nmsWorld = ((CraftWorld)Bukkit.getWorld("world")).getHandle();
        GameProfile gameProfile = new GameProfile(UUID.randomUUID(), coloredNameUtil(name));
        String[] name=null;
        try {
            name = getSkin(skin);
        }
        catch (Exception e) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "INVALID SKIN USERNAME");
        }
        if (name != null) {
            gameProfile.getProperties().put("textures", new Property("textures", name[0], name[1]));
        }
        npc = new EntityPlayer(nmsServer, nmsWorld, gameProfile, new PlayerInteractManager(nmsWorld));
        moveNPC(loc.getX(), loc.getY(), loc.getZ());
    }

    public static String coloredNameUtil(String str) {
        return ChatColor.translateAlternateColorCodes('&', str);
    }

    public void addNPCPacket(Player player) {
        if (npc == null) return;
        PlayerConnection connection = ((CraftPlayer)player).getHandle().playerConnection;
        connection.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, npc));
        connection.sendPacket(new PacketPlayOutNamedEntitySpawn(npc));
        connection.sendPacket(new PacketPlayOutEntityHeadRotation(npc, (byte) (npc.yaw * 256 / 360)));
    }

    public void removeNPCPacket(Player player) {
        PlayerConnection connection = ((CraftPlayer)player).getHandle().playerConnection;
        connection.sendPacket(new PacketPlayOutEntityDestroy(npc.getId()));
    }

    private static String[] getSkin(String name ) {
        try {
            URL url = new URL("https://api.mojang.com/users/profiles/minecraft/" + name);
            InputStreamReader reader = new InputStreamReader(url.openStream());
            String uuid = new JsonParser().parse(reader).getAsJsonObject().get("id").getAsString();
            URL url2 = new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid
                    + "?unsigned=false");
            InputStreamReader reader2 = new InputStreamReader(url2.openStream());
            JsonObject property = new JsonParser().parse(reader2).getAsJsonObject().get("properties").getAsJsonArray().get(0).getAsJsonObject();
            String texture = property.get("value").getAsString();
            String signature = property.get("signature").getAsString();
            return new String[] {texture, signature};
        }
        catch (Exception e) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "INVALID SKIN USERNAME");
            return null;
        }
    }

    public void moveNPC (double x, double y, double z) {
        npc.setLocation(x, y, z, loc.getYaw(), loc.getPitch());
        loc = new Location(loc.getWorld(), x, y, z, loc.getYaw(), loc.getPitch());
        for (Player p : Bukkit.getOnlinePlayers()) {

            ((CraftPlayer)p).getHandle().playerConnection.sendPacket(new PacketPlayOutEntityTeleport(npc));
        }
    }

    public void sendAnimatonPacket(int animation) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            PlayerConnection connection = ((CraftPlayer) p).getHandle().playerConnection;

            connection.sendPacket(new PacketPlayOutAnimation(npc, animation));
        }
    }

    public void interactEvent(PacketPlayInUseEntity packet, Player player) throws ExecutionException, InterruptedException {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (cooldownManager.containsKey(player.getUniqueId())) {
                    if (cooldownManager.get(player.getUniqueId()) > 0) {
                        return;
                    }
                    else {
                        cooldownManager.replace(player.getUniqueId(), cooldown);
                    }
                }
                else {
                    cooldownManager.put(player.getUniqueId(), cooldown);
                }
                String formatedCmd = command.replace("{PlayerName}", player.getName());
                Bukkit.dispatchCommand( Bukkit.getConsoleSender(), formatedCmd);

            }
        }.runTask(Main.instance);

    }

    public EntityPlayer getEntityPlayer() {
        return npc;
    }

    public void cooldownUpdate() {
        for (UUID uuid : cooldownManager.keySet()) {
            if (cooldownManager.get(uuid) > 0) {
                cooldownManager.replace(uuid, cooldownManager.get(uuid)-1);
            }
        }
    }
}