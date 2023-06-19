/*
 * This file is part of Limbo.
 *
 * Copyright (C) 2022. LoohpJames <jamesloohp@gmail.com>
 * Copyright (C) 2022. Contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.loohp.limbo.commands;

import com.loohp.limbo.Console;
import com.loohp.limbo.Limbo;
import com.loohp.limbo.messages.MessagesManager;
import com.loohp.limbo.player.Player;
import com.loohp.limbo.utils.GameMode;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.md_5.bungee.api.ChatColor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DefaultCommands implements CommandExecutor, TabCompletor {
	
	@Override
	public void execute(CommandSender sender, String[] args) {
		if (args.length == 0) {
			return;
		}

		MessagesManager messages = Limbo.getInstance().getMessagesManager();

		if (args[0].equalsIgnoreCase("version")) {
			if (sender.hasPermission("limboserver.version")) {
				sender.sendMessage(messages.getMessage("command-version", Limbo.getInstance().LIMBO_IMPLEMENTATION_VERSION, Limbo.getInstance().SERVER_IMPLEMENTATION_VERSION));
			} else {
				sender.sendMessage(messages.getMessage("no-permission"));
			}
			return;
		}

		if (args[0].equalsIgnoreCase("spawn")) {
			if (sender.hasPermission("limboserver.spawn")) {
				if (args.length == 1 && sender instanceof Player) {
					Player player = (Player) sender;
					player.teleport(Limbo.getInstance().getServerProperties().getWorldSpawn());
					player.sendMessage(messages.getMessage("command-spawn"));
				} else if (args.length == 2) {
					Player player = Limbo.getInstance().getPlayer(args[1]);
					if (player != null) {
						player.teleport(Limbo.getInstance().getServerProperties().getWorldSpawn());
						sender.sendMessage(messages.getMessage("command-spawn-other", player.getName()));
					} else {
						sender.sendMessage(messages.getMessage("player-not-online"));
					}
				} else {
					sender.sendMessage(messages.getMessage("invalid-usage"));
				}
			} else {
				sender.sendMessage(messages.getMessage("no-permission"));
			}
			return;
		}
		
		if (args[0].equalsIgnoreCase("stop")) {
			if (sender.hasPermission("limboserver.stop")) {
				Limbo.getInstance().stopServer();
			} else {
				sender.sendMessage(messages.getMessage("no-permission"));
			}
			return;
		}
		
		if (args[0].equalsIgnoreCase("kick")) {
			if (sender.hasPermission("limboserver.kick")) {
				Component reason = Component.translatable("multiplayer.disconnect.kicked");
				boolean customReason = false;
				if (args.length > 1) {
					Player player = Limbo.getInstance().getPlayer(args[1]);
					if (player != null) {
						String reasonRaw = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
						if (reasonRaw.trim().length() > 0) {
							reason = LegacyComponentSerializer.legacySection().deserialize(reasonRaw);
							customReason = true;
						}
						player.disconnect(reason);
						if (customReason) {
							sender.sendMessage(messages.getMessage("command-kick", player.getName(), LegacyComponentSerializer.legacySection().serialize(reason)));
						} else {
							sender.sendMessage(messages.getMessage("command-kick-no-reason", player.getName()));
						}
					} else {
						sender.sendMessage(messages.getMessage("player-not-online"));
					}
				} else {
					sender.sendMessage(messages.getMessage("specify-player"));
				}
			} else {
				sender.sendMessage(messages.getMessage("no-permission"));
			}
			return;
		}
		
		if (args[0].equalsIgnoreCase("gamemode")) {
			if (sender.hasPermission("limboserver.gamemode")) {
				if (args.length > 1) {
					Player player = args.length > 2 ? Limbo.getInstance().getPlayer(args[2]) : (sender instanceof Player ? (Player) sender : null);
					if (!(sender instanceof Player)) {
						sender.sendMessage(messages.getMessage("specify-player"));
					} else if (player != null) {
						try {
							player.setGamemode(GameMode.fromId(Integer.parseInt(args[1])));
						} catch (Exception e) {
							try {
								player.setGamemode(GameMode.fromName(args[1]));
							} catch (Exception e1) {
								sender.sendMessage(messages.getMessage("invalid-usage"));
								return;
							}
						}
						sender.sendMessage(messages.getMessage("command-gamemode", player.getGamemode().getName()));
					} else {
						sender.sendMessage(messages.getMessage("player-not-online"));
					}
				} else {
					sender.sendMessage(messages.getMessage("invalid-usage"));
				}
			} else {
				sender.sendMessage(messages.getMessage("no-permission"));
			}
			return;
		}
		
		if (args[0].equalsIgnoreCase("say")) {
			if (sender.hasPermission("limboserver.say")) {
				if (sender instanceof Console) {
					if (args.length > 1) {
						String message = "[Server] " + String.join(" ", Arrays.copyOfRange(args, 1, args.length));
						Limbo.getInstance().getConsole().sendMessage(message);
						for (Player each : Limbo.getInstance().getPlayers()) {
							each.sendMessage(message);
						}
					}
				} else {
					if (args.length > 1) {
						String message = "[" + sender.getName() + "] " + String.join(" ", Arrays.copyOfRange(args, 1, args.length));
						Limbo.getInstance().getConsole().sendMessage(message);
						for (Player each : Limbo.getInstance().getPlayers()) {
							each.sendMessage(message);
						}
					}
				}
			} else {
				sender.sendMessage(messages.getMessage("no-permission"));
			}
			return;
		}

		if (args[0].equalsIgnoreCase("whitelist")) {
			if (sender.hasPermission("limboserver.whitelist")) {
				if (args.length != 2) {
					sender.sendMessage(messages.getMessage("invalid-usage"));
				} else if (!args[1].equalsIgnoreCase("reload")) {
					sender.sendMessage(messages.getMessage("invalid-usage"));
				} else {
					Limbo.getInstance().getServerProperties().reloadWhitelist();
					sender.sendMessage(messages.getMessage("command-whitelist"));
				}
			} else {
				sender.sendMessage(messages.getMessage("no-permission"));
			}
			return;
		}

		if (args[0].equalsIgnoreCase("messages")) {
			if (sender.hasPermission("limboserver.messages")) {
				if (args.length != 2) {
					sender.sendMessage(messages.getMessage("invalid-usage"));
				} else if (!args[1].equalsIgnoreCase("reload")) {
					sender.sendMessage(messages.getMessage("invalid-usage"));
				} else {
					messages.loadMessages();
					sender.sendMessage(messages.getMessage("command-messages"));
				}
			} else {
				sender.sendMessage(messages.getMessage("no-permission"));
			}
		}
	}
	
	@Override
	public List<String> tabComplete(CommandSender sender, String[] args) {
		List<String> tab = new ArrayList<>();
		switch (args.length) {
		case 0:
			if (sender.hasPermission("limboserver.spawn")) {
				tab.add("spawn");
			}
			if (sender.hasPermission("limboserver.kick")) {
				tab.add("kick");
			}
			if (sender.hasPermission("limboserver.stop")) {
				tab.add("stop");
			}
			if (sender.hasPermission("limboserver.say")) {
				tab.add("say");
			}
			if (sender.hasPermission("limboserver.gamemode")) {
				tab.add("gamemode");
			}
			break;
		case 1:
			if (sender.hasPermission("limboserver.spawn")) {
				if ("spawn".startsWith(args[0].toLowerCase())) {
					tab.add("spawn");
				}
			}
			if (sender.hasPermission("limboserver.kick")) {
				if ("kick".startsWith(args[0].toLowerCase())) {
					tab.add("kick");
				}
			}
			if (sender.hasPermission("limboserver.stop")) {
				if ("stop".startsWith(args[0].toLowerCase())) {
					tab.add("stop");
				}
			}
			if (sender.hasPermission("limboserver.say")) {
				if ("say".startsWith(args[0].toLowerCase())) {
					tab.add("say");
				}
			}
			if (sender.hasPermission("limboserver.gamemode")) {
				if ("gamemode".startsWith(args[0].toLowerCase())) {
					tab.add("gamemode");
				}
			}
			break;
		case 2:
			if (sender.hasPermission("limboserver.kick")) {
				if (args[0].equalsIgnoreCase("kick")) {
					for (Player player : Limbo.getInstance().getPlayers()) {
						if (player.getName().toLowerCase().startsWith(args[1].toLowerCase())) {
							tab.add(player.getName());
						}
					}
				}
			}
			if (sender.hasPermission("limboserver.gamemode")) {
				if (args[0].equalsIgnoreCase("gamemode")) {
					for (GameMode mode : GameMode.values()) {
						if (mode.getName().toLowerCase().startsWith(args[1].toLowerCase())) {
							tab.add(mode.getName());
						}
					}
				}
			}
			break;
		case 3:
			if (sender.hasPermission("limboserver.gamemode")) {
				if (args[0].equalsIgnoreCase("gamemode")) {
					for (Player player : Limbo.getInstance().getPlayers()) {
						if (player.getName().toLowerCase().startsWith(args[2].toLowerCase())) {
							tab.add(player.getName());
						}
					}
				}
			}
			break;
		}
		return tab;
	}

}
