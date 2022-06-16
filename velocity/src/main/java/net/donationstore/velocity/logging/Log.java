package net.donationstore.velocity.logging;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

import java.util.List;

public class Log {

    public static void send(CommandSource sender, String log) {
        if (sender instanceof Player) {
            TextComponent message = Component.text("[Donation Store]").color(NamedTextColor.GREEN)
                    .append(Component.text(String.format(": %s", log)).color(NamedTextColor.WHITE));

            sender.sendMessage(message);
        } else {
            Log.toConsole(String.format("[Donation Store]: %s", log));
        }
    }

    public static void toConsole(String log) {
        System.out.println(String.format("[Donation Store]: %s", log));
    }

    public static void displayLogs(CommandSource sender, List<String> logs) {
        for(String log: logs) {
            Log.send(sender, log);
        }
    }
}
