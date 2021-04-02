package io.github.jdiscordbots.command_framework.command;

import net.dv8tion.jda.api.entities.AbstractChannel;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.Command.OptionType;

public interface Argument {

	boolean getAsBoolean();

	OptionType getType();

	long getAsLong();

	Member getAsMember();

	User getAsUser();

	AbstractChannel getAsChannel();

	GuildChannel getAsGuildChannel();

	PrivateChannel getAsPrivateChannel();

	MessageChannel getAsMessageChannel();

	ChannelType getChannelType();

	String getAsString();

	Role getAsRole();

	int getAsInt();

}