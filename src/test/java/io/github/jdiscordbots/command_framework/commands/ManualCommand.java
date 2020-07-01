package io.github.jdiscordbots.command_framework.commands;

import io.github.jdiscordbots.command_framework.command.CommandEvent;
import io.github.jdiscordbots.command_framework.command.ICommand;

public class ManualCommand implements ICommand {
	@Override
	public void action(CommandEvent event) {
		event.reply("Manual added command");
	}

	@Override
	public String help() {
		return "manual added command";
	}
}
