package com.mrbysco.durabilitynotifier;

import com.mrbysco.durabilitynotifier.handler.EventHandlers;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public final class DurabilityNotifierPlugin extends JavaPlugin {
	public static final Logger LOGGER = LoggerFactory.getLogger("DurabilityNotifier");
	public static Plugin Plugin;
	public final FileConfiguration config = getConfig();

	public static int percentage = 10;
	public static boolean checkArmor = false;

	public static boolean sendMessage = true;
	public static ChatColor sentMessageColor = ChatColor.WHITE;

	public static boolean playSound = true;
	public static String soundLocation = "minecraft:block.note_block.pling";
	public static double volume = 0.6D;


	@Override
	public void onEnable() {
		// Basic
		config.addDefault("percentage", 10);
		config.addDefault("checkArmor", false);
		// Messages
		config.addDefault("sendMessage", true);
		config.addDefault("sentMessageColor", "f");
		// Sounds
		config.addDefault("playSound", true);
		config.addDefault("soundlocation", "minecraft:block.note_block.pling");
		config.addDefault("volume", 0.6D);

		config.options().copyDefaults(true);
		saveConfig();

		// Basic
		percentage = config.getInt("percentage");
		checkArmor = config.getBoolean("checkArmor");
		// Messages
		sendMessage = config.getBoolean("sendMessage");
		sentMessageColor = ChatColor.getByChar(Objects.requireNonNull(config.getString("sentMessageColor")));
		// Sounds
		playSound = config.getBoolean("playSound");
		soundLocation = config.getString("soundlocation");
		volume = config.getDouble("volume");

		getServer().getPluginManager().registerEvents(new EventHandlers(), this);

		Plugin = this;

		Plugin.getServer().getScheduler().scheduleSyncRepeatingTask(Plugin, new Runnable() {
			@Override
			public void run() {
				if (checkArmor) {
					for (Player player : Plugin.getServer().getOnlinePlayers()) {
						PlayerInventory playerInventory = player.getInventory();
						for (ItemStack armorStack : playerInventory.getArmorContents()) {
							if (armorStack != null)
								EventHandlers.checkDurability(player, armorStack);
						}
					}
				}
			}
		}, 80L, 80L);
	}

	@Override
	public void onDisable() {
		// Plugin shutdown logic
	}
}
