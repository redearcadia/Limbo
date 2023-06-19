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

package com.loohp.limbo.file;

import com.google.common.collect.Lists;
import com.loohp.limbo.Console;
import com.loohp.limbo.Limbo;
import com.loohp.limbo.location.Location;
import com.loohp.limbo.utils.ForwardingUtils;
import com.loohp.limbo.utils.GameMode;
import com.loohp.limbo.world.World;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;

public class ServerProperties {
	
	public static final String COMMENT = "For explaination of what each of the options does, please visit:\nhttps://github.com/LOOHP/Limbo/blob/master/src/main/resources/server.properties";
	
	private File file;
	private int maxPlayers;
	private int serverPort;
	private String serverIp;
	private Key levelName;
	private String schemFileName;
	private Key levelDimension;
	private GameMode defaultGamemode;
	private Location worldSpawn;
	private boolean reducedDebugInfo;
	private boolean logPlayerIPAddresses;
	private boolean allowFlight;
	private boolean allowChat;
	private Component motd;
	private String versionString;
	private int protocol;
	private boolean bungeecord;
	private boolean velocityModern;
	private boolean bungeeGuard;
	private ForwardingUtils.Type forwardType;
	private List<String> forwardingSecrets;
	private int viewDistance;
	private double ticksPerSecond;
	private boolean handshakeVerbose;
	private boolean enforceWhitelist;
	private Map<UUID, String> whitelist;
	
	private String resourcePackSHA1;
	private String resourcePackLink;
	private boolean resourcePackRequired;
	private Component resourcePackPrompt;
	
	private Component tabHeader;
	private Component tabFooter;

	private Optional<BufferedImage> favicon;

	public ServerProperties(File file) throws IOException {
		this.file = file;
		
		Properties def = new Properties();
		InputStreamReader defStream = new InputStreamReader(getClass().getClassLoader().getResourceAsStream("server.properties"), StandardCharsets.UTF_8);
		def.load(defStream);
		defStream.close();
		
		Properties prop = new Properties();
		InputStreamReader stream = new InputStreamReader(Files.newInputStream(file.toPath()), StandardCharsets.UTF_8);
		prop.load(stream);
		stream.close();
		
		for (Entry<Object, Object> entry : def.entrySet()) {
			String key = entry.getKey().toString();
			String value = entry.getValue().toString();
			prop.putIfAbsent(key, value);
		}
		PrintWriter pw = new PrintWriter(new OutputStreamWriter(Files.newOutputStream(file.toPath()), StandardCharsets.UTF_8));
		prop.store(pw, COMMENT);
		pw.close();

		protocol = Limbo.getInstance().SERVER_IMPLEMENTATION_PROTOCOL;

		maxPlayers = Integer.parseInt(prop.getProperty("max-players"));
		serverPort = Integer.parseInt(prop.getProperty("server-port"));
		serverIp = prop.getProperty("server-ip");
		String[] level = prop.getProperty("level-name").split(";");
		levelName = Key.key(level[0]);
		schemFileName = level[1];
		levelDimension = Key.key(prop.getProperty("level-dimension"));
		defaultGamemode = GameMode.fromName(Key.key(prop.getProperty("default-gamemode")).value());
		String[] locStr = prop.getProperty("world-spawn").split(";");
		World world = Limbo.getInstance().getWorld(locStr[0]);
		double x = Double.parseDouble(locStr[1]);
		double y = Double.parseDouble(locStr[2]);
		double z = Double.parseDouble(locStr[3]);
		float yaw = Float.parseFloat(locStr[4]);
		float pitch = Float.parseFloat(locStr[5]);
		worldSpawn = new Location(world, x, y, z, yaw, pitch);
		reducedDebugInfo = Boolean.parseBoolean(prop.getProperty("reduced-debug-info"));
		logPlayerIPAddresses = Boolean.parseBoolean(prop.getProperty("log-player-ip-addresses"));
		allowFlight = Boolean.parseBoolean(prop.getProperty("allow-flight"));
		allowChat = Boolean.parseBoolean(prop.getProperty("allow-chat"));
		String motdJson = prop.getProperty("motd");
		motd = motdJson.equals("") ? Component.empty() : GsonComponentSerializer.gson().deserialize(motdJson);
		versionString = prop.getProperty("version");
		bungeecord = Boolean.parseBoolean(prop.getProperty("bungeecord"));
		velocityModern = Boolean.parseBoolean(prop.getProperty("velocity-modern"));
		bungeeGuard = Boolean.parseBoolean(prop.getProperty("bungee-guard"));
		if (velocityModern || bungeeGuard) {
			String forwardingSecretsStr = prop.getProperty("forwarding-secrets");
			if (forwardingSecretsStr == null || forwardingSecretsStr.equals("")) {
				Limbo.getInstance().getConsole().sendMessage("Velocity Modern Forwarding or BungeeGuard is enabled but no forwarding-secret was found!");
				Limbo.getInstance().getConsole().sendMessage("Server will exit!");
				System.exit(1);
				return;
			}
			this.forwardingSecrets = Lists.newArrayList(forwardingSecretsStr.split(";"));
			if (bungeecord) {
				Limbo.getInstance().getConsole().sendMessage("BungeeCord is enabled but so is Velocity Modern Forwarding or BungeeGuard, We will automatically disable BungeeCord forwarding because of this");
				bungeecord = false;
			}
			if (velocityModern && bungeeGuard) {
				Limbo.getInstance().getConsole().sendMessage("Both Velocity Modern Forwarding and BungeeGuard are enabled! Because of this we always prefer Modern Forwarding, disabling BungeeGuard");
				bungeeGuard = false;
			}
		}

		if (velocityModern) {
			forwardType = ForwardingUtils.Type.VELOCITY_MODERN;
		} else if (bungeeGuard) {
			forwardType = ForwardingUtils.Type.BUNGEEGUARD;
		} else if (bungeecord) {
			forwardType = ForwardingUtils.Type.BUNGEECORD;
		} else {
			forwardType = ForwardingUtils.Type.NONE;
		}

		viewDistance = Integer.parseInt(prop.getProperty("view-distance"));
		ticksPerSecond = Double.parseDouble(prop.getProperty("ticks-per-second"));
		handshakeVerbose = Boolean.parseBoolean(prop.getProperty("handshake-verbose"));

		resourcePackLink = prop.getProperty("resource-pack");
		resourcePackSHA1 = prop.getProperty("resource-pack-sha1");
		resourcePackRequired = Boolean.parseBoolean(prop.getProperty("required-resource-pack"));
		String resourcePackPromptJson = prop.getProperty("resource-pack-prompt");
		resourcePackPrompt = resourcePackPromptJson.equals("") ? null : GsonComponentSerializer.gson().deserialize(resourcePackPromptJson);
		
		String tabHeaderJson = prop.getProperty("tab-header");
		tabHeader = tabHeaderJson.equals("") ? Component.empty() : GsonComponentSerializer.gson().deserialize(tabHeaderJson);
		String tabFooterJson = prop.getProperty("tab-footer");
		tabFooter = tabFooterJson.equals("") ? Component.empty() : GsonComponentSerializer.gson().deserialize(tabFooterJson);
		
		File png = new File("server-icon.png");
		if (png.exists()) {
			try {
				BufferedImage image = ImageIO.read(png);
				if (image.getHeight() == 64 && image.getWidth() == 64) {
					favicon = Optional.of(image);
				} else {
					Limbo.getInstance().getConsole().sendMessage("Unable to load server-icon.png! The image is not 64 x 64 in size!");
				}
			} catch (Exception e) {
				Limbo.getInstance().getConsole().sendMessage("Unable to load server-icon.png! Is it a png image?");
			}
		} else {
			Limbo.getInstance().getConsole().sendMessage("No server-icon.png found");
			favicon = Optional.empty();
		}

		enforceWhitelist = Boolean.parseBoolean(prop.getProperty("enforce-whitelist"));
		reloadWhitelist();

		Limbo.getInstance().getConsole().sendMessage("Loaded server.properties");
	}

	public void reloadWhitelist() {
        Console console = Limbo.getInstance().getConsole();
		File whitelistFile = new File("whitelist.json");
		if (!whitelistFile.exists()) {
			try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(Files.newOutputStream(whitelistFile.toPath())))) {
				pw.println("[]");
				pw.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		whitelist = new HashMap<>();
        try {
            JSONParser parser = new JSONParser();
            Object obj = parser.parse(new InputStreamReader(Files.newInputStream(whitelistFile.toPath()), StandardCharsets.UTF_8));

            if (!(obj instanceof JSONArray)) {
                console.sendMessage("whitelist: expected [] got {}");
                return;
            }

            JSONArray array = (JSONArray) obj;

			for (Object o : array) {
				if (!(o instanceof JSONObject)) {
					console.sendMessage("whitelist: array element is not an object");
					continue;
				}

				JSONObject element = (JSONObject) o;
				o = element.get("uuid");
				if (o == null) {
					console.sendMessage("whitelist: missing uuid attribute");
					continue;
				}
				if (!(o instanceof String)) {
					console.sendMessage("whitelist: uuid is not a string");
					continue;
				}

				String uuidStr = (String) o;
				UUID uuid = UUID.fromString(uuidStr);
				String name = element.containsKey("name") ? (String) element.get("name") : null;
				whitelist.put(uuid, name);
			}
        } catch (Exception e) {
			e.printStackTrace();
        }
	}

	public String getServerImplementationVersion() {
		return Limbo.getInstance().SERVER_IMPLEMENTATION_VERSION;
	}

	public String getServerModName() {
		return Limbo.LIMBO_BRAND;
	}

	public boolean isBungeecord() {
		return forwardType == ForwardingUtils.Type.BUNGEECORD;
	}

	public boolean isVelocityModern() {
		return forwardType == ForwardingUtils.Type.VELOCITY_MODERN;
	}

	public boolean isBungeeGuard() {
		return forwardType == ForwardingUtils.Type.BUNGEEGUARD;
	}

	public ForwardingUtils.Type getForwardType() {
		return forwardType;
	}

	public List<String> getForwardingSecrets() {
		return forwardingSecrets;
	}

	public Optional<BufferedImage> getFavicon() {
		return favicon;
	}

	public File getFile() {
		return file;
	}

	public int getMaxPlayers() {
		return maxPlayers;
	}

	public int getServerPort() {
		return serverPort;
	}

	public String getServerIp() {
		return serverIp;
	}

	public Key getLevelName() {
		return levelName;
	}

	public String getSchemFileName() {
		return schemFileName;
	}

	public Key getLevelDimension() {
		return levelDimension;
	}

	public GameMode getDefaultGamemode() {
		return defaultGamemode;
	}

	public Location getWorldSpawn() {
		return worldSpawn;
	}

	public void setWorldSpawn(Location location) {
		this.worldSpawn = location;
	}

	public boolean isReducedDebugInfo() {
		return reducedDebugInfo;
	}

	public boolean isLogPlayerIPAddresses() {
		return logPlayerIPAddresses;
	}

	public boolean isAllowFlight() {
		return allowFlight;
	}

	public boolean isAllowChat() {
		return this.allowChat;
	}

	public Component getMotd() {
		return motd;
	}

	public String getVersionString() {
		return versionString;
	}

	public int getProtocol() {
		return protocol;
	}
	
	public int getViewDistance() {
		return viewDistance;
	}
	
	public double getDefinedTicksPerSecond() {
		return ticksPerSecond;
	}

	public boolean handshakeVerboseEnabled() {
		return handshakeVerbose;
	}

	public boolean enforceWhitelist() {
		return enforceWhitelist;
	}

	public boolean uuidWhitelisted(UUID uuid)  {
		return whitelist.containsKey(uuid);
	}
	
	public String getResourcePackLink() {
		return resourcePackLink;
	}
	
	public String getResourcePackSHA1() {
		return resourcePackSHA1;
	}
	
	public boolean getResourcePackRequired() {
		return resourcePackRequired;
	}
	
	public Component getResourcePackPrompt() {
		return resourcePackPrompt;
	}
	
	public Component getTabHeader() {
		return tabHeader;
	}
	
	public Component getTabFooter() {
		return tabFooter;
	}

}
