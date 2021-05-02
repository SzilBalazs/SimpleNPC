package me.szilprog.simplenpc.gui;

import me.szilprog.simplenpc.npc.NPC;
import me.szilprog.simplenpc.SimpleNPC;
import me.szilprog.simplenpc.utils.ConfigManager;
import me.szilprog.simplenpc.utils.WaitingMessageType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;

public class  NPCEditGUI implements Listener {
    public Inventory inv;
    public NPC npc;
    public Player player;
    public FileConfiguration config;
    public static HashMap<UUID, NPCEditGUI> playerData = new HashMap<UUID, NPCEditGUI>();
    public WaitingMessageType waitingMessage = WaitingMessageType.NONE;

    public NPCEditGUI(Player player, NPC npc) {
        this.npc = npc;
        this.player = player;
        if (!playerData.containsKey(player.getUniqueId())) playerData.put(player.getUniqueId(), this);
        else playerData.replace(player.getUniqueId(), this);
        config = ConfigManager.getNPCConfig(npc.getId());
        createInventory();
        player.openInventory(inv);

    }

    public Inventory getInventory() {
        return inv;
    }

    public ItemStack getItem(int index) {
        ItemStack itemStack = null;
        switch (index) {
            case 0:
                itemStack = new ItemStack(Material.PAPER);
                ItemMeta meta = itemStack.getItemMeta();
                meta.setDisplayName(ChatColor.RED + "NPC Display Name");
                meta.setLore(Arrays.asList(ChatColor.GRAY + "Click here to edit the NPC's display name!", ChatColor.GRAY + "Display name of your NPC", ChatColor.RED + "Maximum 16 characters!", ChatColor.GREEN + "Current is " + NPC.coloredNameUtil(config.getString("npc.name"))));
                itemStack.setItemMeta(meta);
                break;
            case 1:
                itemStack = new ItemStack(Material.PAPER);
                meta = itemStack.getItemMeta();
                meta.setDisplayName(ChatColor.RED + "NPC Skin Name");
                meta.setLore(Arrays.asList(ChatColor.GRAY + "Click here to edit the NPC's skin name!", ChatColor.GRAY + "The skin's owner.", ChatColor.GREEN + "Current is " + config.getString("npc.skinname")));
                itemStack.setItemMeta(meta);
                break;
            case 2:
                itemStack = new ItemStack(Material.PAPER);
                meta = itemStack.getItemMeta();
                meta.setDisplayName(ChatColor.RED + "NPC Command");
                meta.setLore(Arrays.asList(ChatColor.GRAY + "Click here to edit the NPC's command!", ChatColor.GRAY + "The command that the NPC runs.", ChatColor.GREEN + "Current is " + config.getString("npc.command")));
                itemStack.setItemMeta(meta);
                break;
            case 3:
                itemStack = new ItemStack(Material.PAPER);
                meta = itemStack.getItemMeta();
                meta.setDisplayName(ChatColor.RED + "NPC Cooldown");
                meta.setLore(Arrays.asList(ChatColor.GRAY + "Click here to edit the NPC's cooldown!", ChatColor.GRAY + "How many seconds the player have to", ChatColor.GRAY + "wait until the running the command again.", ChatColor.GRAY + "At least 1 is recommended to prevent spamming.", ChatColor.GREEN + "Current is " + config.getString("npc.cooldown")));
                itemStack.setItemMeta(meta);
                break;
            case 4:
                itemStack = new ItemStack(Material.PAPER);
                meta = itemStack.getItemMeta();
                meta.setDisplayName(ChatColor.RED + "NPC Permission");
                meta.setLore(Arrays.asList(ChatColor.GRAY + "Click here to edit the NPC's permission!", ChatColor.GRAY + "Right click to " + ChatColor.RED + "disable " + ChatColor.GRAY + "this feature", ChatColor.GRAY + "Permission needed to use the NPC.", ChatColor.GREEN + "Current is " + config.getString("npc.permission.use.permission")));
                itemStack.setItemMeta(meta);
                break;
            case 5:
                itemStack = new ItemStack(Material.PAPER);
                meta = itemStack.getItemMeta();
                meta.setDisplayName(ChatColor.RED + "NPC Permission Message");
                meta.setLore(Arrays.asList(ChatColor.GRAY + "Click here to edit the NPC's permission message!", ChatColor.GRAY + "The permission message.", ChatColor.GREEN + "Current is " + NPC.coloredNameUtil(config.getString("npc.permission.use.message"))));
                itemStack.setItemMeta(meta);
                break;
            case 6:
                itemStack = new ItemStack(Material.PAPER);
                if (config.getBoolean("npc.look.lookPlayer")) {
                    itemStack.addUnsafeEnchantment(Enchantment.DURABILITY, 1);
                }
                meta = itemStack.getItemMeta();
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

                meta.setDisplayName(ChatColor.RED + "NPC Look Player");
                meta.setLore(Arrays.asList(ChatColor.GRAY + "Click here to set the NPC's look player value to " + !config.getBoolean("npc.look.lookPlayer") + "!", ChatColor.GRAY + "This will make the NPC always look the player."));
                itemStack.setItemMeta(meta);
                break;
        }
        return itemStack;
    }

    public void createInventory() {
        inv = Bukkit.createInventory(null, 27, ChatColor.DARK_GREEN + "Editing " + npc.getId());
        for (int i = 10; i < 19; i++) {
            ItemStack itemStack = getItem(i - 10);
            if (itemStack != null) {
                inv.setItem(i, itemStack);
            }
        }
        ItemStack itemStack = new ItemStack(Material.BLUE_CONCRETE);
        ItemMeta meta = itemStack.getItemMeta();
        meta.setDisplayName(ChatColor.RED + "Teleport here NPC");
        meta.setLore(Arrays.asList(ChatColor.GRAY + "Click here to teleport here this NPC!"));
        itemStack.setItemMeta(meta);
        inv.setItem(24, itemStack);
        itemStack = new ItemStack(Material.RED_CONCRETE);
        meta = itemStack.getItemMeta();
        meta.setDisplayName(ChatColor.RED + "Delete NPC");
        meta.setLore(Arrays.asList(ChatColor.GRAY + "Click here to delete this NPC!"));
        itemStack.setItemMeta(meta);
        inv.setItem(25, itemStack);
        itemStack = new ItemStack(Material.GREEN_CONCRETE);
        meta = itemStack.getItemMeta();
        meta.setDisplayName(ChatColor.RED + "Reload NPCs");
        meta.setLore(Arrays.asList(ChatColor.GRAY + "Click here to reload NPCs!"));
        itemStack.setItemMeta(meta);
        inv.setItem(26, itemStack);
    }

    public void openGUI() {
        new BukkitRunnable() {
            @Override
            public void run() {
                createInventory();
                player.openInventory(inv);
            }
        }.runTask(SimpleNPC.getInstance());

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
                            config.set("npc.cooldown", Integer.parseInt(message));
                        } catch (Exception e) {
                            player.sendMessage(ChatColor.RED + "Please enter a valid number!");
                        }
                        break;
                    case PERMISSION:
                        config.set("npc.permission.use.permission", message);
                        break;
                    case PMESSAGE:
                        config.set("npc.permission.use.message", message);
                        break;

                }
                openGUI();

                try {
                    config.save(ConfigManager.getConfigFile(npc.getId()));
                } catch (IOException e) {

                }
                waitingMessage = WaitingMessageType.NONE;
            }
        }.runTask(SimpleNPC.getInstance());

    }
}
