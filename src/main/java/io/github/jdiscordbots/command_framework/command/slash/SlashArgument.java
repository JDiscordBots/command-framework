package io.github.jdiscordbots.command_framework.command.slash;

import io.github.jdiscordbots.command_framework.command.Argument;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Command.OptionType;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent.OptionData;

public final class SlashArgument implements Argument {
	
	private OptionData option;
	
	public SlashArgument(OptionData option) {
		this.option=option;
	}
	
	@Override
	public boolean getAsBoolean() {
		return option.getAsBoolean();
	}

	@Override
	public OptionType getType() {
		return option.getType();
	}

	@Override
	public long getAsLong() {
		return option.getAsLong();
	}

	@Override
	public Member getAsMember() {
		return option.getAsMember();
	}

	@Override
	public User getAsUser() {
		return option.getAsUser();
	}

	@Override
	public GuildChannel getAsGuildChannel() {
		return option.getAsGuildChannel();
	}

	@Override
	public MessageChannel getAsMessageChannel() {
		return option.getAsMessageChannel();
	}

	@Override
	public ChannelType getChannelType() {
		return option.getChannelType();
	}

	@Override
	public String getAsString() {
		return option.getAsString();
	}

	@Override
	public Role getAsRole() {
		return option.getAsRole();
	}
}
