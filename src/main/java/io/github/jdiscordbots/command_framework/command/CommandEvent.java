package io.github.jdiscordbots.command_framework.command;

import io.github.jdiscordbots.command_framework.CommandFramework;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.util.List;

/**
 * CommandEvent
 */
public class CommandEvent {
	private final GuildMessageReceivedEvent event;
	private final List<String> args;

	/**
	 * Construct a new CommandEvent
	 *
	 * @param event incomming {@link net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent GuildMessageReceivedEvent}
	 * @param args  Command arguments
	 */
	public CommandEvent(GuildMessageReceivedEvent event, List<String> args) {
		this.event = event;
		this.args = args;
	}

	/**
	 * Get CommandFramework instance
	 *
	 * @return {@link io.github.jdiscordbots.command_framework.CommandFramework CommandFramework} instance
	 */
	public CommandFramework getFramework() {
		return CommandFramework.getInstance();
	}

	/**
	 * Get corresponding message event
	 *
	 * @return incomming {@link net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent GuildMessageReceivedEvent}
	 */
	public GuildMessageReceivedEvent getEvent() {
		return this.event;
	}

	/**
	 * Get command arguments as {@link java.util.List<String> List}
	 *
	 * @return command arguments
	 */
	public List<String> getArgs() {
		return this.args;
	}

	/**
	 * Get corresponding guild of event
	 *
	 * @return {@link net.dv8tion.jda.api.entities.Guild Guild} of event
	 */
	public Guild getGuild() {
		return this.event.getGuild();
	}

	/**
	 * Get corresponding API of event
	 *
	 * @return {@link net.dv8tion.jda.api.JDA JDA} instance of event
	 */
	public JDA getJDA() {
		return this.event.getJDA();
	}

	/**
	 * Get corresponding user of event
	 *
	 * @return {@link net.dv8tion.jda.api.entities.User User} of event
	 */
	public User getAuthor() {
		return this.event.getAuthor();
	}

	/**
	 * Get corresponding member of event
	 *
	 * @return {@link net.dv8tion.jda.api.entities.Member Member} if event
	 */
	public Member getMember() {
		return this.event.getMember();
	}

	/**
	 * Get corresponding message of event
	 *
	 * @return {@link net.dv8tion.jda.api.entities.Message Message} of event
	 */
	public Message getMessage() {
		return this.event.getMessage();
	}

	/**
	 * Get corresponding channel of event
	 *
	 * @return {@link net.dv8tion.jda.api.entities.TextChannel TextChannel} of event
	 */
	public TextChannel getChannel() {
		return this.event.getChannel();
	}

	/**
	 * Get corresponding self user of JDA instance
	 *
	 * @return {@link net.dv8tion.jda.api.entities.SelfUser SelfUser} of JDA
	 */
	public User getSelfUser() {
		return this.event.getJDA().getSelfUser();
	}

	/**
	 * Get corresponding self member of guild of event
	 *
	 * @return {@link net.dv8tion.jda.api.entities.Member Member} of event
	 */
	public Member getSelfMember() {
		return this.event.getGuild().getSelfMember();
	}

	/**
	 * Reply on Event
	 * <p>
	 * Shortcut for <code>event.getChannel().sendMessage(CharSequence).queue()</code>
	 *
	 * @param text Text to send
	 */
	public void reply(CharSequence text) {
		this.getChannel().sendMessage(text).queue();
	}

	/**
	 * Reply on Event
	 * <p>
	 * Shortcut for <code>event.getChannel().sendMessage(Message).queue()</code>
	 *
	 * @param msg Message to send
	 */
	public void reply(Message msg) {
		this.getChannel().sendMessage(msg).queue();
	}

	/**
	 * Reply on Event
	 * <p>
	 * Shortcut for <code>event.getChannel().sendMessage(MessageEmbed).queue()</code>
	 *
	 * @param embed MessageEmbed to send
	 */
	public void reply(MessageEmbed embed) {
		this.getChannel().sendMessage(embed).queue();
	}
}
