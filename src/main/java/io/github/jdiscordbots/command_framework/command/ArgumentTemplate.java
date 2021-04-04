package io.github.jdiscordbots.command_framework.command;

import java.util.Arrays;
import java.util.Objects;

import net.dv8tion.jda.api.entities.Command.OptionType;

/**
 * Represents an expected parameter of a command
 */
public class ArgumentTemplate {
	private final OptionType type;
	private final String name;
	private final String description;
	private final boolean required;
	private final String[] choices;

	public ArgumentTemplate(OptionType type, String name, String description,boolean required) {
		Objects.requireNonNull(type);
		Objects.requireNonNull(name);
		Objects.requireNonNull(description);
		if(type==OptionType.UNKNOWN) {
			throw new IllegalArgumentException("OptionType.UNKNOWN not supported");
		}
		this.type = type;
		this.name = name;
		this.description = description;
		this.required=required;
		this.choices=null;
	}
	
	public ArgumentTemplate(String name, String description,boolean required,String... choices) {
		Objects.requireNonNull(name);
		Objects.requireNonNull(description);
		this.type = OptionType.STRING;
		this.name = name;
		this.description = description;
		this.required=required;
		if(choices==null||choices.length==0) {
			this.choices=null;
		}else if(type.canSupportChoices()) {
			this.choices=Arrays.copyOf(choices, choices.length);//TODO
		}else {
			throw new IllegalArgumentException("Tried to use choices on a type that does not support choices");
		}
	}
	
	public ArgumentTemplate(String name, String description,boolean required,int... choices) {
		Objects.requireNonNull(name);
		Objects.requireNonNull(description);
		this.type = OptionType.INTEGER;
		this.name = name;
		this.description = description;
		this.required=required;
		if(choices==null||choices.length==0) {
			this.choices=null;
		}else if(type.canSupportChoices()) {
			this.choices=Arrays.stream(choices).mapToObj(String::valueOf).toArray(String[]::new);
		}else {
			throw new IllegalArgumentException("Tried to use choices on a type that does not support choices");
		}
	}
	
	/**
	 * gets the type of the parameter
	 * @return
	 */
	public OptionType getType() {
		return type;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public boolean isRequired() {
		return required;
	}
	
	public String[] getChoices() {
		return Arrays.copyOf(choices, choices.length);
	}
	
	public boolean hasChoices() {
		return choices!=null;
	}
}
