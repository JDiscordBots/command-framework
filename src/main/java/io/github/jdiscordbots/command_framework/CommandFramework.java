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

/**
 * Nightdream's easy-to-use command system as a library
 */
public class CommandFramework {
	private static final Logger LOG = LoggerFactory.getLogger(CommandFramework.class);

	private static CommandFramework instance;

	private Consumer<TextChannel> onUnknownCommandHandler;
	private String prefix = "!";
	private String[] owners = {};
	private boolean mentionPrefix = true;
	private boolean unknownCommand = true;

	/**
	 * Constructs a new CommandFramework instance with the given Commands package
	 *
	 * @param commandsPackagePath path to package where commands are located, e.g. <code>com.example.bot.commands</code>
	 * @see CommandFramework#CommandFramework()
	 */
	public CommandFramework(String commandsPackagePath) {
		instance = this;

		final Reflections reflections = new Reflections(commandsPackagePath);

		addCommands(reflections);
	}

	/**
	 * Constructs a new CommandFramework instance with the caller-package
	 *
	 * @see io.github.jdiscordbots.command_framework.CommandFramework#CommandFramework(String)
	 */
	public CommandFramework() {
		this(getCallerPackageName());
	}

	/**
	 * Return package path of caller-class
	 *
	 * @return package path
	 */
	private static String getCallerPackageName() {
		try {
			StackTraceElement trace = new Throwable().getStackTrace()[2];
			String clName = trace.getClassName();
			return Thread.currentThread().getContextClassLoader().loadClass(clName).getPackage().getName();
		} catch (ClassNotFoundException ignored) {
			throw new IllegalStateException("caller class not available");
		}
	}

	/**
	 * Add Commands using Reflections
	 *
	 * @param reflections {@link org.reflections.Reflections Reflections}
	 */
	private static void addCommands(Reflections reflections) {
		addAction(reflections, (cmdAsAnnotation, annotatedAsObject) ->
		{
			final Command cmdAsBotCommand = (Command) cmdAsAnnotation;
			final ICommand cmd = (ICommand) annotatedAsObject;

			for (String alias : cmdAsBotCommand.value())
				CommandHandler.addCommand(alias.toLowerCase(), cmd);
		});
	}

	/**
	 * Instantiate Command-classes
	 *
	 * @param reflections {@link org.reflections.Reflections Reflections}
	 * @param function    {@link java.util.function.BiConsumer BiConsumer}
	 */
	private static void addAction(Reflections reflections, BiConsumer<Annotation, Object> function) {
		for (Class<?> cl : reflections.getTypesAnnotatedWith(Command.class, true)) {
			try {
				final Object annotatedAsObject = cl.getDeclaredConstructor().newInstance();
				final Annotation cmdAsAnnotation = cl.getAnnotation((Class<? extends Annotation>) Command.class);

				function.accept(cmdAsAnnotation, annotatedAsObject);
			} catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
				if (LOG.isErrorEnabled())
					LOG.error("An exception occurred trying to create and register an instance of the class {}.",
						cl.getCanonicalName(), e);
			}
		}
	}

	/**
	 * Get current instance of CommandFramework
	 * <p>
	 * NOTE: Currently singleton-system (only one instance possible)
	 *
	 * @return {@link io.github.jdiscordbots.command_framework.CommandFramework CommandFramework}
	 */
	public static CommandFramework getInstance() {
		return instance;
	}

	/**
	 * Set privileged user ids
	 *
	 * @param owner  owner id
	 * @param owners more owner ids
	 * @return updated {@link io.github.jdiscordbots.command_framework.CommandFramework CommandFramework} for chaining convenience
	 * @see io.github.jdiscordbots.command_framework.CommandFramework#getOwners()
	 */
	public CommandFramework setOwners(String owner, String... owners) {
		final List<String> ownersList = new ArrayList<>(Arrays.asList(owners));

		ownersList.add(owner);

		this.owners = ownersList.toArray(new String[0]);
		return this;
	}

	/**
	 * Return whether the unknown command message is enabled or not
	 *
	 * @return <code>true</code> if unknown command message is enabled, otherwise <code>false</code>
	 * @see io.github.jdiscordbots.command_framework.CommandFramework#setUnknownCommand(boolean)
	 */
	public boolean isUnknownCommand() {
		return unknownCommand;
	}

	/**
	 * Enable/disable unknown command message
	 *
	 * @param unknownCommand state of unknown command message
	 * @return updated {@link io.github.jdiscordbots.command_framework.CommandFramework CommandFramework} for chaining convenience
	 * @see io.github.jdiscordbots.command_framework.CommandFramework#isUnknownCommand()
	 */
	public CommandFramework setUnknownCommand(boolean unknownCommand) {
		this.unknownCommand = unknownCommand;
		return this;
	}

	/**
	 * Get the prefix
	 *
	 * @return prefix of current {@link io.github.jdiscordbots.command_framework.CommandFramework CommandFramework} instance
	 * @see io.github.jdiscordbots.command_framework.CommandFramework#setPrefix(String)
	 */
	public String getPrefix() {
		return prefix;
	}

	/**
	 * Set the prefix
	 *
	 * @param prefix prefix for {@link io.github.jdiscordbots.command_framework.CommandFramework CommandFramework} instance
	 * @return updated {@link io.github.jdiscordbots.command_framework.CommandFramework CommandFramework} for chaining convenience
	 * @see io.github.jdiscordbots.command_framework.CommandFramework#getPrefix()
	 */
	public CommandFramework setPrefix(String prefix) {
		this.prefix = prefix;
		return this;
	}

	/**
	 * Get all owners of the current {@link io.github.jdiscordbots.command_framework.CommandFramework CommandFramework} instance
	 *
	 * @return owner ids
	 * @see io.github.jdiscordbots.command_framework.CommandFramework#setOwners(String[])
	 * @see io.github.jdiscordbots.command_framework.CommandFramework#setOwners(String, String...)
	 */
	public String[] getOwners() {
		return owners;
	}

	/**
	 * Set owners of {@link io.github.jdiscordbots.command_framework.CommandFramework CommandFramework} instance
	 *
	 * @param owners owners
	 * @return updated {@link io.github.jdiscordbots.command_framework.CommandFramework CommandFramework} for chaining convenience
	 */
	public CommandFramework setOwners(String[] owners) {
		return this.setOwners(owners[0], Arrays.copyOfRange(owners, 1, owners.length));
	}

	/**
	 * Get unknown command message handler
	 *
	 * @return {@link java.util.function.Consumer<TextChannel> Consumer}
	 * @see io.github.jdiscordbots.command_framework.CommandFramework#onUnknownCommand(Consumer)
	 */
	Consumer<TextChannel> getOnUnknownCommandHandler() {
		return onUnknownCommandHandler;
	}

	/**
	 * Set unknown command message handler
	 *
	 * @param onUnknownCommandHandler {@link java.util.function.Consumer<TextChannel> Consumer}
	 * @return updated {@link io.github.jdiscordbots.command_framework.CommandFramework CommandFramework} for chaining convenience
	 * @see io.github.jdiscordbots.command_framework.CommandFramework#getOnUnknownCommandHandler()
	 */
	public CommandFramework onUnknownCommand(Consumer<TextChannel> onUnknownCommandHandler) {
		this.onUnknownCommandHandler = onUnknownCommandHandler;
		return this.setUnknownCommand(true);
	}

	/**
	 * Return whether mention prefix is enabled or not
	 *
	 * @return <code>true</code> if mention prefix is enabled, otherwise <code>false</code>
	 * @see io.github.jdiscordbots.command_framework.CommandFramework#setMentionPrefix(boolean)
	 */
	public boolean isMentionPrefix() {
		return mentionPrefix;
	}

	/**
	 * Set whether the system should respond to mentions or not
	 *
	 * @param mentionPrefix state of mention prefix
	 * @return updated {@link io.github.jdiscordbots.command_framework.CommandFramework CommandFramework} for chaining convenience
	 * @see io.github.jdiscordbots.command_framework.CommandFramework#isMentionPrefix()
	 */
	public CommandFramework setMentionPrefix(boolean mentionPrefix) {
		this.mentionPrefix = mentionPrefix;
		return this;
	}

	/**
	 * Get all registered commands of the {@link io.github.jdiscordbots.command_framework.CommandFramework CommandFramework} instance
	 *
	 * @return all registerd commands
	 * @see io.github.jdiscordbots.command_framework.CommandFramework#addCommands(Reflections)
	 * @see io.github.jdiscordbots.command_framework.CommandFramework#addAction(Reflections, BiConsumer)
	 */
	public Map<String, ICommand> getCommands() {
		return CommandHandler.getCommands();
	}

	/**
	 * Build {@link io.github.jdiscordbots.command_framework.CommandFramework CommandFramework} instance and create command listener
	 *
	 * @return {@link net.dv8tion.jda.api.hooks.ListenerAdapter ListenerAdapter}
	 */
	public ListenerAdapter build() {
		if (LOG.isDebugEnabled())
			LOG.debug("Listening to following commands ({}):\n{}", CommandHandler.getCommands().size(), String
				.join(", ", CommandHandler.getCommands().keySet()));

		return new CommandListener(this);
	}

	/**
	 * CommandListener
	 */
	private static final class CommandListener extends ListenerAdapter {
		private final String prefix;
		private final boolean mentionPrefix;

		/**
		 * Construct a new CommandListener with the given CommandFramework
		 *
		 * @param framework {@link io.github.jdiscordbots.command_framework.CommandFramework CommandFramework}
		 */
		public CommandListener(CommandFramework framework) {
			this.prefix = framework.getPrefix();
			this.mentionPrefix = framework.isMentionPrefix();
		}

		/**
		 * Handle incomming messages
		 *
		 * @param event {@link net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent GuildMessageReceivedEvent}
		 */
		@Override
		public void onGuildMessageReceived(@Nonnull GuildMessageReceivedEvent event) {
			final Message message = event.getMessage();
			final String contentRaw = message.getContentRaw().trim();
			final String selfUserId = message.getJDA().getSelfUser().getId();
			final boolean containsMention = contentRaw.startsWith("<!@" + selfUserId + "> ")
				|| contentRaw.startsWith("<@" + selfUserId + "> ");

			if (message.getAuthor().isBot())
				return;

			if (this.mentionPrefix && containsMention) {
				CommandHandler.handle(CommandHandler.CommandParser
					.parse(event, CommandHandler.CommandParser.SPACE_PATTERN.split(contentRaw)[0] + " "));
				return;
			}

			if (message.getContentDisplay().startsWith(this.prefix))
				CommandHandler.handle(CommandHandler.CommandParser.parse(event, prefix));
		}
	}
}
