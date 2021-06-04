package io.github.jdiscordbots.command_framework.command.slash;

import io.github.jdiscordbots.command_framework.command.Argument;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;

/**
 * An {@link Argument} representing an argument of a slash command
 */
public final class SlashArgument implements Argument
{
	private final OptionMapping option;
	
	public SlashArgument(OptionMapping option)
	{
		this.option=option;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean getAsBoolean()
	{
		
		return option.getAsBoolean();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public OptionType getType()
	{
		return option.getType();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getAsLong()
	{
		return option.getAsLong();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Member getAsMember()
	{
		return option.getAsMember();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public User getAsUser()
	{
		return option.getAsUser();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public GuildChannel getAsGuildChannel()
	{
		return option.getAsGuildChannel();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public MessageChannel getAsMessageChannel()
	{
		return option.getAsMessageChannel();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ChannelType getChannelType()
	{
		return option.getChannelType();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getAsString()
	{
		return option.getAsString();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Role getAsRole()
	{
		return option.getAsRole();
	}
}
