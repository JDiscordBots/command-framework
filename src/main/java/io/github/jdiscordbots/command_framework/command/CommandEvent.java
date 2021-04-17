package io.github.jdiscordbots.command_framework.command;

import io.github.jdiscordbots.command_framework.CommandFramework;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.requests.RestAction;

import java.util.List;

import javax.annotation.CheckReturnValue;

/**
 * Represents a command that is executed and allows to take actions in response.
 */
public interface CommandEvent {

	/**
	 * Gets the {@link CommandFramework} instance managing {@link CommandEvent}.
	 * @return the {@link CommandFramework}
	 */
	CommandFramework getFramework();

	/**
	 * Gets a {@link List} containing all {@link Argument}s supplied to this command.
	 * The order of arguments is preserved.
	 * @return a {@link List} with all {@link Argument}s the command is invoked with
	 */
	List<Argument> getArgs();
	
	/**
	 * Gets the {@link Guild} the command is invoked in.
	 * @return the {@link Guild} the command is invoked in
	 */
	Guild getGuild();

	/**
	 * Gets the {@link JDA}-object used for receiving the command.
	 * @return the {@link JDA} object used for receiving the command
	 */
	JDA getJDA();

	/**
	 * Gets the {@link User} object representing the user invoking the command.
	 * @return the {@link User} invoking the command
	 */
	User getAuthor();

	/**
	 * Gets the {@link Member} object representing the member invoking the command.
	 * @return the {@link Member} invoking the command
	 */
	Member getMember();

	/**
	 * Gets the {@link MessageChannel} in which the command was invoked.
	 * @return the {@link MessageChannel} where the command was invoked
	 */
	MessageChannel getChannel();
	
	/**
	 * Gets the currently logged in {@link User}.
	 * @return the {@link SelfUser}-object representing the current user
	 */
	SelfUser getSelfUser();

	
	/**
	 * Gets the currently logged in {@link User} as {@link Member}.
	 * @return the {@link Member}-object representing the current user in the {@link Guild} the command was invoked
	 */
	Member getSelfMember();
	
	/**
	 * Gets the id of the invocation of the command represented as a {@link String}.
	 * @return the {@link String} id of the invocation of the command
	 */
	String getId();
	
	/**
	 * Gets the id of the invocation of the command represented as a <code>long</code>.
	 * @return the <code>long</code> id of the invocation of the command
	 */
	long getIdLong();
	
	/**
	 * Gets the {@link PrivateChannel} object is the command was invoked in a private channel.
	 * @return the {@link PrivateChannel} object
	 * @throws IllegalStateException if the command was not sent in a {@link PrivateChannel}
	 */
	PrivateChannel getPrivateChannel();
	
	/**
	 * Gets the {@link Message} associated with the invoked command.
	 * This message may not be sent the moment the command has been invoked
	 * @return the {@link Message} associated with the invoked command
	 */
	Message getMessage();
	
	/**
	 * Sends a message in response to the invoked command.
	 * @param message the message to send as {@link String}
	 * @return a {@link RestAction} that can be used to actually send and react to sending this message
	 */
	@CheckReturnValue
	RestAction<Message> reply(String message);

	/**
	 * Sends a message in response to the invoked command.
	 * @param message the message to send as {@link MessageEmbed}
	 * @return a {@link RestAction} that can be used to actually send and react to sending this message
	 */
	@CheckReturnValue
	RestAction<Message> reply(MessageEmbed message);
	
	/**
	 * Sends a message in response to the invoked command.
	 * @param message the message to send as {@link Message}
	 * @return a {@link RestAction} that can be used to actually send and react to sending this message
	 */
	@CheckReturnValue
	RestAction<Message> reply(Message message);
	
	/**
	 * deletes the message associated with the command
	 * @return
	 */
	@CheckReturnValue
	RestAction<Void> deleteOriginalMessage();
	
}
