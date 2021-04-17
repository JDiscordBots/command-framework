package io.github.jdiscordbots.command_framework.command.text;

import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.github.jdiscordbots.command_framework.command.Argument;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Command.OptionType;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;

public final class MessageArgument implements Argument{
	
	private static final Pattern USER_FORMAT=Pattern.compile("\\<@!?(\\d+)\\>");
	private static final Pattern ROLE_FORMAT=Pattern.compile("\\<@&(\\d+)\\>");
	private static final Pattern CHANNEL_FORMAT=Pattern.compile("\\<#(\\d+)\\>");
	
	private Message msg;
	private String text;
	
	public MessageArgument(Message msg, String text) {
		this.msg=msg;
		this.text=text;
	}

	@Override
	public String getAsString() {
		return text;
	}

	@Override
	public Role getAsRole() {
		requireInGuild();
		return getAsMentionedEntityOrThrowIfNotExist(ROLE_FORMAT, msg.getGuild()::getRoleById,()->new IllegalStateException("Argument cannot be converted to Role"));
	}

	@Override
	public boolean getAsBoolean() {
		return Boolean.parseBoolean(text);
	}

	@Override
	public OptionType getType() {
		return OptionType.STRING;
	}

	@Override
	public long getAsLong() {
		return Long.parseLong(text);
	}

	@Override
	public Member getAsMember() {
		requireInGuild();
		return getAsMentionedEntityOrThrowIfNotExist(USER_FORMAT, msg.getGuild()::getMemberById,()->new IllegalStateException("Argument cannot be converted to Member"));
	}

	@Override
	public User getAsUser() {
		return getAsMentionedEntityOrThrowIfNotExist(USER_FORMAT, msg.getJDA()::getUserById,()->new IllegalStateException("Argument cannot be converted to User"));
	}

	@Override
	public GuildChannel getAsGuildChannel() {
		requireInGuild();
		return getAsMentionedEntityOrThrowIfNotExist(CHANNEL_FORMAT, msg.getGuild()::getGuildChannelById,()->new IllegalStateException("Argument cannot be converted to guild channel"));
	}

	@Override
	public MessageChannel getAsMessageChannel() {
		return getAsMentionedEntityOrThrowIfNotExist(CHANNEL_FORMAT, msg.getJDA()::getTextChannelById,()->new IllegalStateException("Argument cannot be converted to message channel"));
	}
	
	private void requireInGuild() {
		if(!msg.isFromGuild()) {
			throw new IllegalStateException("Cannot get member if user is not in guild");
		}
	}
	
	private <T,E extends Exception> T getAsMentionedEntityOrThrowIfNotExist(Pattern pattern,Function<String,T> converter,Supplier<E> toThrow) throws E{
		T entity = getAsMentionedEntity(pattern,converter);
		if(entity==null) {
			throw toThrow.get();
		}
		return entity;
	}
	
	private <T> T getAsMentionedEntity(Pattern pattern,Function<String,T> converter){
		Matcher matcher = pattern.matcher(text);
		if(matcher.matches()) {
			return converter.apply(matcher.group(1));
		}
		return converter.apply(text);
	}

	@Override
	public ChannelType getChannelType() {
		return msg.getChannelType();
	}
}
