package me.szilprog.simplenpc.utils;

import me.szilprog.simplenpc.npc.NPC;
import me.szilprog.simplenpc.SimpleNPC;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

public class ConfigManager {
    private static FileConfiguration configuration = null;
    private static File configFile = null;

    public static void loadMainConfig() throws IOException {
        File skindata = new File(SimpleNPC.getInstance().getDataFolder(), "skindata");
        if (!skindata.exists()) skindata.mkdirs();
        if (configFile == null) {
            configFile = new File(SimpleNPC.getInstance().getDataFolder(), "npc.yml");
        }
        configuration = YamlConfiguration.loadConfiguration(configFile);
        configuration.addDefault("npc.names", Arrays.asList());
        configuration.addDefault("update-check", true);
        configuration.options().copyDefaults(true);
        configuration.save(configFile);
        List<String> names = configuration.getStringList("npc.names");
        loadNPCConfig(names);
        if (configuration.getBoolean("update-check"))
            checkForUpdates();
    }

    public static void loadNPCConfig(List<String> names) throws IOException {
        for (String name : names) {
            name = name.toLowerCase();
            loadNPCConfig(name);
        }
    }

    public static void loadNPCConfig(String name) throws IOException {
        name = name.toLowerCase();
        SimpleNPC.getInstance().getLogger().info("Loading {name} npc.".replace("{name}", name));
        File npcdata = new File(SimpleNPC.getInstance().getDataFolder(), name + ".yml");
        FileConfiguration c = YamlConfiguration.loadConfiguration(npcdata);
        c.addDefault("npc.name", "&3Simple&2&lNPC");
        c.addDefault("npc.skinname", "Notch");
        c.addDefault("npc.loc.x", 0);
        c.addDefault("npc.loc.y", 0);
        c.addDefault("npc.loc.z", 0);
        c.addDefault("npc.loc.yaw", 0);
        c.addDefault("npc.loc.pitch", 0);
        c.addDefault("npc.loc.world", 0);
        c.addDefault("npc.command", "say hi {PlayerName}");
        c.addDefault("npc.cooldown", 3);
        c.addDefault("npc.permission.use.permission", "Disabled");
        c.addDefault("npc.permission.use.message", "&cYou don't have the permission to use this NPC!");
        c.addDefault("npc.permission.see.permission", "Disabled");
        c.addDefault("npc.look.lookPlayer", true);
        c.addDefault("npc.look.radius", -1);
        c.options().copyDefaults(true);
        c.save(npcdata);
        SimpleNPC.getInstance().getNpcs().add(new NPC(new Location(Bukkit.getWorlds().get(c.getInt("npc.loc.world")), c.getInt("npc.loc.x"), c.getInt("npc.loc.y"), c.getInt("npc.loc.z"), c.getInt("npc.loc.yaw"), c.getInt("npc.loc.pitch")), c.getString("npc.name"), c.getString("npc.skinname"), c.getString("npc.command"), c.getInt("npc.cooldown"), new PermissionData(c.getString("npc.permission.use.permission"), c.getString("npc.permission.use.message"), c.getString("npc.permission.see.permission")), c.getBoolean("npc.look.lookPlayer"), c.getInt("npc.look.radius"), name));
    }

    public static void loadNPCConfig(String name, Location location) throws IOException {
        SimpleNPC.getInstance().getLogger().info("Loading {name} npc.".replace("{name}", name));
        File npcdata = new File(SimpleNPC.getInstance().getDataFolder(), name + ".yml");
        FileConfiguration c = YamlConfiguration.loadConfiguration(npcdata);
        c.addDefault("npc.name", "&3Simple&2&lNPC");
        c.addDefault("npc.skinname", "Notch");
        c.addDefault("npc.loc.x", location.getX());
        c.addDefault("npc.loc.y", location.getY());
        c.addDefault("npc.loc.z", location.getZ());
        c.addDefault("npc.loc.yaw", location.getYaw());
        c.addDefault("npc.loc.pitch", location.getPitch());
        c.addDefault("npc.loc.world", Bukkit.getWorlds().indexOf(location.getWorld()));
        c.addDefault("npc.command", "say hi {PlayerName}");
        c.addDefault("npc.cooldown", 3);
        c.addDefault("npc.permission.use.permission", "Disabled");
        c.addDefault("npc.permission.use.message", "&cYou don't have the permission to use this NPC!");
        c.addDefault("npc.permission.see.permission", "Disabled");
        c.addDefault("npc.look.lookPlayer", true);
        c.addDefault("npc.look.radius", -1);
        c.options().copyDefaults(true);
        c.save(npcdata);
        SimpleNPC.getInstance().getNpcs().add(new NPC(new Location(Bukkit.getWorlds().get(c.getInt("npc.loc.world")), c.getInt("npc.loc.x"), c.getInt("npc.loc.y"), c.getInt("npc.loc.z"), c.getInt("npc.loc.yaw"), c.getInt("npc.loc.pitch")), c.getString("npc.name"), c.getString("npc.skinname"), c.getString("npc.command"), c.getInt("npc.cooldown"), new PermissionData(c.getString("npc.permission.use.permission"), c.getString("npc.permission.use.message"), c.getString("npc.permission.see.permission")), c.getBoolean("npc.look.lookPlayer"), c.getInt("npc.look.radius"), name));
    }

    public static FileConfiguration getNPCConfig(String name) {
        File npcdata = new File(SimpleNPC.getInstance().getDataFolder(), name + ".yml");
        FileConfiguration c = YamlConfiguration.loadConfiguration(npcdata);
        return c;
    }

    public static File getConfigFile(String name) {
        return new File(SimpleNPC.getInstance().getDataFolder(), name + ".yml");
    }

    public static void checkForUpdates() throws IOException {
        URL url = new URL("https://raw.githubusercontent.com/SzilBalazs/SimpleNPC/master/update_check.txt");
        InputStream inputStream = url.openStream();
        String file = "";
        int content;
        Integer versionNumber = null;
        while ((content = inputStream.read()) != -1) {
            file += (char) content;
        }
        try {
            versionNumber = Integer.parseInt(file);
        } catch (Exception e) {
            Bukkit.getConsoleSender().sendMessage("[Simple NPC] Invalid update check!");
        }
        if (versionNumber != null) {
            Bukkit.getLogger().info("Version number: {0}".replace("{0}", String.valueOf(SimpleNPC.VERSION)));
            if (SimpleNPC.VERSION < versionNumber) {
                Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[Simple NPC] OUT-DATED!");
                SimpleNPC.getInstance().getLogger().info("Please visit https://www.spigotmc.org/resources/simple-npc-easily-create-clickable-npcs-permissions.90893/ to download the latest version.");
            } else {
                Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "[Simple NPC] UP-TO-DATE!");
            }
        }
    }

    public static void createNPC(String name) throws IOException {
        loadNPCConfig(name);
        List<String> list = configuration.getStringList("npc.names");
        list.add(name);
        configuration.set("npc.names", list);
        configuration.save(configFile);
    }

    public static void createNPC(String name, Location location) throws IOException {
        loadNPCConfig(name, location);
        List<String> list = configuration.getStringList("npc.names");
        list.add(name);
        configuration.set("npc.names", list);
        configuration.save(configFile);
    }

    public static void deleteNPC(String name) {
        SimpleNPC.getInstance().getLogger().info("Deleting {name} npc.".replace("{name}", name));
        File npcdata = new File(SimpleNPC.getInstance().getDataFolder(), name + ".yml");

        try {
            List<String> list = configuration.getStringList("npc.names");
            list.remove(name);
            configuration.set("npc.names", list);
            configuration.save(configFile);
        } catch (Exception e) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "NPC Name not found!");
        }
        npcdata.delete();
    }


}
