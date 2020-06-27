package io.github.jdiscordbots.command_framework;

import io.github.jdiscordbots.command_framework.command.CommandEvent;
import io.github.jdiscordbots.command_framework.command.ICommand;
import io.github.jdiscordbots.command_framework.utils.PermissionUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.regex.Pattern;

/**
 * CommandHandler of Command system
 */
final class CommandHandler {
	private static final Map<String, ICommand> commands = new HashMap<>();
	private static final Logger LOG = LoggerFactory.getLogger(CommandHandler.class);

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
	 * @param name invoke of command
	 * @param command {@link io.github.jdiscordbots.command_framework.command.ICommand Command}
	 */
	static void addCommand(String name, ICommand command) {
		commands.put(name, command);
	}

	/**
	 * handle commands
	 *
	 * @param commandContainer {@link io.github.jdiscordbots.command_framework.CommandHandler.CommandParser CommandContainer}
	 */
	public static void handle(final CommandContainer commandContainer) {
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

	/**
	 * CommandParser
	 */
	static final class CommandParser {
		static final Pattern SPACE_PATTERN = Pattern.compile("\\s+");

		/**
		 * Private constructor for utility class
		 */
		private CommandParser() {
			/* Prevent instantiation */
		}

		/**
		 * Parse GuildMessageReceivedEvent and Prefix to CommandContainer
		 *
		 * @param event incomming {@link net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent GuildMessageReceivedEvent}
		 * @param prefix prefix
		 * @return {@link io.github.jdiscordbots.command_framework.CommandHandler.CommandContainer CommandContainer}
		 */
		static CommandContainer parse(final GuildMessageReceivedEvent event, final String prefix) {
			final Member member = event.getMember();

			if (member == null)
				throw new IllegalStateException("Member is null");

			String raw = event.getMessage().getContentRaw();

			if (!member.hasPermission(event.getChannel(), Permission.MESSAGE_MENTION_EVERYONE))
				raw = raw.replace("@everyone", "@\u200Beveryone").replace("@here", "@\u200Bhere");

			final String beheaded = raw.replaceFirst(Pattern.quote(prefix), "");

			final String[] splitBeheaded = SPACE_PATTERN.split(beheaded.trim());
			final String invoke = splitBeheaded[0];
			final List<String> split = new ArrayList<>();
			boolean inQuote = false;

			for (int i = 1; i < splitBeheaded.length; i++) {
				String s = splitBeheaded[i];

				if (inQuote) {
					if (s.endsWith("\"")) {
						inQuote = false;
						s = s.substring(0, s.length() - 1);
					}

					split.add(split.remove(split.size() - 1).concat(" ").concat(s));
				} else {
					if (s.startsWith("\"") && !s.endsWith("\"")) {
						inQuote = true;
						s = s.substring(1);
					}

					split.add(s);
				}
			}

			final CommandEvent commandEvent = new CommandEvent(event, split);
			return new CommandContainer(invoke, commandEvent);
		}
	}

	/**
	 * CommandContainer
	 */
	public static final class CommandContainer {
		public final String invoke;
		public final List<String> args;
		public final CommandEvent event;

		/**
		 * Construct a new Container by given command invoke and -event
		 *
		 * @param invoke name/invoke of command
		 * @param event {@link io.github.jdiscordbots.command_framework.command.CommandEvent CommandEvent}
		 */
		public CommandContainer(String invoke, CommandEvent event) {
			this.invoke = invoke;
			this.args = event.getArgs();
			this.event = event;
		}
	}
}
