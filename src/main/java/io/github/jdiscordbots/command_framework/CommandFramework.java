package io.github.jdiscordbots.command_framework;

import io.github.jdiscordbots.command_framework.command.Command;
import io.github.jdiscordbots.command_framework.command.ICommand;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class CommandFramework
{
	private static final Logger LOG=LoggerFactory.getLogger(CommandFramework.class);
	
	private static CommandFramework instance;

	private Consumer<TextChannel> unknownCommandConsumer = null;
	private String prefix = "!";
	private String[] owners = {};
	private boolean mentionPrefix = true;
	private boolean unknownCommand = true;

	public CommandFramework()
	{
		this(getCallerPackageName());
	}

	private static String getCallerPackageName()
	{
		try
		{
			StackTraceElement trace = new Throwable().getStackTrace()[2];
			String clName = trace.getClassName();
			return Thread.currentThread().getContextClassLoader().loadClass(clName).getPackage().getName();
		}
		catch (ClassNotFoundException e)
		{
			throw new IllegalStateException("caller class not available");
		}
	}
	
	public CommandFramework(String commandsPackagePath)
	{
		instance = this;

		final Reflections reflections = new Reflections(commandsPackagePath);

		addCommands(reflections);
	}

	private static void addCommands(Reflections reflections)
	{
		addAction(reflections, (cmdAsAnnotation, annotatedAsObject) ->
		{
			final Command cmdAsBotCommand = (Command) cmdAsAnnotation;
			final ICommand cmd = (ICommand) annotatedAsObject;

			for (String alias : cmdAsBotCommand.value())
				CommandHandler.addCommand(alias.toLowerCase(), cmd);
		});
	}
	private static void addAction(Reflections reflections, BiConsumer<Annotation, Object> function)
	{
		for (Class<?> cl : reflections.getTypesAnnotatedWith(Command.class,true))
		{
			try
			{
				final Object annotatedAsObject = cl.getDeclaredConstructor().newInstance();
				final Annotation cmdAsAnnotation = cl.getAnnotation((Class<? extends Annotation>) Command.class);

				function.accept(cmdAsAnnotation, annotatedAsObject);
			}
			catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e)
			{
				if(LOG.isErrorEnabled())
					LOG.error("An exception occurred trying to create and register an instance of the class {}.", cl.getCanonicalName(), e);
			}
		}
	}

	public CommandFramework setPrefix(String prefix)
	{
		this.prefix = prefix;
		return this;
	}

	public CommandFramework setOwners(String owner, String... owners)
	{

		final List<String> ownersList = new ArrayList<>(Arrays.asList(owners));

		ownersList.add(owner);

		this.owners = ownersList.toArray(new String[0]);
		return this;
	}

	public CommandFramework setMentionPrefix(boolean mentionPrefix) {
		this.mentionPrefix = mentionPrefix;
		return this;
	}

	public CommandFramework setUnknownCommand(boolean unknownCommand) {
		this.unknownCommand = unknownCommand;
		return this;
	}

	public boolean isUnknownCommand() {
		return unknownCommand;
	}

	public ListenerAdapter build()
	{
		LOG.debug("Listening to following commands ({}):\n{}", CommandHandler.getCommands().size(), String.join(", ", CommandHandler.getCommands().keySet()));
		return new CommandListener(this);
	}

	public static CommandFramework getInstance() {
		return instance;
	}

	public String getPrefix() {
		return prefix;
	}

	public String[] getOwners() {
		return owners;
	}

	Consumer<TextChannel> getUnknownCommandConsumer() {
		return unknownCommandConsumer;
	}

	public CommandFramework setUnknownMessage(Consumer<TextChannel> unknownCommandConsumer) {
		this.unknownCommandConsumer = unknownCommandConsumer;
		return this.setUnknownCommand(true);
	}

	public boolean isMentionPrefix() {
		return mentionPrefix;
	}

	public CommandFramework setOwners(String[] owners)
	{
		return this.setOwners(owners[0], Arrays.copyOfRange(owners, 1, owners.length));
	}

	public Map<String, ICommand> getCommands()
	{
		return CommandHandler.getCommands();
	}

	private static final class CommandListener extends ListenerAdapter
	{
		private final String prefix;
		private final boolean mentionPrefix;

		public CommandListener(CommandFramework framework)
		{
			this(framework.getPrefix(), framework.isMentionPrefix());
		}

		// Can be removed
		private CommandListener(String prefix, boolean mentionPrefix)
		{
			this.prefix = prefix;
			this.mentionPrefix = mentionPrefix;
		}

		@Override
		public void onGuildMessageReceived(@Nonnull GuildMessageReceivedEvent event)
		{
			final Message message = event.getMessage();
			final String contentRaw = message.getContentRaw().trim();
			final String selfUserId = message.getJDA().getSelfUser().getId();
			final boolean containsMention = contentRaw.startsWith("<!@" + selfUserId + "> ")
				|| contentRaw.startsWith("<@" + selfUserId + "> ");

			if (message.getAuthor().isBot())
				return;

			if (this.mentionPrefix && containsMention)
			{
				CommandHandler.handle(CommandHandler.CommandParser.parse(event, contentRaw.split("\\s+")[0] + " "));
				return;
			}

			if (message.getContentDisplay().startsWith(this.prefix))
				CommandHandler.handle(CommandHandler.CommandParser.parse(event, prefix));
		}
	}
}
