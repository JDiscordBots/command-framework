package io.github.jdiscordbots.command_framework;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import io.github.jdiscordbots.command_framework.command.CommandEvent;
import io.github.jdiscordbots.command_framework.command.text.MessageArgument;
import io.github.jdiscordbots.command_framework.command.text.MessageCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

final class CommandParser
{
	static final Pattern SPACE_PATTERN=Pattern.compile("\\s+");
	private CommandParser()
	{
		/* Prevent instantiation */
	}

	static CommandContainer parse(final CommandFramework framework, final MessageReceivedEvent event, final String prefix)
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

		final CommandEvent commandEvent = new MessageCommandEvent(framework, event, split.stream().map(str->new MessageArgument(event.getMessage(), str)).collect(Collectors.toList()));
		return new CommandContainer(invoke, commandEvent);
	}
}
