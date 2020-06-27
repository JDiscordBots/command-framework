package io.github.jdiscordbots.command_framework.commands;

import io.github.jdiscordbots.command_framework.command.ICommand;
import io.github.jdiscordbots.command_framework.command.Command;
import io.github.jdiscordbots.command_framework.command.CommandEvent;

@Command({"framework", "cmd", "commandframework"})
public class FrameworkCommand implements ICommand
{
	@Override
	public void action(CommandEvent event)
	{
		event.getChannel().sendMessage("Framework: Command framework by JDiscordBots").queue();
	}

	@Override
	public boolean allowExecute(CommandEvent event) {
		return false;
	}

	@Override
	public String help() {
		return "Testing purposes";
	}
}