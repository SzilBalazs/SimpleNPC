package me.szilprog.simplenpc;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;

public class NPCEditGUI implements Listener {
    public static Inventory inv;
    public NPC npc;
    public Player player;
    public static FileConfiguration config;
    public static HashMap<UUID, NPCEditGUI> playerData = new HashMap<UUID, NPCEditGUI>();
    public WaitingMessageType waitingMessage=WaitingMessageType.NONE;

    public NPCEditGUI(Player player, NPC npc) {
        this.npc = npc;
        this.player = player;
        if (!playerData.containsKey(player.getUniqueId())) playerData.put(player.getUniqueId(), this);
        else playerData.replace(player.getUniqueId(), this);
        if (config == null) config = ConfigManager.getNPCConfig(npc.getId());
        if (inv == null) createInventory();
        player.openInventory(inv);

    }

    public NPCEditGUI() {
        // LISTENER CONSTRUCTOR
    }

    public Inventory getInventory() { return inv; }

    public static ItemStack getItem(int index) {
        ItemStack itemStack=null;
        switch (index) {
            case 0:
                itemStack = new ItemStack(Material.PAPER);
                ItemMeta meta = itemStack.getItemMeta();
                meta.setDisplayName(ChatColor.RED + "NPC Display Name");
                meta.setLore(Arrays.asList(ChatColor.GRAY + "Click here to edit the NPC's display name!", ChatColor.GRAY + "Display name of your NPC", ChatColor.RED + "Maximum 16 characters!", ChatColor.GREEN+"Current is " + config.getString("npc.name")));
                itemStack.setItemMeta(meta);
                break;
            case 1:
                itemStack = new ItemStack(Material.PAPER);
                meta = itemStack.getItemMeta();
                meta.setDisplayName(ChatColor.RED + "NPC Skin Name");
                meta.setLore(Arrays.asList(ChatColor.GRAY + "Click here to edit the NPC's skin name!", ChatColor.GRAY + "The skin's owner.", ChatColor.GREEN+"Current is " + config.getString("npc.skinname")));
                itemStack.setItemMeta(meta);
                break;
            case 2:
                itemStack = new ItemStack(Material.PAPER);
                meta = itemStack.getItemMeta();
                meta.setDisplayName(ChatColor.RED + "NPC Command");
                meta.setLore(Arrays.asList(ChatColor.GRAY + "Click here to edit the NPC's command!", ChatColor.GRAY + "The command that the NPC runs.", ChatColor.GREEN+"Current is " + config.getString("npc.command")));
                itemStack.setItemMeta(meta);
                break;
            case 3:
                itemStack = new ItemStack(Material.PAPER);
                meta = itemStack.getItemMeta();
                meta.setDisplayName(ChatColor.RED + "NPC Cooldown");
                meta.setLore(Arrays.asList(ChatColor.GRAY + "Click here to edit the NPC's cooldown!", ChatColor.GRAY + "How many seconds the player have to", ChatColor.GRAY + "wait until the running the command again.", ChatColor.GRAY + "At least 1 is recommended to prevent spamming.", ChatColor.GREEN + "Current is " + config.getString("npc.cooldown")));
                itemStack.setItemMeta(meta);
                break;
        }
        return itemStack;
    }

    public void createInventory() {
        inv=Bukkit.createInventory(null, 9, ChatColor.DARK_GREEN + "Editing " + npc.getId());
        for (int i=0;i<9;i++) {
            ItemStack itemStack = getItem(i);
            if (itemStack != null) {
                inv.setItem(i, itemStack);
            }
        }
    }

    public void openGUI() {
        new BukkitRunnable() {
            @Override
            public void run() {
                player.openInventory(inv);
            }
        }.runTask(Main.instance);

    }

    public void messageEvent(String message) {

        new BukkitRunnable() {
            @Override
            public void run() {

                config = ConfigManager.getNPCConfig(npc.getId());
                switch (waitingMessage) {
                    case DISPLAY_NAME:
                        config.set("npc.name", message);
                        break;
                    case SKIN_NAME:
                        config.set("npc.skinname", message);
                        break;
                    case COMMAND:
                        config.set("npc.command", message);
                        break;
                    case COOLDOWN:
                        try {
                            config.set("npc.cooldown", String.valueOf(message));
                        }
                        catch (Exception e) {
                            player.sendMessage(ChatColor.RED + "Please enter a valid number!");
                        }
                        break;

                }
                createInventory();
                player.openInventory(inv);
                try {
                    config.save(ConfigManager.getConfigFile(npc.getId()));
                } catch (IOException e) {

                }
                waitingMessage=WaitingMessageType.NONE;
            }
        }.runTask(Main.instance);

    }
}
