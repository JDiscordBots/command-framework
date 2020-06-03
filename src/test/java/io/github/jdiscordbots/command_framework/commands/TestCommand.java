package io.github.jdiscordbots.command_framework.commands;

import io.github.jdiscordbots.command_framework.Command;
import io.github.jdiscordbots.command_framework.CommandEvent;
import io.github.jdiscordbots.command_framework.ICommand;

@Command("test")
public class TestCommand implements ICommand
{
	@Override
	public void action(CommandEvent event) {
		event.getChannel().sendMessage("test").queue();
	}
}
