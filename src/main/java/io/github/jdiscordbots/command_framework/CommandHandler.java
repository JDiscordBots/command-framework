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
import java.util.*;
import java.util.List;
import java.util.regex.Pattern;

final class CommandHandler
{
	private static final Map<String, ICommand> commands = new HashMap<>();
	private static final Logger LOG=LoggerFactory.getLogger(CommandHandler.class);

	private CommandHandler()
	{
		/* Prevent instantiation */
	}

	static Map<String, ICommand> getCommands()
	{
		return Collections.unmodifiableMap(commands);
	}

	static void addCommand(String name, ICommand command)
	{
		commands.put(name, command);
	}

	public static void handle(final CommandContainer commandContainer)
	{
		final TextChannel channel = commandContainer.event.getChannel();
		
		if (commands.containsKey(commandContainer.invoke.toLowerCase()))
		{
			final ICommand command = commands.get(commandContainer.invoke.toLowerCase());
			final boolean canExecute = command.allowExecute(commandContainer.event);

			/* Check permission and allow all commands to Owners */
			if (canExecute || PermissionUtils.checkOwner(commandContainer.event))
			{
				try
				{
					command.action(commandContainer.event);
				}
				catch (RuntimeException e)
				{
					LOG.error("The command {} was executed but an error occurred.", commandContainer.invoke, e);
					channel.sendMessage("Error:\n```" + e.getMessage() + "\n```").queue();
				}
			}
			else
			{
				channel.sendMessage("You're not allowed to use this command!").queue();
			}
		}
		else
		{
			// TODO: 09.06.2020 Custom unknown command message 
			if (commandContainer.event.getFramework().isUnknownCommand())
			{
				final EmbedBuilder eb = new EmbedBuilder()
					.setColor(Color.red)
					.setTitle("Unknown command")
					.setDescription("See `" + commandContainer.event.getFramework().getPrefix() + "help` for more information!");
				
				channel.sendMessage(eb.build()).queue();
			}
		}
	}

	static final class CommandParser
	{
		private CommandParser()
		{
			/* Prevent instantiation */
		}

		/*
		static CommandContainer parse(final GuildMessageReceivedEvent event)
		{
			return parse(event, CommandFramework.getInstance().getPrefix());
		}
		 */

		static CommandContainer parse(final GuildMessageReceivedEvent event, final String prefix)
		{
			final Member member = event.getMember();

			if (member == null)
				throw new IllegalStateException("Member is null");

			String raw = event.getMessage().getContentRaw();

			if (!member.hasPermission(event.getChannel(), Permission.MESSAGE_MENTION_EVERYONE))
				raw = raw.replace("@everyone", "@\u200Beveryone").replace("@here", "@\u200Bhere");

			final String beheaded = raw.replaceFirst(Pattern.quote(prefix), "");
			final String[] splitBeheaded = beheaded.trim().split("\\s+");
			final String invoke = splitBeheaded[0];
			final List<String> split = new ArrayList<>();
			boolean inQuote = false;

			for (int i = 1; i < splitBeheaded.length; i++)
			{
				String s = splitBeheaded[i];

				if (inQuote)
				{
					if (s.endsWith("\""))
					{
						inQuote = false;
						s = s.substring(0, s.length() - 1);
					}

					split.add(split.remove(split.size() - 1).concat(" ").concat(s));
				}
				else
				{
					if (s.startsWith("\"") && !s.endsWith("\""))
					{
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

	public static final class CommandContainer
	{
		public final String invoke;
		public final List<String> args;
		public final CommandEvent event;

		public CommandContainer(String invoke, CommandEvent event)
		{
			this.invoke = invoke;
			this.args = event.getArgs();
			this.event = event;
		}
	}
}
