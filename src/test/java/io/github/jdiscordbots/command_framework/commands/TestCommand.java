package io.github.jdiscordbots.command_framework.commands;

import io.github.jdiscordbots.command_framework.command.Command;
import io.github.jdiscordbots.command_framework.command.CommandEvent;
import io.github.jdiscordbots.command_framework.command.ICommand;

@Command("test")
public class TestCommand implements ICommand {
	@Override
	public void action(CommandEvent event) {
		event.getChannel().sendMessage("test").queue();
	}

	@Override
	public String help() {
		return "Testing purposes";
	}
}
