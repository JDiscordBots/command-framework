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

public class MessageCommandEvent implements CommandEvent {
	private final MessageReceivedEvent event;
	private final List<Argument> args;

	public MessageCommandEvent(MessageReceivedEvent event, List<Argument> args) {
		this.event = event;
		this.args = Collections.unmodifiableList(new ArrayList<>(args));
	}

	@Override
	public CommandFramework getFramework() {
		return CommandFramework.getInstance();
	}

	@Override
	public List<Argument> getArgs() {
		return this.args;
	}

	@Override
	public Guild getGuild() {
		return this.event.getGuild();
	}

	@Override
	public JDA getJDA() {
		return this.event.getJDA();
	}

	@Override
	public User getAuthor() {
		return this.event.getAuthor();
	}

	@Override
	public Member getMember() {
		return this.event.getMember();
	}

	@Override
	public Message getMessage() {
		return this.event.getMessage();
	}

	public MessageReceivedEvent getEvent() {
		return event;
	}

	@Override
	public MessageChannel getChannel() {
		return this.event.getChannel();
	}

	@Override
	public User getSelfUser() {
		return this.event.getJDA().getSelfUser();
	}

	@Override
	public Member getSelfMember() {
		return this.event.getGuild().getSelfMember();
	}

	@Override
	public RestAction<Message> reply(String message) {
		return event.getMessage().reply(message);
	}

	@Override
	public RestAction<Message> reply(MessageEmbed message) {
		return event.getMessage().reply(message);
	}

	@Override
	public RestAction<Message> reply(Message message) {
		return event.getMessage().reply(message);
	}

	
	@Override
	public String getId() {
		return event.getMessageId();
	}

	@Override
	public long getIdLong() {
		return event.getMessageIdLong();
	}

	@Override
	public PrivateChannel getPrivateChannel() {
		return event.getMessage().getPrivateChannel();
	}

	@Override
	public RestAction<Void> deleteOriginalMessage() {
		return event.getMessage().delete();
	}
}
