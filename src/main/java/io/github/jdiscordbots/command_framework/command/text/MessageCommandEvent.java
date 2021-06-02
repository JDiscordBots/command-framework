package io.github.jdiscordbots.command_framework.command.text;

import io.github.jdiscordbots.command_framework.CommandFramework;
import io.github.jdiscordbots.command_framework.command.Argument;
import io.github.jdiscordbots.command_framework.command.CommandEvent;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.requests.RestAction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A {@link CommandEvent} representing an executed text command.
 */
public final class MessageCommandEvent implements CommandEvent {
	
	private final CommandFramework framework;
	private final MessageReceivedEvent event;
	private final List<Argument> args;

	public MessageCommandEvent(CommandFramework framework,MessageReceivedEvent event, List<Argument> args) {
		this.event = event;
		this.args = Collections.unmodifiableList(new ArrayList<>(args));
		this.framework=framework;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public CommandFramework getFramework() {
		return framework;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Argument> getArgs() {
		return this.args;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Guild getGuild() {
		return this.event.getGuild();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public JDA getJDA() {
		return this.event.getJDA();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public User getAuthor() {
		return this.event.getAuthor();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Member getMember() {
		return this.event.getMember();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Message getMessage() {
		return this.event.getMessage();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public MessageChannel getChannel() {
		return this.event.getChannel();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SelfUser getSelfUser() {
		return this.event.getJDA().getSelfUser();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Member getSelfMember() {
		return this.event.getGuild().getSelfMember();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public RestAction<Message> reply(String message) {
		return event.getMessage().reply(message);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public RestAction<Message> reply(MessageEmbed message) {
		return event.getMessage().reply(message);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public RestAction<Message> reply(Message message) {
		return event.getMessage().reply(message);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getId() {
		return event.getMessageId();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getIdLong() {
		return event.getMessageIdLong();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PrivateChannel getPrivateChannel() {
		return event.getMessage().getPrivateChannel();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public RestAction<Void> deleteOriginalMessage() {
		return event.getMessage().delete();
	}

	public MessageReceivedEvent getEvent() {
		return event;
	}
}
