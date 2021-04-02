package io.github.jdiscordbots.command_framework.command.slash;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import io.github.jdiscordbots.command_framework.CommandFramework;
import io.github.jdiscordbots.command_framework.command.Argument;
import io.github.jdiscordbots.command_framework.command.ArgumentTemplate;
import io.github.jdiscordbots.command_framework.command.CommandEvent;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.MessageType;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.internal.entities.SystemMessage;

public class SlashCommandFrameworkEvent implements CommandEvent{

	private final SlashCommandEvent event;
	private List<Argument> args;
	private boolean acknowledged;
	private Message firstMessage;
	
	public SlashCommandFrameworkEvent(SlashCommandEvent event) {
		this.event = event;
	}
	
	public void loadArguments(Collection<ArgumentTemplate> expectedArgs) {
		if(args==null) {
			this.args=expectedArgs.stream()
				.map(ArgumentTemplate::getName)
				.map(event::getOption)
				.filter(Objects::nonNull)
				.map(SlashArgument::new)
				.collect(Collectors.toList());
		}else {
			throw new IllegalStateException("Arguments can only be loaded once");
		}
	}

	@Override
	public CommandFramework getFramework() {
		return CommandFramework.getInstance();
	}

	@Override
	public List<Argument> getArgs() {
		return args;
	}

	@Override
	public Guild getGuild() {
		return event.getGuild();
	}

	@Override
	public JDA getJDA() {
		return event.getJDA();
	}

	@Override
	public User getAuthor() {
		return event.getUser();
	}

	@Override
	public Member getMember() {
		return event.getMember();
	}

	@Override
	public MessageChannel getChannel() {
		return event.getChannel();
	}

	@Override
	public User getSelfUser() {
		return getJDA().getSelfUser();
	}

	@Override
	public Member getSelfMember() {
		return event.getGuild().getSelfMember();
	}

	@Override
	public String getId() {
		return event.getInteractionId();
	}

	@Override
	public PrivateChannel getPrivateChannel() {
		return event.getPrivateChannel();
	}

	@Override
	public Message getMessage() {
		if(firstMessage==null) {
			return new SystemMessage(getIdLong(), getChannel(), MessageType.APPLICATION_COMMAND, true, false, null, null, false, false, getArgs().stream().map(Argument::getAsString).collect(Collectors.joining(" ")), "", getAuthor(), getMember(), null, null, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), 0);
		}
		return firstMessage;
	}

	@Override
	public long getIdLong() {
		return event.getInteractionIdLong();
	}

	@Override
	public void reply(String message) {
		if(acknowledged) {
			event.getHook().sendMessage(message).queue();
		}else {
			event.reply(message).queue();
		}
	}
	
	@Override
	public void reply(Message message) {
		if(acknowledged) {
			event.getHook().sendMessage(message).queue();
		}else {
			event.reply(message).queue();
			acknowledged=true;
		}
	}
	
	@Override
	public void reply(MessageEmbed message) {
		if(acknowledged) {
			event.getHook().sendMessage(message).queue();
		}else {
			event.reply(message).queue();
			acknowledged=true;
		}
	}
	
	public void acknowledge() {
		if(!acknowledged) {
			event.acknowledge().queue();
			acknowledged=true;
		}
	}
}
