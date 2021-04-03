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
import net.dv8tion.jda.api.entities.Guild;

/**
 * Represents an argument of a command.
 */
public interface Argument {

	/**
	 * Gets the argument as a {@code boolean}.
	 * @return the argument as a {@code boolean} or {@code false} if the argument cannot be converted to a {@code boolean}
	 */
	boolean getAsBoolean();

	/**
	 * Gets the type of the argument.
	 * <br/>
	 * The argument type is {@link OptionType#STRING} for message arguments
	 * @return the type of the argument
	 */
	OptionType getType();

	/**
	 * Gets the argument as a {@code long}.
	 * @return the argument as a {@code long}
	 * @throws NumberFormatException if the argument cannot be converted to a {@code long}
	 */
	long getAsLong();
	
	/**
	 * Gets the argument as a {@link Member}.
	 * @return the argument as a {@link Member}
	 * @throws IllegalStateException if the argument cannot be converted to a {@link Member} or the {@link Member} was not found
	 * @throws NumberFormatException if the argument format is invalid
	 */
	Member getAsMember();

	/**
	 * Gets the argument as a {@link User}.
	 * @return the argument as a {@link User}
	 * @throws IllegalStateException if the argument cannot be converted to a {@link User} or the {@link User} was not found
	 * @throws NumberFormatException if the argument format is invalid
	 */
	User getAsUser();

	/**
	 * Gets the argument as an {@link AbstractChannel}.
	 * @return the argument as an {@link AbstractChannel}
	 * @throws IllegalStateException if the argument cannot be converted to an {@link AbstractChannel} or the {@link AbstractChannel} was not found
	 * @throws NumberFormatException if the argument format is invalid
	 */
	AbstractChannel getAsChannel();

	/**
	 * Gets the argument as a {@link GuildChannel}.
	 * @return the argument as a {@link GuildChannel}
	 * @throws IllegalStateException if the argument cannot be converted to a {@link GuildChannel} or the {@link GuildChannel} was not found in the {@link Guild} the message was sent
	 * @throws NumberFormatException if the argument format is invalid
	 */
	GuildChannel getAsGuildChannel();
	
	/**
	 * Gets the argument as a {@link PrivateChannel}.
	 * @return the argument as a {@link PrivateChannel}
	 * @throws IllegalStateException if the argument cannot be converted to a {@link PrivateChannel} or the {@link PrivateChannel} was not found
	 * @throws NumberFormatException if the argument format is invalid
	 */
	PrivateChannel getAsPrivateChannel();

	/**
	 * Gets the argument as a {@link MessageChannel}.
	 * @return the argument as a {@link MessageChannel}
	 * @throws IllegalStateException if the argument cannot be converted to a {@link MessageChannel} or the {@link MessageChannel} was not found
	 * @throws NumberFormatException if the argument format is invalid
	 */
	MessageChannel getAsMessageChannel();

	/**
	 * Gets the channel type of the argument if it belongs to a channel
	 * @return the {@link ChannelType} of the {@link AbstractChannel} of the argument
	 * @throws IllegalStateException if the argument cannot be converted to an {@link AbstractChannel} or the {@link AbstractChannel} was not found
	 * @throws NumberFormatException if the argument format is invalid
	 */
	ChannelType getChannelType();

	/**
	 * gets the argument as a {@link String}
	 * @return the argument as a {@link String}
	 */
	String getAsString();

	/**
	 * Gets the argument as a {@link Role}.
	 * @return the argument as a {@link Role}
	 * @throws IllegalStateException if the argument cannot be converted to a {@link Role} or the {@link Role} was not found in the {@link Guild} the message was sent
	 * @throws NumberFormatException if the argument format is invalid
	 */
	Role getAsRole();

}