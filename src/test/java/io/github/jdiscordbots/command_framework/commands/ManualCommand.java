package io.github.jdiscordbots.command_framework.commands;

import java.util.Collections;
import java.util.List;

import io.github.jdiscordbots.command_framework.command.ArgumentTemplate;
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

	@Override
	public List<ArgumentTemplate> getExpectedArguments() {
		return Collections.emptyList();
	}
}
