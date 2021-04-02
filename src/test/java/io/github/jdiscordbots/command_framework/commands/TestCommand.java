package io.github.jdiscordbots.command_framework.commands;

import java.util.Arrays;
import java.util.List;

import io.github.jdiscordbots.command_framework.command.ArgumentTemplate;
import io.github.jdiscordbots.command_framework.command.Command;
import io.github.jdiscordbots.command_framework.command.CommandEvent;
import io.github.jdiscordbots.command_framework.command.ICommand;
import net.dv8tion.jda.api.entities.Command.OptionType;

@Command({"test"})
public class TestCommand implements ICommand
{
	@Override
	public void action(CommandEvent event) {
		event.getChannel().sendMessage("test"+event.getArgs().get(0)).queue();
	}

	@Override
	public String help() {
		return "Testing purposes";
	}

	@Override
	public List<ArgumentTemplate> getExpectedArguments() {
		return Arrays.asList(new ArgumentTemplate(OptionType.CHANNEL, "channel", "the channel to get the ID",true));
	}
}
