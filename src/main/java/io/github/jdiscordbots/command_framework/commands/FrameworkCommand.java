package io.github.jdiscordbots.command_framework.commands;

import io.github.jdiscordbots.command_framework.ICommand;
import io.github.jdiscordbots.command_framework.Command;
import io.github.jdiscordbots.command_framework.CommandEvent;

import java.util.Arrays;

@Command({"framework", "cmd", "commandframework"})
public class FrameworkCommand implements ICommand
{
	@Override
	public void action(CommandEvent event)
	{
		event.getChannel().sendMessage("Framework: Command framework by Noobi#0001").queue();
	}

	@Override
	public boolean allowExecute(CommandEvent event) {
		return Arrays.stream(event.getFramework().getOwners()).anyMatch(s -> s.equals(event.getAuthor().getId()));
	}
}