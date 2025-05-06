package sngmc.build.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import sngmc.build.SNGMCEconomy;

public class TakeMoneyCommand implements CommandExecutor {

    private final SNGMCEconomy plugin;

    public TakeMoneyCommand(SNGMCEconomy plugin) {
        this.plugin = plugin;
        plugin.getCommand("takemoney").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("sngmceconomy.admin")) {
            sender.sendMessage(plugin.getConfigManager().getMessage("no-permission"));
            return true;
        }

        if (args.length != 2) {
            sender.sendMessage(plugin.getConfigManager().getMessage("usagetakemoney"));
            return true;
        }

        Player target = plugin.getServer().getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(plugin.getConfigManager().getMessage("player-not-found"));
            return true;
        }

        try {
            double amount = Double.parseDouble(args[1]);
            if (amount <= 0) {
                sender.sendMessage(plugin.getConfigManager().getMessage("invalid-amount"));
                return true;
            }

            double balance = plugin.getEconomy().getBalance(target);
            if (balance < amount) {
                amount = balance;
            }

            plugin.getEconomy().withdrawPlayer(target, amount);

            plugin.getEconomy().getDataManager().logTransaction(
                    target.getUniqueId().toString(),
                    "SERVER",
                    amount,
                    "ADMIN_TAKE"
            );

            String formattedAmount = plugin.getEconomy().format(amount);
            sender.sendMessage(plugin.getConfigManager().getMessage("money-taken",
                    "%player%", target.getName(),
                    "%amount%", formattedAmount));

            sender.sendMessage(plugin.getConfigManager().getMessage("transaction-success"));

        } catch (NumberFormatException e) {
            sender.sendMessage(plugin.getConfigManager().getMessage("invalid-amount"));
        }

        return true;
    }
}