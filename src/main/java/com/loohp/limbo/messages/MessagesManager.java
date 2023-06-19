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

package com.loohp.limbo.messages;

import com.loohp.limbo.Limbo;
import com.loohp.limbo.file.FileConfiguration;
import net.md_5.bungee.api.ChatColor;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

public class MessagesManager {

    private Map<String, String> messages;

    public MessagesManager() {
        messages = new HashMap<>();
    }

    public void loadDefaultMessageFile(File file) throws IOException {
        FileConfiguration config = new FileConfiguration(file);
        try {
            for (Object obj : config.get("messages", Map.class).keySet()) {
                String key = (String) obj;
                String message = ChatColor.translateAlternateColorCodes('&', config.get("messages." + key, String.class));
                messages.put(key, message);
            }
        } catch (Exception e) {}
    }

    public String getMessage(String key, Object... args) {
        if (!messages.containsKey(key)) {
            Limbo.getInstance().getConsole().sendMessage("Tried to get message '" + key + "' from message.yml but wasn't found. Please try resetting this file.");
        }
        return MessageFormat.format(messages.getOrDefault(key, "<message '" + key + "' missing>"), args);
    }

    public Map<String, String> getMessages() {
        return messages;
    }

}
