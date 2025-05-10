package sngmc.build.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import sngmc.build.SNGMCEconomy;

public class PayCommand implements CommandExecutor {

    private final SNGMCEconomy plugin;

    public PayCommand(SNGMCEconomy plugin) {
        this.plugin = plugin;
        plugin.getCommand("pay").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getConfigManager().getMessage("player-not-found"));
            return true;
        }

        Player player = (Player) sender;

        if (args.length != 2) {
            sender.sendMessage(plugin.getConfigManager().getMessage("usagepay"));
            return true;
        }

        Player target = plugin.getServer().getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(plugin.getConfigManager().getMessage("player-not-found"));
            return true;
        }

        if (player.equals(target)) {
            sender.sendMessage(plugin.getConfigManager().getMessage("self-payment"));
            return true;
        }

        try {
            double amount = Double.parseDouble(args[1]);
            if (amount <= 0) {
                sender.sendMessage(plugin.getConfigManager().getMessage("invalid-amount"));
                return true;
            }

            if (!plugin.getEconomy().has(player, amount)) {
                sender.sendMessage(plugin.getConfigManager().getMessage("not-enough-money"));
                return true;
            }

            plugin.getEconomy().withdrawPlayer(player, amount);
            plugin.getEconomy().depositPlayer(target, amount);

            plugin.getEconomy().getDataManager().logTransaction(
                    player.getUniqueId().toString(),
                    target.getUniqueId().toString(),
                    amount,
                    "PLAYER_TO_PLAYER",
                    player.getName(),
                    target.getName()
            );

            String formattedAmount = plugin.getEconomy().format(amount);

            player.sendMessage(plugin.getConfigManager().getMessage("pay-sent",
                    "%player%", target.getName(),
                    "%amount%", formattedAmount));

            target.sendMessage(plugin.getConfigManager().getMessage("pay-received",
                    "%player%", player.getName(),
                    "%amount%", formattedAmount));

            sender.sendMessage(plugin.getConfigManager().getMessage("transaction-success"));

        } catch (NumberFormatException e) {
            sender.sendMessage(plugin.getConfigManager().getMessage("invalid-amount"));
        }

        return true;
    }
}