package io.github.jdiscordbots.command_framework;

import io.github.jdiscordbots.command_framework.command.Argument;
import io.github.jdiscordbots.command_framework.command.CommandEvent;
import io.github.jdiscordbots.command_framework.command.ICommand;
import io.github.jdiscordbots.command_framework.command.slash.SlashCommandFrameworkEvent;
import io.github.jdiscordbots.command_framework.command.text.MessageCommandEvent;
import io.github.jdiscordbots.command_framework.command.text.MessageArgument;
import io.github.jdiscordbots.command_framework.utils.PermissionUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
		final CommandEvent event = commandContainer.event;
		
		if (commands.containsKey(commandContainer.invoke.toLowerCase()))
		{
			final ICommand command = commands.get(commandContainer.invoke.toLowerCase());
			final boolean canExecute = command.allowExecute(commandContainer.event);

			if(event instanceof SlashCommandFrameworkEvent) {
				((SlashCommandFrameworkEvent) event).loadArguments(command.getExpectedArguments());
			}
			
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
					event.reply("Error:\n```" + e.getMessage() + "\n```");
				}
			}
			else
			{
				event.reply("You're not allowed to use this command!");
			}
		}
		else if (commandContainer.event.getFramework().isUnknownCommand())
		{
			// TODO: 09.06.2020 Custom unknown command message 
			if(commandContainer.event.getFramework().getUnknownCommandConsumer() == null)
			{
				final EmbedBuilder eb = new EmbedBuilder()
						.setColor(Color.red)
						.setTitle("Unknown command")
						.setDescription("See `" + commandContainer.event.getFramework().getPrefix() + "help` for more information!");
					
					event.reply(eb.build());
			}
			else
			{
				commandContainer.event.getFramework().getUnknownCommandConsumer().accept(event);
			}
		}
	}

	static final class CommandParser
	{
		static final Pattern SPACE_PATTERN=Pattern.compile("\\s+");
		private CommandParser()
		{
			/* Prevent instantiation */
		}

		static CommandContainer parse(final MessageReceivedEvent event, final String prefix)
		{
			String raw = event.getMessage().getContentRaw();

			final String beheaded = raw.replaceFirst(Pattern.quote(prefix), "");
			
			final String[] splitBeheaded = SPACE_PATTERN.split(beheaded.trim());
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

					split.add(split.remove(split.size() - 1)+" "+s);
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

			final CommandEvent commandEvent = new MessageCommandEvent(event, split.stream().map(str->new MessageArgument(event.getMessage(), str)).collect(Collectors.toList()));
			return new CommandContainer(invoke, commandEvent);
		}
	}

	public static final class CommandContainer
	{
		public final String invoke;
		public final List<Argument> args;
		public final CommandEvent event;

		public CommandContainer(String invoke, CommandEvent event)
		{
			this.invoke = invoke;
			this.args = event.getArgs();
			this.event = event;
		}
	}
}
