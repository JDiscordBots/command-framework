package io.github.jdiscordbots.command_framework.command;

import java.util.Arrays;
import java.util.Objects;

import net.dv8tion.jda.api.interactions.commands.OptionType;

/**
 * Represents an expected parameter of a command
 */
public final class ArgumentTemplate
{
	private final OptionType type;
	private final String name;
	private final String description;
	private final boolean required;
	private final String[] choices;

	private ArgumentTemplate(OptionType type, String name, String description, boolean required, String[] choices)
	{
		Objects.requireNonNull(type);
		if(type==OptionType.UNKNOWN)
		{
			throw new IllegalArgumentException("OptionType.UNKNOWN not supported");
		}
		Objects.requireNonNull(name);
		if(name.isEmpty())
		{
			throw new IllegalArgumentException("Argument name is empty");
		}
		Objects.requireNonNull(description);
		if(description.isEmpty())
		{
			throw new IllegalArgumentException("Argument description is empty");
		}
		if(choices!=null&&!type.canSupportChoices())
		{
			throw new IllegalArgumentException("Tried to use choices on a type ("+type+") that does not support choices");
		}
		this.type = type;
		this.name = name;
		this.description = description;
		this.required = required;
		this.choices = choices;
	}
	
	/**
	 * Creates an {@link ArgumentTemplate}.
	 * @param type the {@link OptionType} that arguments should be associated with, should not be <code>null</code> or {@link OptionType#UNKNOWN}
	 * @param name the name of the argument, should not be <code>null</code> or empty
	 * @param description the description of the argument, should not be <code>null</code> or empty
	 * @param required <code>true</code> if the argument is required, else <code>false</code>
	 * @throws IllegalArgumentException if an argument is invalid
	 * @throws NullPointerException if an argument that should not be <code>null</code> is <code>null</code>
	 */
	public ArgumentTemplate(OptionType type, String name, String description,boolean required)
	{
		this(type,name, description, required, null);
	}
	/**
	 * Creates an {@link ArgumentTemplate} of an argument that can only be one of specific {@link String} choices.
	 * If <code>choices</code> is <code>null</code>, a normal {@link String} argument will be created instead
	 * @param name the name of the argument, should not be <code>null</code> or empty
	 * @param description the description of the argument, should not be <code>null</code> or empty
	 * @param required <code>true</code> if the argument is required, else <code>false</code>
	 * @param choices the possible {@link String} choices for this argument
	 * @throws IllegalArgumentException if an argument is invalid
	 * @throws NullPointerException if an argument that should not be <code>null</code> is <code>null</code>
	 */
	public ArgumentTemplate(String name, String description,boolean required,String... choices)
	{
		this(OptionType.STRING,name, description, required,
				choices==null||choices.length==0?null:
					Arrays.copyOf(choices, choices.length));
	}
	/**
	 * Creates an {@link ArgumentTemplate} of an argument that can only be one of specific <code>int</code> choices.
	 * If <code>choices</code> is <code>null</code>, a normal <code>int</code> argument will be created instead
	 * @param name the name of the argument, should not be <code>null</code> or empty
	 * @param description the description of the argument, should not be <code>null</code> or empty
	 * @param required <code>true</code> if the argument is required, else <code>false</code>
	 * @param choices the possible <code>int</code> choices for this argument
	 */
	public ArgumentTemplate(String name, String description, boolean required, int... choices)
	{
		this(OptionType.INTEGER,name, description, required,
				choices==null||choices.length==0?null:
					Arrays.stream(choices).mapToObj(String::valueOf).toArray(String[]::new));
	}
	
	/**
	 * Gets the type of the parameter.
	 * @return the {@link OptionType type} of the parameter
	 */
	public OptionType getType()
	{
		return type;
	}

	/**
	 * Gets the name of the parameter.
	 * @return the name of the parameter
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * Gets the description of the parameter.
	 * @return the description of the parameter
	 */
	public String getDescription()
	{
		return description;
	}

	/**
	 * Tests if the parameter is required.
	 * @return <code>true</code> if the parameter is required, else <code>false</code>
	 */
	public boolean isRequired()
	{
		return required;
	}
	
	/**
	 * Gets the choices for this argument.
	 * @return the choices or <code>null</code> the argument is not a choice argument
	 */
	public String[] getChoices()
	{
		return choices==null?null:Arrays.copyOf(choices, choices.length);
	}
	
	/**
	 * Checks if the argument is a choice-argument
	 * @return <code>true</code> if it is a choice-argument, else <code>false</code>
	 */
	public boolean hasChoices()
	{
		return choices!=null;
	}
}
