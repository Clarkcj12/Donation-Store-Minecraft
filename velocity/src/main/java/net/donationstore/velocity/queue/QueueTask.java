package net.donationstore.velocity.queue;

import com.velocitypowered.api.proxy.Player;
import net.donationstore.commands.CommandManager;
import net.donationstore.models.request.UpdateCommandExecutedRequest;
import net.donationstore.models.response.PaymentsResponse;
import net.donationstore.models.response.QueueResponse;
import net.donationstore.velocity.DonationStorePlugin;
import net.donationstore.velocity.config.FileConfiguration;
import net.donationstore.velocity.logging.Log;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class QueueTask {

    private CommandManager commandManager;

    public void run(FileConfiguration configuration, DonationStorePlugin plugin) {
        plugin.getServer().getScheduler().buildTask(plugin, new Runnable() {
            @Override
            public void run() {
                if (configuration.getNode("secret-key").getValue() == null || configuration.getNode("webstore-api-location").getValue() == null) {
                    Log.toConsole("You must connect the plugin to your webstore before it can start executing purchased packages.");
                    Log.toConsole("Use /ds connect");
                } else {
                    commandManager = new CommandManager(configuration.getNode("secret-key").getString(), configuration.getNode("webstore-api-location").getString());

                    try {
                        UpdateCommandExecutedRequest updateCommandExecutedRequest = new UpdateCommandExecutedRequest();

                        QueueResponse queueResponse = commandManager.getCommands();

                        for(PaymentsResponse payment: queueResponse.payments) {
                            for(net.donationstore.models.Command command: payment.commands) {

                                Optional<Player> player;

                                if (queueResponse.webstore.webstoreType.equals("OFF")) {
                                    player = plugin.getServer().getPlayer(command.username);
                                } else {
                                    player = plugin.getServer().getPlayer(UUID.fromString(command.uuid));
                                }

                                boolean canExecuteCommand = false;

                                if (command.requireOnline) {
                                    if (player.isPresent()) {
                                        canExecuteCommand = true;
                                    }
                                } else {
                                    canExecuteCommand = true;
                                }

                                if(canExecuteCommand) {
                                    runCommand(plugin, command.command);
                                    updateCommandExecutedRequest.getCommands().add(command.id);
                                }
                            }
                        }
                        commandManager.updateCommandsToExecuted(updateCommandExecutedRequest);
                    } catch(Exception e) {
                        Log.toConsole(e.getMessage());
                    }
                }
            }
        }).delay(20, TimeUnit.SECONDS).repeat(configuration.getNode("queue-delay").getInt(), TimeUnit.SECONDS).schedule();
    }

    public void runCommand(DonationStorePlugin plugin, String command) {
        plugin.getServer().getCommandManager().execute(plugin.getServer().getConsoleCommandSource(), command);
    }
}
