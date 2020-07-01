package io.github.jdiscordbots.command_framework;

import io.github.jdiscordbots.command_framework.command.ICommand;
import io.github.jdiscordbots.command_framework.utils.PermissionUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * CommandHandler of Command system
 */
final class CommandHandler {
	private static final Logger LOG = LoggerFactory.getLogger(CommandHandler.class);
	private static final Map<String, ICommand> commands = new HashMap<>();

	/**
	 * Private constructor for utility class
	 */
	private CommandHandler() {
		/* Prevent instantiation */
	}

	/**
	 * Get registered commands
	 *
	 * @return {@link java.util.Map Map}
	 */
	static Map<String, ICommand> getCommands() {
		return Collections.unmodifiableMap(commands);
	}

	/**
	 * Add command to commands map
	 *
	 * @param name    invoke of command
	 * @param command {@link io.github.jdiscordbots.command_framework.command.ICommand Command}
	 */
	static void addCommand(String name, ICommand command) {
		commands.put(name, command);
	}

	/**
	 * handle commands
	 *
	 * @param commandContainer {@link io.github.jdiscordbots.command_framework.CommandParser.CommandContainer CommandContainer}
	 */
	public static void handle(final CommandParser.CommandContainer commandContainer) {
		final TextChannel channel = commandContainer.event.getChannel();

		if (commands.containsKey(commandContainer.invoke.toLowerCase())) {
			final ICommand command = commands.get(commandContainer.invoke.toLowerCase());
			final boolean canExecute = command.allowExecute(commandContainer.event);

			/* Check permission and allow all commands to Owners */
			if (canExecute || PermissionUtils.checkOwner(commandContainer.event)) {
				try {
					command.action(commandContainer.event);
				} catch (RuntimeException e) {
					LOG.error("The command {} was executed but an error occurred.", commandContainer.invoke, e);
					channel.sendMessage("Error:\n```" + e.getMessage() + "\n```").queue();
				}
			} else {
				channel.sendMessage("You're not allowed to use this command!").queue();
			}
		} else if (commandContainer.event.getFramework().isUnknownCommand()) {
			if (commandContainer.event.getFramework().getOnUnknownCommandHandler() == null) {
				final EmbedBuilder eb = new EmbedBuilder()
					.setColor(Color.red)
					.setTitle("Unknown command")
					.setDescription("See `" + commandContainer.event.getFramework().getPrefix() + "help` for more information!");

				channel.sendMessage(eb.build()).queue();
			} else {
				commandContainer.event.getFramework().getOnUnknownCommandHandler().accept(channel);
			}
		}
	}
}
