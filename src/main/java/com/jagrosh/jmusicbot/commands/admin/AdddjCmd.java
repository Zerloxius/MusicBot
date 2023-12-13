
package com.jagrosh.jmusicbot.commands.admin;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.commands.AdminCommand;
import com.jagrosh.jmusicbot.settings.Settings;

/**
 *
 * @author Welka
 */
public class AdddjCmd extends AdminCommand
{
    public AdddjCmd(Bot bot)
    {
        this.name = "adddj";
        this.help = "add user as DJ for certain music commands";
        this.arguments = "<username>";
        this.aliases = bot.getConfig().getAliases(this.name);
    }
    
    @Override
    protected void execute(CommandEvent event) 
    {
        if(event.getArgs().isEmpty())
        {
            event.reply(event.getClient().getError()+" Please include a user name");
            return;
        }
        Settings s = event.getClient().getSettingsFor(event.getGuild());

        s.addDJName(event.getArgs());
        event.reply(event.getClient().getSuccess()+" DJ commands can now be used by **"+event.getArgs()+"**.");
    }
    
}