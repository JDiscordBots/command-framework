package io.github.jdiscordbots.command_framework.command.text;

import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.github.jdiscordbots.command_framework.command.Argument;
import net.dv8tion.jda.api.entities.AbstractChannel;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Command.OptionType;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;

public class MessageArgument implements Argument{
	
	private static final Pattern USER_FORMAT=Pattern.compile("<@!?(\\\\d+)>");
	private static final Pattern ROLE_FORMAT=Pattern.compile("<@&(\\\\d+)>");
	private static final Pattern CHANNEL_FORMAT=Pattern.compile("<#(\\\\d+)>");
	
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
		if(!msg.isFromGuild()) {
			return null;
		}
		return getAsMentionedEntity(ROLE_FORMAT, msg.getGuild()::getRoleById);
	}

	@Override
	public int getAsInt() {
		return Integer.parseInt(text);
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
		if(!msg.isFromGuild()) {
			return null;
		}
		return getAsMentionedEntity(USER_FORMAT, msg.getGuild()::getMemberById);
	}

	@Override
	public User getAsUser() {
		return getAsMentionedEntity(USER_FORMAT, msg.getJDA()::getUserById);
	}

	@Override
	public AbstractChannel getAsChannel() {
		AbstractChannel channel = getAsGuildChannel();
		if(channel==null) {
			channel=getAsChannel();
		}
		return channel;
	}

	@Override
	public GuildChannel getAsGuildChannel() {
		if(!msg.isFromGuild()) {
			return null;
		}
		return getAsMentionedEntity(CHANNEL_FORMAT, msg.getGuild()::getGuildChannelById);
	}

	@Override
	public PrivateChannel getAsPrivateChannel() {
		return getAsMentionedEntity(CHANNEL_FORMAT, msg.getJDA()::getPrivateChannelById);
	}

	@Override
	public MessageChannel getAsMessageChannel() {
		return getAsMentionedEntity(CHANNEL_FORMAT, msg.getJDA()::getTextChannelById);
	}
	
	private <T> T getAsMentionedEntity(Pattern pattern,Function<String,T> converter){
		Matcher matcher = pattern.matcher(text);
		if(matcher.matches()) {
			return converter.apply(matcher.group());
		}
		return null;
	}

	@Override
	public ChannelType getChannelType() {
		AbstractChannel channel = getAsChannel();
		return channel==null?ChannelType.UNKNOWN:channel.getType();
	}
}
