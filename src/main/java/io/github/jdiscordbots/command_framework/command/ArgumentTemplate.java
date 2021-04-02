package io.github.jdiscordbots.command_framework.command;

import net.dv8tion.jda.api.entities.Command.OptionType;

public class ArgumentTemplate {
	private final OptionType type;
	private final String name;
	private final String description;
	private final boolean required;

	public ArgumentTemplate(OptionType type, String name, String description,boolean required) {
		this.type = type;
		this.name = name;
		this.description = description;
		this.required=required;
	}

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
}
