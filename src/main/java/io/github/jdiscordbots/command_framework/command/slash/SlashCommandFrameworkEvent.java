package io.github.jdiscordbots.command_framework.command.slash;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.github.jdiscordbots.command_framework.CommandFramework;
import io.github.jdiscordbots.command_framework.command.Argument;
import io.github.jdiscordbots.command_framework.command.ArgumentTemplate;
import io.github.jdiscordbots.command_framework.command.CommandEvent;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.MessageType;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.entities.SelfUser;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.entities.SystemMessage;

/**
 * A {@link CommandEvent} representing an executed slash command.
 */
public final class SlashCommandFrameworkEvent implements CommandEvent
{
	private final CommandFramework framework;
	private final SlashCommandInteractionEvent event;
	private final List<Argument> args;
	private AtomicReference<Message> firstMessage=new AtomicReference<>();

	public SlashCommandFrameworkEvent(CommandFramework framework, SlashCommandInteractionEvent event)
	{
		this.framework=framework;
		this.event = event;
		args=null;
	}
	
	public SlashCommandFrameworkEvent(CommandFramework framework, SlashCommandInteractionEvent event, Collection<ArgumentTemplate> expectedArgs)
	{
		this.framework=framework;
		this.event = event;
		args = Collections.unmodifiableList(loadArgs(expectedArgs));
	}
	
	private List<Argument> loadArgs(Collection<ArgumentTemplate> expectedArgs)
	{
		return Stream.concat(createSubcommandDataStream(), createActualArgumentsStream(expectedArgs))
				.collect(Collectors.toList());
	}
	
	private Stream<Argument> createSubcommandDataStream()
	{
		return Stream.of(event.getSubcommandGroup(), event.getSubcommandName())
				.filter(Objects::nonNull)
				.map(SlashCommandFrameworkEvent::createOptionDataFromString);
	}
	
	private Stream<Argument> createActualArgumentsStream(Collection<ArgumentTemplate> expectedArgs)
	{
		return expectedArgs.stream().map(ArgumentTemplate::getName).map(event::getOption).filter(Objects::nonNull)
				.map(SlashArgument::new);
	}

	private static Argument createOptionDataFromString(String in)
	{
		return new SlashArgument(new OptionMapping(new DataObject(creationOptionDataMap(in)) {}, null));
	}
	
	private static Map<String, Object> creationOptionDataMap(String in)
	{
		Map<String, Object> ret=new HashMap<>();
		ret.put("value", in);
		ret.put("name", "subcommand");
		return ret;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public CommandFramework getFramework()
	{
		return framework;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Argument> getArgs()
	{
		return args;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Guild getGuild()
	{
		return event.getGuild();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public JDA getJDA()
	{
		return event.getJDA();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public User getAuthor()
	{
		return event.getUser();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Member getMember()
	{
		return event.getMember();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public MessageChannel getChannel()
	{
		return event.getMessageChannel();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SelfUser getSelfUser()
	{
		return getJDA().getSelfUser();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Member getSelfMember()
	{
		return event.getGuild().getSelfMember();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getId()
	{
		return event.getInteraction().getId();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PrivateChannel getPrivateChannel()
	{
		return event.getPrivateChannel();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Message getMessage()
	{
		Message msg = firstMessage.get();
		if (msg == null)
		{
			return new SystemMessage(getIdLong(), getChannel(), MessageType.SLASH_COMMAND, null, true, false, null,
					null, false, false, getArgs().stream().map(Argument::getAsString).collect(Collectors.joining(" ")),
					"", getAuthor(), getMember(), null, null, Collections.emptyList(), Collections.emptyList(),
					Collections.emptyList(), Collections.emptyList(), 0);
		}
		return msg;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getIdLong()
	{
		return event.getInteraction().getIdLong();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public RestAction<Message> reply(String message)
	{
		return event.getHook().sendMessage(message).map(this::saveMessageIfFirst);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public RestAction<Message> reply(Message message)
	{
		return event.getHook().sendMessage(message).map(this::saveMessageIfFirst);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public RestAction<Message> reply(MessageEmbed message)
	{
		return event.getHook().sendMessageEmbeds(message).map(this::saveMessageIfFirst);
	}
	
	private Message saveMessageIfFirst(Message msg)
	{
		firstMessage.compareAndSet(null, msg);
		return msg;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public RestAction<Void> deleteOriginalMessage()
	{
		return event.getHook().deleteOriginal();
	}
	
	public SlashCommandInteractionEvent getEvent()
	{
		return event;
	}
}
