
package com.jagrosh.jmusicbot.commands.dj;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.text.similarity.LevenshteinDistance;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.audio.AudioHandler;
import com.jagrosh.jmusicbot.audio.QueuedTrack;
import com.jagrosh.jmusicbot.commands.DJCommand;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;

public class NextCmd extends DJCommand 
{
    public NextCmd(Bot bot)
    {
        super(bot);
        this.name = "next";
        this.help = "searches for a song in the queue and plays it after the current one";
        this.arguments = "<title>";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.bePlaying = true;
    }

    @Override
    public void doCommand(CommandEvent event) 
    {
        if(event.getArgs().isEmpty() && event.getMessage().getAttachments().isEmpty())
        {
            event.replyWarning("Please include a song title");
            return;
        }

        AudioHandler handler = (AudioHandler) event.getGuild().getAudioManager().getSendingHandler();
        int result = findClosestString(handler.getQueue().getList(), event.getArgs());

        if (result == -1) {
            event.replyWarning("No results found for `"+event.getArgs()+"`.");
            return;
        }

        handler.getQueue().moveItem(result, 0);
        AudioTrackInfo info = handler.getQueue().get(0).getTrack().getInfo();

        event.reply(event.getClient().getSuccess()+" Playing next: **"+info.title+"** by *"+info.author+"*");
    }

    private static int findClosestString(List<QueuedTrack> trackList, String query) {
        int minDistance = Integer.MAX_VALUE;
        double maxSimilarity = 0.0;
        int closestID = -1;
        int closestIDLevenshtein = -1;

        for (int i = 0; i < trackList.size(); i++) {
            AudioTrackInfo tInfo = trackList.get(i).getTrack().getInfo();
            String title = tInfo.author + " " + tInfo.title;
            double similarity = calculateJaccardSimilarity(title, query);
            int distance = LevenshteinDistance.getDefaultInstance().apply(tInfo.title, query);

            if (similarity > maxSimilarity) {
                maxSimilarity = similarity;
                closestID = i;
            }

            if (distance < minDistance) {
                minDistance = distance;
                closestIDLevenshtein = i;
            }
        }

        if (closestID != -1)
            return closestID;
        else
            return closestIDLevenshtein;
    }

    private static double calculateJaccardSimilarity(String str1, String str2) {
        Set<String> set1 = new HashSet<>(Arrays.asList(str1.toLowerCase().split("[\\s-]+")));
        Set<String> set2 = new HashSet<>(Arrays.asList(str2.toLowerCase().split("[\\s-]+")));

        Set<String> intersection = new HashSet<>(set1);
        intersection.retainAll(set2);

        Set<String> union = new HashSet<>(set1);
        union.addAll(set2);

        return (double) intersection.size() / union.size();
    }
}