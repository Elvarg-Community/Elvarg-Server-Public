package com.elvarg.util;

import com.elvarg.game.GameLogic;

import java.io.*;
import java.util.ArrayList;

public class PlayerPunishment {

    private static final String BAN_DIRECTORY = "./data/saves/";
    private static final String MUTE_DIRECTORY = "./data/saves/";

    public static ArrayList<String> IPSBanned = new ArrayList<>();
    public static ArrayList<String> IPSMuted = new ArrayList<>();
    public static ArrayList<String> AccountsBanned = new ArrayList<>();
    public static ArrayList<String> AccountsMuted = new ArrayList<>();

    public static void init() {
        //In case we're reloading bans, reset lists first.
        IPSBanned.clear();
        IPSMuted.clear();
        AccountsBanned.clear();
        AccountsMuted.clear();

        initializeList(BAN_DIRECTORY, "IPBans", IPSBanned);
        initializeList(BAN_DIRECTORY, "Bans", AccountsBanned);
        initializeList(MUTE_DIRECTORY, "IPMutes", IPSMuted);
        initializeList(MUTE_DIRECTORY, "Mutes", AccountsMuted);
    }

    public static void initializeList(String directory, String name, ArrayList<String> list) {
        try {
            File file = new File(directory,name + ".txt");
            if(!file.exists()) {
                file.createNewFile();
            }
            BufferedReader in = new BufferedReader(new FileReader(directory + "" + name + ".txt"));

            String data = null;
            while ((data = in.readLine()) != null) {
                list.add(data);
            }
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void addBannedIP(String IP) {
        if (!IPSBanned.contains(IP))
            addToFile(BAN_DIRECTORY + "IPBans.txt", IP);
        IPSBanned.add(IP);

    }

    public static void addMutedIP(String IP) {
        if (!IPSMuted.contains(IP))
            addToFile(MUTE_DIRECTORY + "IPMutes.txt", IP);
        IPSMuted.add(IP);

    }

    public static void ban(String p) {
        p = Misc.formatPlayerName(p.toLowerCase());
        if (!AccountsBanned.contains(p))
            addToFile(BAN_DIRECTORY + "Bans.txt", p);
        AccountsBanned.add(p);

    }

    public static void mute(String p) {
        p = Misc.formatPlayerName(p.toLowerCase());
        if (!AccountsMuted.contains(p))
            addToFile(MUTE_DIRECTORY + "Mutes.txt", p);
        AccountsMuted.add(p);

    }

    public static boolean banned(String player) {

        player = Misc.formatPlayerName(player.toLowerCase());
        return AccountsBanned.contains(player);

    }

    public static boolean muted(String player) {

        player = Misc.formatPlayerName(player.toLowerCase());
        return AccountsMuted.contains(player);

    }

    public static boolean IPBanned(String IP) {
        return IPSBanned.contains(IP);
    }

    public static boolean IPMuted(String IP) {
        return IPSMuted.contains(IP);
    }

    public static void unban(String player) {
        player = Misc.formatPlayerName(player.toLowerCase());
        deleteFromFile(BAN_DIRECTORY + "Bans.txt", player);
        AccountsBanned.remove(player);

    }

    public static void unmute(String player) {
        player = Misc.formatPlayerName(player.toLowerCase());
        deleteFromFile(MUTE_DIRECTORY + "Mutes.txt", player);
        AccountsMuted.remove(player);

    }

    public static void reloadIPBans() {
        IPSBanned.clear();
        initializeList(BAN_DIRECTORY, "IPBans", IPSBanned);
    }

    public static void reloadIPMutes() {
        IPSMuted.clear();
        initializeList(MUTE_DIRECTORY, "IPMutes", IPSMuted);
    }

    public static void deleteFromFile(String file, String name) {
        GameLogic.submit(() -> {
            try {
                BufferedReader r = new BufferedReader(new FileReader(file));
                ArrayList<String> contents = new ArrayList<>();
                while (true) {
                    String line = r.readLine();
                    if (line == null) {
                        break;
                    } else {
                        line = line.trim();
                    }
                    if (!line.equalsIgnoreCase(name)) {
                        contents.add(line);
                    }
                }
                r.close();
                BufferedWriter w = new BufferedWriter(new FileWriter(file));
                for (String line : contents) {
                    w.write(line, 0, line.length());
                    w.newLine();
                }
                w.flush();
                w.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public static void addToFile(String file, String data) {
        GameLogic.submit(() -> {
            try {
                try (BufferedWriter out = new BufferedWriter(new FileWriter(file, true))) {
                    out.write(data);
                    out.newLine();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
