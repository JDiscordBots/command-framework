package io.github.jdiscordbots.command_framework;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;
import io.github.jdiscordbots.command_framework.command.Command;
import io.github.jdiscordbots.command_framework.command.CommandEvent;
import io.github.jdiscordbots.command_framework.command.ICommand;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
/**
 * Main class of the command framework.
 * 
 * See the README file of the project for details.
 * This class is thread safe.
 * 
 * @see CommandFramework#build()
 */
public class CommandFramework
{
	private static final Logger LOG=LoggerFactory.getLogger(CommandFramework.class);
	
	private AtomicReference<Consumer<CommandEvent>> unknownCommandConsumer = new AtomicReference<>();
	private AtomicReference<Consumer<ButtonClickEvent>> unknownButtonConsumer = new AtomicReference<>();
	private AtomicReference<String> prefix = new AtomicReference<>("!");
	private final Set<String> owners = Collections.newSetFromMap(new ConcurrentHashMap<>());
	private volatile boolean mentionPrefix = true;
	private volatile boolean unknownCommand = true;
	private volatile boolean slashCommandsPerGuild=false;
	private volatile boolean removeUnknownSlashCommands=true;
	
	/**
	 * Creates an instance of the command framework.
	 * Only classes in the same package (and subpackages) than the calling class will be scanned for commands.
	 * @implSpec This constructor should not be used by subclasses. Instead, subclasses should use {@code super(getCallerPackageName())}
	 */
	public CommandFramework()
	{
		this(getCallerPackageName());
		if(getClass()==CommandFramework.class)
		{
			throw new IllegalStateException("This constructor may not be called by subclasses!");
		}
	}

	/**
	 * Gets the package name of the class of the calling method
	 * @return the package name of the class of the calling method
	 */
	protected static final String getCallerPackageName()
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
	
	/**
	 * Creates an instance of the command framework.
	 * Only classes in the passed package (and subpackages) will be scanned for commands.
	 * @param commandsRootPackage the root package to scan
	 */
	public CommandFramework(String commandsRootPackage)
	{
		try (ScanResult scanResult = new ClassGraph().acceptPackages(commandsRootPackage).enableAnnotationInfo().scan())
		{
			addCommands(scanResult);
		}
	}

	private static void addCommands(ScanResult scanResult)
	{
		addAction(scanResult, (cmdAsAnnotation, annotatedAsObject) ->
		{
			final Command cmdAsBotCommand = (Command) cmdAsAnnotation;
			final ICommand cmd = (ICommand) annotatedAsObject;
			
			for (String alias : cmdAsBotCommand.value())
			{
				CommandHandler.addCommand(alias.toLowerCase(), cmd);
			}
		});
	}
	
	private static void addAction(ScanResult scanResult, BiConsumer<Annotation, Object> function)
	{
		for (ClassInfo cInfo : scanResult.getClassesWithAnnotation(Command.class.getCanonicalName()))
		{
			try
			{
				final Class<?> cl=cInfo.loadClass();
				final Object annotatedAsObject = cl.getDeclaredConstructor().newInstance();
				final Annotation cmdAsAnnotation = cl.getAnnotation((Class<? extends Annotation>) Command.class);

				function.accept(cmdAsAnnotation, annotatedAsObject);
			}
			catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e)
			{
				if(LOG.isErrorEnabled())
					LOG.error("An exception occurred trying to create and register an instance of the class {}.", cInfo.getName(), e);
			}
		}
	}

	/**
	 * Sets the prefix for commands to listen on.
	 * 
	 * Only messages beginning with the given prefix are interpreted as commands.
	 * @param prefix the new prefix
	 * @return the instance (<code>this</code>) of the {@link CommandFramework} that can be used for chaining.
	 */
	public CommandFramework setPrefix(String prefix)
	{
		this.prefix.set(prefix);
		return this;
	}
	
	/**
	 * Gets the current prefix for commands to listen on.
	 * 
	 * Only messages beginning with the given prefix are interpreted as commands.
	 * @return the current prefix
	 */
	public String getPrefix()
	{
		return prefix.get();
	}
	
	/**
	 * Sets the owners of the bot.
	 * 
	 * Owners can bypass permissions
	 * @param owners an array containing the IDs of all owners
	 * @return the instance (<code>this</code>) of the {@link CommandFramework} that can be used for chaining.
	 */
	public CommandFramework setOwners(String... owners)
	{
		setOwners(Arrays.asList(owners));
		return this;
	}
	
	/**
	 * Gets the owners of the bot.
	 * 
	 * Owners can bypass permissions
	 * @return a {@link Set} containing the IDs of all owners
	 */
	public Set<String> getOwners()
	{
		return Collections.unmodifiableSet(owners);
	}
	
	/**
	 * Sets the owners of the bot.
	 * 
	 * Owners can bypass permissions.
	 * @param owners a {@link Collection} containing the IDs of all owners
	 * @return the instance (<code>this</code>) of the {@link CommandFramework} that can be used for chaining.
	 */
	public CommandFramework setOwners(Collection<String> owners)
	{
		synchronized (this.owners)
		{
			this.owners.clear();
			this.owners.addAll(owners);
		}
		return this;
	}

	/**
	 * Sets whether messages starting with a mention are interpreted as commands or not.
	 * @param mentionPrefix <code>true</code> if a mention of the bot should be a valid prefix, else <code>false</code>
	 * @return the instance (<code>this</code>) of the {@link CommandFramework} that can be used for chaining.
	 */
	public CommandFramework setMentionPrefix(boolean mentionPrefix)
	{
		this.mentionPrefix=mentionPrefix;
		return this;
	}
	
	/**
	 * Checks whether messages starting with a mention are interpreted as commands or not.
	 * @return the instance (<code>this</code>) of the {@link CommandFramework} that can be used for chaining.
	 */
	public boolean isMentionPrefix()
	{
		return mentionPrefix;
	}

	/**
	 * Sets whether an action should be taken if an unknown command is executed.
	 * @param unknownCommand <code>true</code> if an action should be taken if an unknown command is executed, else <code>false</code>
	 * @return the instance (<code>this</code>) of the {@link CommandFramework} that can be used for chaining.
	 * @see CommandFramework#isUnknownCommand()
	 * @see CommandFramework#setUnknownCommandAction(Consumer)
	 */
	public CommandFramework setUnknownCommand(boolean unknownCommand)
	{
		this.unknownCommand=unknownCommand;
		return this;
	}
	
	Consumer<CommandEvent> getUnknownCommandConsumer()
	{
		return unknownCommandConsumer.get();
	}

	/**
	 * Sets the action that occurs when an unknwon command is entered.
	 * @param unknownCommandConsumer the action triggered on unknown commands
	 * @return the instance (<code>this</code>) of the {@link CommandFramework} that can be used for chaining.
	 * @see CommandFramework#isUnknownCommand()
	 */
	public CommandFramework setUnknownCommandAction(Consumer<CommandEvent> unknownCommandConsumer)
	{
		this.unknownCommandConsumer.set(unknownCommandConsumer);
		return this.setUnknownCommand(true);
	}

	/**
	 * Checks whether an action should be taken if an unknown command is executed.
	 * @return <code>true</code> if an action should be taken if an unknown command is executed, else <code>false</code>
	 */
	public boolean isUnknownCommand()
	{
		return unknownCommand;
	}
	
	/**
	 * Sets whether slash commands should be enabled globally or on a per-guild basis.
	 * 
	 * <a href="https://discord.com/developers/docs/interactions/slash-commands#registering-a-command">Per-Guild commands are updated instantly while updating of global slash commands may take an hour to update.</a>
	 * @param slashCommandsPerGuild <code>true</code> if slash commands should be set up per-guild
	 * @return the instance (<code>this</code>) of the {@link CommandFramework} that can be used for chaining.
	 */
	public CommandFramework setSlashCommandsPerGuild(boolean slashCommandsPerGuild)
	{
		this.slashCommandsPerGuild = slashCommandsPerGuild;
		return this;
	}
	
	/**
	 * Checks whether slash commands should be enabled globally or on a per-guild basis.
	 * @return <code>true</code> if slash commands should be set up per-guild
	 * @see CommandFramework#setSlashCommandsPerGuild(boolean)
	 */
	public boolean isSlashCommandsPerGuild()
	{
		return slashCommandsPerGuild;
	}
	
	/**
	 * Sets whether unknown slash commands should be removed on startup or not.
	 * @param removeUnknownSlashCommands <code>true</code> if unknown slash commands should be removed on startup, else <code>false</code>
	 */
	public void setRemoveUnknownSlashCommands(boolean removeUnknownSlashCommands)
	{
		this.removeUnknownSlashCommands = removeUnknownSlashCommands;
	}
	
	/**
	 * Checks whether unknown slash commands should be removed on startup or not.
	 * @return <code>true</code> if unknown slash commands should be removed on startup, else <code>false</code>
	 */
	public boolean isRemoveUnknownSlashCommands()
	{
		return removeUnknownSlashCommands;
	}
	
	/**
	 * Creates a listener for handling events related to the command framework.
	 * 
	 * The command framework is active only if the listener returned by this method is active.
	 * @return the listener required for the command framework
	 */
	public ListenerAdapter build()
	{
		if(LOG.isDebugEnabled())
			LOG.debug("Listening to following commands ({}):\n{}", CommandHandler.getCommands().size(), String.join(", ", CommandHandler.getCommands().keySet()));
		
		return new CommandListener(this);
	}
	
	Consumer<ButtonClickEvent> getUnknownButtonConsumer()
	{
		return unknownButtonConsumer.get();
	}
	
	/**
	 * Sets the action executed when an unknown button is pressed.
	 * @param unknownButtonConsumer the action triggered when an unknown button is pressed.
	 * @return the instance (<code>this</code>) of the {@link CommandFramework} that can be used for chaining.
	 */
	public CommandFramework setUnknownButtonAction(Consumer<ButtonClickEvent> unknownButtonConsumer)
	{
		this.unknownButtonConsumer.set(unknownButtonConsumer);
		return this;
	}

	/**
	 * Gets a {@link Map} with the aliases as keys and the command implementations as values.
	 * @return a {@link Map} with all commands
	 */
	public Map<String, ICommand> getCommands()
	{
		return CommandHandler.getCommands();
	}

	
}
