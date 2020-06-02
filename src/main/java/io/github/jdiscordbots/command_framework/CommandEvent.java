package io.github.jdiscordbots.command_framework;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.util.List;

public class CommandEvent
{
	private final CommandFramework framework;
	private final GuildMessageReceivedEvent event;
	private final List<String> args;

	public CommandEvent(GuildMessageReceivedEvent event, List<String> args)
	{
		this.framework = CommandFramework.getInstance();
		this.event = event;
		this.args = args;
	}

	public CommandFramework getFramework() {
		return framework;
	}

	public GuildMessageReceivedEvent getEvent()
	{
		return this.event;
	}

	public List<String> getArgs()
	{
		return this.args;
	}

	public Guild getGuild()
	{
		return this.event.getGuild();
	}

	public JDA getJDA()
	{
		return this.event.getJDA();
	}

	public User getAuthor()
	{
		return this.event.getAuthor();
	}

	public Member getMember()
	{
		return this.event.getMember();
	}

	public Message getMessage()
	{
		return this.event.getMessage();
	}

	public TextChannel getChannel()
	{
		return this.event.getChannel();
	}

	public User getSelfUser()
	{
		return this.event.getJDA().getSelfUser();
	}

	public Member getSelfMember()
	{
		return this.event.getGuild().getSelfMember();
	}
}
