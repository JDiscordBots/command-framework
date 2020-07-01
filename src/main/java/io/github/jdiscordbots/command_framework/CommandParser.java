package io.github.jdiscordbots.command_framework;

import io.github.jdiscordbots.command_framework.command.CommandEvent;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class CommandParser {
	public static final Pattern SPACE_PATTERN = Pattern.compile("\\s+");
	private final CommandFramework framework;

	public CommandParser(CommandFramework framework) {
		this.framework = framework;
	}

	public CommandContainer parse(GuildMessageReceivedEvent event, String prefix) {
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

		final CommandEvent commandEvent = new CommandEvent(this.framework, event, split);
		return new CommandContainer(invoke, commandEvent);
	}

	/**
	 * CommandContainer
	 */
	static final class CommandContainer {
		public final String invoke;
		public final List<String> args;
		public final CommandEvent event;

		/**
		 * Construct a new Container by given command invoke and -event
		 *
		 * @param invoke name/invoke of command
		 * @param event  {@link io.github.jdiscordbots.command_framework.command.CommandEvent CommandEvent}
		 */
		public CommandContainer(String invoke, CommandEvent event) {
			this.invoke = invoke;
			this.args = event.getArgs();
			this.event = event;
		}
	}
}
