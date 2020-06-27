package io.github.jdiscordbots.command_framework.utils;

import io.github.jdiscordbots.command_framework.command.CommandEvent;

import java.util.Arrays;

/**
 * Permission utilities
 */
public class PermissionUtils {
	/**
	 * Private constructor for utility class
	 */
	private PermissionUtils() {
		/* Prevent instantiation */
	}

	/**
	 * Return whether user is owner or not
	 *
	 * @param event {@link io.github.jdiscordbots.command_framework.command.CommandEvent CommandEvent}
	 * @return <code>true</code> if user is owner, otherwise <code>false</code>
	 */
	public static boolean checkOwner(CommandEvent event) {
		return Arrays.stream(event.getFramework().getOwners()).anyMatch(id -> event.getAuthor().getId().equals(id));
	}
}
