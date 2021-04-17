package io.github.jdiscordbots.command_framework.utils;

import io.github.jdiscordbots.command_framework.command.CommandEvent;

import java.util.Arrays;

public final class PermissionUtils
{
	private PermissionUtils()
	{
		/* Prevent instantiation */
	}

	public static boolean checkOwner(CommandEvent event)
	{
		return Arrays.stream(event.getFramework().getOwners()).anyMatch(id -> event.getAuthor().getId().equals(id));
	}
}
