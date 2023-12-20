package com.jagrosh.jmusicbot.commands.music;

import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.menu.Paginator;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.audio.SongCounter;
import com.jagrosh.jmusicbot.commands.MusicCommand;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.exceptions.PermissionException;

public class TopArtistCmd extends MusicCommand 
{
    private final Paginator.Builder builder;
    
    public TopArtistCmd(Bot bot)
    {
        super(bot);
        this.name = "hhw";
        this.help = "shows the most played artists";
        this.arguments = "[pagenum]";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.bePlaying = false;
        this.botPermissions = new Permission[]{Permission.MESSAGE_ADD_REACTION,Permission.MESSAGE_EMBED_LINKS};
        builder = new Paginator.Builder()
                .setColumns(1)
                .setFinalAction(m -> {try{m.clearReactions().queue();}catch(PermissionException ignore){}})
                .setItemsPerPage(10)
                .waitOnSinglePage(false)
                .useNumberedItems(true)
                .showPageNumbers(true)
                .wrapPageEnds(true)
                .setEventWaiter(bot.getWaiter())
                .setTimeout(1, TimeUnit.MINUTES);
    }

    @Override
    public void doCommand(CommandEvent event)
    {
        int pagenum = 1;
        try
        {
            pagenum = Integer.parseInt(event.getArgs());
        }
        catch(NumberFormatException ignore){}

        List<Entry<String, Integer>> list = SongCounter.getArtistCounts();
        if(list.isEmpty())
        {
            event.reply(event.getClient().getWarning() + " No songs were ever played!"); 
            return;
        }

        String[] artists = new String[list.size()];

        for(int i = 0; i < list.size(); i++)
        {
            Entry<String, Integer> entry = list.get(i);
            artists[i] =  "`[" + entry.getValue() + "]` " + entry.getKey();
        }

        builder.setItems(artists)
                .setText(event.getClient().getSuccess() + " Most Played Artists")
                .setUsers(event.getAuthor())
                .setColor(event.getSelfMember().getColor());
        builder.build().paginate(event.getChannel(), pagenum);
    }
}