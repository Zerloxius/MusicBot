package com.jagrosh.jmusicbot.audio;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.utils.OtherUtil;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

public class SongCounter {

    private static final Object lock = new Object();
    private static Map<String, Integer> songCounter = new ConcurrentHashMap<>();
    private static ArrayList<String> songHistory = new ArrayList<>();
    private static Bot bot;

    public static void initSongCounter(Bot bot)
    {
        Logger log = LoggerFactory.getLogger("Counter");
        SongCounter.bot = bot;

        try {
            JSONObject json = new JSONObject(new String(Files.readAllBytes(OtherUtil.getPath("songcount.json")), StandardCharsets.UTF_8));
            json.keySet().forEach(songName ->
            {
                int plays = json.getInt(songName);
                songCounter.put(songName, plays);
            });
            log.info("Song counter loaded! (" + songCounter.size() + " songs)");
        } catch (Exception e) {
            log.warn("Song counter couldn't be loaded! " + e);
        }
    }

    public static ArrayList<String> getHistory()
    {
        return songHistory;
    }

    public static List<Map.Entry<String, Integer>> getSongCounts()
    {
        return songCounter.entrySet()
            .stream()
            .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
            .collect(Collectors.toList());
    }

    public static List<Map.Entry<String, Integer>> getArtistCounts()
    {
        return songCounter.entrySet()
        .stream()
        .collect(Collectors.groupingBy(entry -> getArtist(entry.getKey()), LinkedHashMap::new, Collectors.summingInt(Map.Entry::getValue)))
        .entrySet()
        .stream()
        .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
        .collect(Collectors.toList());
    }

    private static String getArtist(String songName) {
        int dashIndex = songName.indexOf(" - ");
        if (dashIndex != -1) {
            return songName.substring(0, dashIndex);
        } else {
            return songName; // if no " - " is found, return the entire string as the artist
        }
    }

    public static void countSong(AudioTrack track, long guildId)
    {
        String songTitle = track.getInfo().author + " - " + track.getInfo().title;
        songHistory.add(0, songTitle);
        songCounter.put(songTitle, songCounter.getOrDefault(songTitle, 0) + 1);
        saveCounter(guildId);
    }

    private static void saveCounter(long guildId) {
        synchronized (lock) {
            JSONObject jsonObject = new JSONObject(songCounter);
            File tmp = new File("songcount.json.tmp");
            File target = new File("songcount.json");

            try (Writer writer = new OutputStreamWriter(new FileOutputStream(tmp), StandardCharsets.UTF_8)) {
                writer.write(jsonObject.toString(4));
                writer.flush(); // just to be safe
            } catch (IOException | JSONException e) {
                LoggerFactory.getLogger("Counter").warn("⚠️ Failed to write temp file: " + e);
                Guild guild = bot.getJDA().getGuildById(guildId);
                TextChannel textChannel = bot.getSettingsManager().getSettings(guildId).getTextChannel(guild);
                if (textChannel != null) {
                    textChannel.sendMessage("⚠️ Failed to write to file: " + e).queue();
                }
                return; // stop, don’t try to replace the file
            }

            try {
                Files.move(tmp.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (IOException e) {
                LoggerFactory.getLogger("Counter").warn("⚠️ Failed to replace file: " + e);
            }
        }
    }

    // private static void saveCounter(long guildId)
    // {
    //     synchronized (lock) {
    //         JSONObject jsonObject = new JSONObject(songCounter);
    //         // Debug PrintLine in DM Style
    //         //User owner = bot.getJDA().retrieveUserById(bot.getConfig().getOwnerId()).complete();
    //         //owner.openPrivateChannel().queue(pc -> pc.sendMessage(jsonObject.toString(4)).queue());
            
    //         try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream("songcount.json"), StandardCharsets.UTF_8))
    //         {
    //             String s = jsonObject.toString(4);
    //             writer.write(s);
    //         } catch(IOException | JSONException e){
    //             LoggerFactory.getLogger("Counter").warn("Failed to write to file: " + e);
    //             Guild guild = bot.getJDA().getGuildById(guildId);
    //             TextChannel textChannel = bot.getSettingsManager().getSettings(guildId).getTextChannel(guild);
    //             textChannel.sendMessage("⚠️ Failed to write to file: " + e).queue();
    //         }
    //     }
    // }
}