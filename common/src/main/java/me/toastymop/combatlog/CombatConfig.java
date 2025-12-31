package me.toastymop.combatlog;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quiltmc.parsers.json.JsonReader;
import org.quiltmc.parsers.json.JsonToken;
import org.quiltmc.parsers.json.JsonWriter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CombatConfig {
    public static Config CONFIG;
    static File configFolder = new File("./config");
    static File configFile = new File(configFolder + "/combatlog-common.json5");
    protected static final Logger log = LogManager.getLogger(CombatLog.LOGGER);

    public static Config load() {
        if (!configFolder.exists()) {
            configFolder.mkdirs();
        }
        if (!configFile.getName().endsWith(".json5")) {
            throw new RuntimeException("Failed to read config");
        }

        Config cfg = null;
        if (configFile.exists()) {
            try (JsonReader reader = JsonReader.json5(configFile.toPath())) {
                cfg = new Config();
                reader.beginObject();
                while (reader.hasNext()) {
                    String nextName = reader.nextName();
                    switch (nextName) {
                        case "combatTime":
                            cfg.combatTime = reader.nextInt();
                            break;
                        case "allDamage":
                            cfg.allDamage = reader.nextBoolean();
                            break;
                        case "mobDamage":
                            cfg.mobDamage = reader.nextBoolean();
                            break;
                        case "disableElytra":
                            cfg.disableElytra = reader.nextBoolean();
                            break;
                        case "disablePearl":
                            cfg.disablePearl = reader.nextBoolean();
                            break;
                        case "deathMessage":
                            cfg.deathMessage = reader.nextString();
                            break;
                        case "combatNotice":
                            cfg.combatNotice = reader.nextBoolean();
                            break;
                        case "inCombat":
                            cfg.inCombat = reader.nextString();
                            break;
                        case "outCombat":
                            cfg.outCombat = reader.nextString();
                            break;
                        case "blockedCommands":
                            cfg.blockedCommands = readStringList(reader);
                            break;
                        case "blockedCommandMessage":
                            cfg.blockedCommandMessage = reader.nextString();
                            break;
                        case "disconnectKill":
                            cfg.disconnectKill = reader.nextBoolean();
                            break;
                        case "disconnectCommand":
                            cfg.disconnectCommand = reader.nextString();
                            break;
                        case "blockedItemMessage":
                            cfg.blockedItemMessage = reader.nextString();
                            break;
                        case "blockedItems":
                            cfg.blockedItems = readStringList(reader);
                            break;
                        default:
                            reader.skipValue();
                            break;
                    }
                }
                reader.endObject();
            } catch (IOException e) {
                log.error("Failed to parse config", e);
            }
        }

        if (cfg == null) cfg = new Config();
        save(configFile, cfg);
        return cfg;
    }

    // Helper to read either array or comma-separated string
    private static List<String> readStringList(JsonReader reader) throws IOException {
        List<String> list = new ArrayList<>();

        if (reader.peek() == JsonToken.BEGIN_ARRAY) {
            reader.beginArray();
            while (reader.hasNext()) {
                String value = reader.nextString().trim();
                if (!value.isEmpty()) {
                    list.add(value);
                }
            }
            reader.endArray();
        } else {
            String str = reader.nextString().trim();
            if (!str.isEmpty()) {
                for (String part : str.split("\\s*,\\s*")) {
                    if (!part.isEmpty()) {
                        list.add(part);
                    }
                }
            }
        }

        return list;
    }

    public static void save(File file, Config cfg) {
        try (JsonWriter writer = JsonWriter.json5(file.toPath())) {
            writer.beginObject();

            writer.comment("The amount of time in seconds a player should be in combat")
                    .name("combatTime").value(cfg.combatTime);

            writer.comment("Whether a player should be put in combat from just other players or all damage")
                    .name("allDamage").value(cfg.allDamage);

            writer.comment("Whether a player should be put in combat from mobs")
                    .name("mobDamage").value(cfg.mobDamage);

            writer.comment("Whether a player should be able to use their elytra while in combat (restricts starting flight only)")
                    .name("disableElytra").value(cfg.disableElytra);

            writer.comment("Whether a player should be able to use ender pearls while in combat")
                    .name("disablePearl").value(cfg.disablePearl);

            writer.comment("The death message shown when a player disconnects while in combat")
                    .name("deathMessage").value(cfg.deathMessage);

            writer.comment("Whether to show notices when entering combat or attempting blocked actions")
                    .name("combatNotice").value(cfg.combatNotice);

            writer.comment("Message shown while in combat ({timeLeft} for remaining seconds)")
                    .name("inCombat").value(cfg.inCombat);

            writer.comment("Message shown when leaving combat")
                    .name("outCombat").value(cfg.outCombat);

            writer.comment("List of commands (without /) to block during combat")
                    .name("blockedCommands").beginArray();
            for (String cmd : cfg.blockedCommands) {
                writer.value(cmd);
            }
            writer.endArray();

            writer.comment("Message shown when attempting a blocked command")
                    .name("blockedCommandMessage").value(cfg.blockedCommandMessage);

            writer.comment("Kill player on disconnect while in combat")
                    .name("disconnectKill").value(cfg.disconnectKill);

            writer.comment("Command to run on disconnect while in combat ({player} placeholder)")
                    .name("disconnectCommand").value(cfg.disconnectCommand);

            writer.comment("Message shown when attempting to use a blocked item")
                    .name("blockedItemMessage").value(cfg.blockedItemMessage);

            writer.comment("List of item registry IDs to block during combat")
                    .name("blockedItems").beginArray();
            for (String item : cfg.blockedItems) {
                writer.value(item);
            }
            writer.endArray();

            writer.endObject();
        } catch (IOException e) {
            log.error("Failed to save config", e);
        }
    }

    public static class Config {
        public static int combatTime = 30;
        public static boolean allDamage = false;
        public static boolean mobDamage = false;
        public static boolean disableElytra = false;
        public static boolean disablePearl = false;
        public static String deathMessage = " has died of cowardice";
        public static boolean combatNotice = true;
        public static String inCombat = "You are in combat do not leave! {timeLeft} seconds left";
        public static String outCombat = "You are no longer in combat";
        public static List<String> blockedCommands = new ArrayList<>();
        public static String blockedCommandMessage = "You are in combat and cannot execute this command";
        public static boolean disconnectKill = true;
        public static String disconnectCommand = "";
        public static String blockedItemMessage = "You are in combat and cannot use this item";
        public static List<String> blockedItems = new ArrayList<>(List.of("minecraft:golden_apple"));
    }
}