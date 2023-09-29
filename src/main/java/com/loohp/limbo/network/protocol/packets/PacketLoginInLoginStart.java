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

package com.loohp.limbo.network.protocol.packets;

import com.loohp.limbo.utils.DataTypeIO;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class PacketLoginInLoginStart extends PacketIn {
	
	private final String username;
	private final UUID uuid;
	
	public PacketLoginInLoginStart(String username, UUID uuid) {
		this.username = username;
		this.uuid = uuid;
	}
	
	public PacketLoginInLoginStart(DataInputStream in) throws IOException {
		this(DataTypeIO.readString(in, StandardCharsets.UTF_8), DataTypeIO.readUUID(in));
	}

	public String getUsername() {
		return username;
	}

	public UUID getUniqueId() {
		return uuid;
	}
}
