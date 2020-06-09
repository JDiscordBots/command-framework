# Command Framework
> **Most of the code has been copied from [Nightdream](https://github.com/JDiscordBots/Nightdream)'s Command system which was made by [dan1st](https://github.com/danthe1st)**

## Usage

1. Create an instance of the CommandFramework
2. Set prefix, owners etc.
3. Build the command listener using `CommandFramework#build()` and add it to your JDA(Builder) instance
4. Create command classes which are annotated by `@Command("commandname")` and implement the `ICommand` interface
5. See how it works perfectly

### Working example:

Main:
```java
public class Main {
    public static void main(String[] args) throws LoginException {
    		final JDABuilder builder = JDABuilder.createDefault("your token");
    		final CommandFramework framework = new CommandFramework() // Step 1
                    /* Step 2 */
    				.setMentionPrefix(true) // Allow mention prefix, Default: true
    				.setPrefix("prefix") // Default: !
    				.setOwners("owner1", "optional owner 2", "...", "optional owner n"); // Set owners for permissions system, Default: {}
    
    		builder.addEventListeners(framework.build()).build(); // Step 3
    	}
}
```

Example command:
```java
import io.github.jdiscordbots.command_framework.*;

@Command({"example", "examplealias"}) // Step 4
public class Example implements ICommand {
    @Override
    public void action(CommandEvent event) {
        event.getChannel().sendMessage("Example command reply").queue();
    }
}
```

Example permission restricted command:
```java
import io.github.jdiscordbots.command_framework.*;
import net.dv8tion.jda.api.Permission;

@Command({"permissionexample", "permissionexamplealias"}) // Step 4
public class PermissionExample implements ICommand {
    @Override
    public void action(CommandEvent event) {
        event.getChannel().sendMessage("Permission example command reply").queue();
    }
    
    @Override
    public boolean allowExecute(CommandEvent event) {
        return event.getMember().hasPermission(Permission.MANAGE_PERMISSIONS); // Allow use of command only to members with manage permissions permission
    }
}
```