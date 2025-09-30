package com.mrbysco.durabilitynotifier.handler;

import com.mrbysco.durabilitynotifier.DurabilityNotifierPlugin;
import com.mrbysco.durabilitynotifier.util.CooldownUtil;
import net.kyori.adventure.key.Key;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class EventHandlers implements Listener {

	@EventHandler
	public void onDamageChange(PlayerItemDamageEvent event) {
		Player player = event.getPlayer();
		checkDurability(player, event.getItem());
	}

	public static void checkDurability(Player player, ItemStack stack) {
		double durabilityChecking = 1 - (DurabilityNotifierPlugin.percentage / 100.0);
		if (!stack.isEmpty())
			checkDurability(stack, player, durabilityChecking);
	}


	private static void checkDurability(@NotNull ItemStack stack, @NotNull Player playerIn, double checkNumber) {
		Material itemMaterial = stack.getType();
		int maxDurability = itemMaterial.getMaxDurability();
		ItemMeta itemMeta = stack.getItemMeta();
		DurabilityNotifierPlugin.LOGGER.warn("{}", stack);
		if (!stack.isEmpty() && itemMeta instanceof Damageable damageable && maxDurability != 0) {
			if (((double) damageable.getDamage() / maxDurability) > checkNumber) {
				if (DurabilityNotifierPlugin.sendMessage) {
					sendMessage(playerIn, stack);
				}

				if (DurabilityNotifierPlugin.playSound && CooldownUtil.isNotOnCooldown(stack, 500L)) {
					//This guy really wanted something special. So explosion sounds it is.
					UUID uuid = playerIn.getPlayerProfile().getId();
					if (uuid != null && uuid.equals(UUID.fromString("86121150-39f2-4063-831a-3715f2e7f397"))) { //Dcat682
						playerIn.playSound(playerIn.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, SoundCategory.PLAYERS, 1F, 1F);
					}

					playSound(playerIn);
				}
			}
		}
	}

	private static void sendMessage(@NotNull Player player, @NotNull ItemStack stack) {
		ChatColor messageColor = DurabilityNotifierPlugin.sentMessageColor;
		if (messageColor == null) {
			messageColor = ChatColor.YELLOW;
			DurabilityNotifierPlugin.LOGGER.warn("Invalid chat color found in config, please check the config");
		}

		String warningString = "Warning! §f%item%§r has dropped below §c%percent%§r durability.";
		warningString = warningString
				.replace("%item%", stack.getI18NDisplayName())
				.replace("%percent%", DurabilityNotifierPlugin.percentage + "§c%§r");
		TextComponent warning = new TextComponent(messageColor + warningString);

		player.sendMessage(ChatMessageType.ACTION_BAR, warning);
	}

	private static void playSound(@NotNull Player player) {
		Sound chosenSound = getChosenSound();
		if (chosenSound != null) {
			player.playSound(player.getLocation(), chosenSound, SoundCategory.PLAYERS, (float) DurabilityNotifierPlugin.volume, 1F);
		} else {
			DurabilityNotifierPlugin.LOGGER.warn("Could not locate the following sound: {}. Perhaps you misspelled it.", DurabilityNotifierPlugin.soundLocation);
		}
	}

	@Nullable
	private static Sound getChosenSound() {
		Key soundLocation = Key.key(DurabilityNotifierPlugin.soundLocation);
		if (soundLocation != null) {
			Sound sound = Registry.SOUNDS.get(soundLocation);
			if (sound != null) {
				return sound;
			} else {
				DurabilityNotifierPlugin.LOGGER.warn("Could not locate the following sound: {}. Perhaps you misspelled it. Falling back to default!", soundLocation);
				return Sound.BLOCK_NOTE_BLOCK_PLING;
			}
		}
		return null;
	}
}
