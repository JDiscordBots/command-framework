package io.github.jdiscordbots.command_framework.commands;

import java.util.Collections;
import java.util.List;

import io.github.jdiscordbots.command_framework.command.ArgumentTemplate;
import io.github.jdiscordbots.command_framework.command.Command;
import io.github.jdiscordbots.command_framework.command.CommandEvent;
import io.github.jdiscordbots.command_framework.command.ICommand;

@Command({"framework", "cmd", "commandframework"})
public class FrameworkCommand implements ICommand
{
	@Override
	public void action(CommandEvent event)
	{
		event.reply("Framework: Command framework by JDiscordBots").queue();
	}

	@Override
	public boolean allowExecute(CommandEvent event) {
		return false;
	}

	@Override
	public String help() {
		return "show information about framework";
	}

	@Override
	public List<ArgumentTemplate> getExpectedArguments() {
		return Collections.emptyList();
	}
}