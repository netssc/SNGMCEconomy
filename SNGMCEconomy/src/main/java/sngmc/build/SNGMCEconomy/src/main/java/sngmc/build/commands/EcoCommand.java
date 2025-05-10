package sngmc.build.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import sngmc.build.SNGMCEconomy;

public class EcoCommand implements CommandExecutor {

    private final SNGMCEconomy plugin;

    public EcoCommand(SNGMCEconomy plugin) {
        this.plugin = plugin;
        plugin.getCommand("eco").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("sngmceconomy.admin")) {
            sender.sendMessage(plugin.getConfigManager().getMessage("no-permission"));
            return true;
        }

        if (args.length < 3) {
            sender.sendMessage(plugin.getConfigManager().getMessage("usageeco"));
            return true;
        }

        Player target = plugin.getServer().getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(plugin.getConfigManager().getMessage("player-not-found"));
            return true;
        }

        try {
            double amount = Double.parseDouble(args[2]);
            if (amount <= 0) {
                sender.sendMessage(plugin.getConfigManager().getMessage("invalid-amount"));
                return true;
            }

            switch (args[0].toLowerCase()) {
                case "give":
                    plugin.getEconomy().depositPlayer(target, amount);
                    plugin.getEconomy().getDataManager().logTransaction(
                            "SERVER",
                            target.getUniqueId().toString(),
                            amount,
                            "ADMIN_GIVE",
                            "Сервер",
                            target.getName()
                    );
                    sender.sendMessage(plugin.getConfigManager().getMessage("money-given",
                            "%player%", target.getName(),
                            "%amount%", plugin.getEconomy().format(amount)));
                    break;

                case "set":
                    double oldBalance = plugin.getEconomy().getBalance(target);
                    plugin.getEconomy().withdrawPlayer(target, oldBalance);
                    plugin.getEconomy().depositPlayer(target, amount);
                    plugin.getEconomy().getDataManager().logTransaction(
                            oldBalance > amount ? target.getUniqueId().toString() : "SERVER",
                            oldBalance < amount ? target.getUniqueId().toString() : "SERVER",
                            Math.abs(amount - oldBalance),
                            "ADMIN_SET",
                            oldBalance > amount ? target.getName() : "Сервер",
                            oldBalance < amount ? target.getName() : "Сервер"
                    );
                    sender.sendMessage(plugin.getConfigManager().getMessage("money-set",
                            "%player%", target.getName(),
                            "%amount%", plugin.getEconomy().format(amount)));
                    break;

                case "take":
                    double balance = plugin.getEconomy().getBalance(target);
                    if (balance < amount) {
                        amount = balance;
                    }
                    plugin.getEconomy().withdrawPlayer(target, amount);
                    plugin.getEconomy().getDataManager().logTransaction(
                            target.getUniqueId().toString(),
                            "SERVER",
                            amount,
                            "ADMIN_TAKE",
                            target.getName(),
                            "Сервер"
                    );
                    sender.sendMessage(plugin.getConfigManager().getMessage("money-taken",
                            "%player%", target.getName(),
                            "%amount%", plugin.getEconomy().format(amount)));
                    break;

                default:
                    sender.sendMessage(plugin.getConfigManager().getMessage("usageeco"));
                    return true;
            }

            sender.sendMessage(plugin.getConfigManager().getMessage("transaction-success"));

        } catch (NumberFormatException e) {
            sender.sendMessage(plugin.getConfigManager().getMessage("invalid-amount"));
        }
// Ебатня с логами пиздец позже исправлю
        return true;
    }
}