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

package com.loohp.limbo.utils;

import com.loohp.limbo.Limbo;
import net.kyori.adventure.key.Key;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.UUID;

public class ForwardingUtils {

    public static final Key VELOCITY_FORWARDING_CHANNEL = Key.key("velocity", "player_info");

    public static boolean validateVelocityModernResponse(byte[] data) throws IOException {
        ByteArrayInputStream byteIn = new ByteArrayInputStream(data);
        DataInputStream input = new DataInputStream(byteIn);

        byte[] signature = new byte[32];
        input.readFully(signature);

        boolean foundValid = false;
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            for (String secret : Limbo.getInstance().getServerProperties().getForwardingSecrets()) {
                SecretKey key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
                mac.init(key);
                mac.update(data, 32, data.length - 32);
                byte[] sig = mac.doFinal();
                if (Arrays.equals(signature, sig)) {
                    foundValid = true;
                    break;
                }
            }
        } catch (InvalidKeyException e) {
            throw new RuntimeException("Unable to authenticate data", e);
        } catch (NoSuchAlgorithmException e) {
            // Should never happen
            throw new AssertionError(e);
        }

        return foundValid;
    }

    public static VelocityModernForwardingData getVelocityDataFrom(byte[] data) throws IOException {
        ByteArrayInputStream byteIn = new ByteArrayInputStream(data);
        DataInputStream input = new DataInputStream(byteIn);

        input.skipBytes(32);

        int velocityVersion = DataTypeIO.readVarInt(input);
        if (velocityVersion != 1) {
            System.out.println("Unsupported Velocity version! Stopping Execution");
            throw new AssertionError("Unknown Velocity Packet");
        }
        String address = DataTypeIO.readString(input, StandardCharsets.UTF_8);
        UUID uuid = DataTypeIO.readUUID(input);
        String username = DataTypeIO.readString(input, StandardCharsets.UTF_8);

        //cycle properties
        MojangAPIUtils.SkinResponse response = null;
        int count = DataTypeIO.readVarInt(input);
        for (int i = 0; i < count; ++i) {
            String propertyName = DataTypeIO.readString(input, StandardCharsets.UTF_8);
            String propertyValue = DataTypeIO.readString(input, StandardCharsets.UTF_8);
            String propertySignature = "";
            boolean signatureIncluded = input.readBoolean();
            if (signatureIncluded) {
                propertySignature = DataTypeIO.readString(input, StandardCharsets.UTF_8);
            }
            if (propertyName.equals("textures")) {
                response = new MojangAPIUtils.SkinResponse(propertyValue, propertySignature);
                break; // we don't use others properties for now
            }
        }

        return new VelocityModernForwardingData(velocityVersion, address, uuid, username, response);
    }

    public static class VelocityModernForwardingData {

        private final int version;
        private final String ipAddress;
        private final UUID uuid;
        private final String username;
        private final MojangAPIUtils.SkinResponse skinResponse;

        public VelocityModernForwardingData(int version, String ipAddress, UUID uuid, String username, MojangAPIUtils.SkinResponse skinResponse) {
            this.version = version;
            this.ipAddress = ipAddress;
            this.uuid = uuid;
            this.username = username;
            this.skinResponse = skinResponse;
        }

        public int getVersion()
        {
            return version;
        }

        public String getIpAddress()
        {
            return ipAddress;
        }

        public UUID getUuid()
        {
            return uuid;
        }

        public String getUsername()
        {
            return username;
        }

        public MojangAPIUtils.SkinResponse getSkinResponse()
        {
            return skinResponse;
        }
    }

    public enum Type {
        VELOCITY_MODERN,
        BUNGEEGUARD,
        BUNGEECORD,
        NONE
    }
}
