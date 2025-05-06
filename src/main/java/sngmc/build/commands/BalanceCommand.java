package sngmc.build.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import sngmc.build.SNGMCEconomy;

public class BalanceCommand implements CommandExecutor {

    private final SNGMCEconomy plugin;

    public BalanceCommand(SNGMCEconomy plugin) {
        this.plugin = plugin;
        plugin.getCommand("balance").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player) && args.length == 0) {
            sender.sendMessage("Эта команда только для игроков!");
            return true;
        }

        if (args.length == 0) {
            // Проверка своего баланса
            Player player = (Player) sender;
            double balance = plugin.getEconomy().getBalance(player);
            String formatted = plugin.getEconomy().format(balance);
            sender.sendMessage(plugin.getConfigManager().getMessage("balance-self", "%balance%", formatted));
            return true;
        }

        if (args.length == 1 && sender.hasPermission("sngmceconomy.admin")) {
            // Проверка баланса другого игрока
            Player target = plugin.getServer().getPlayer(args[0]);
            if (target == null) {
                sender.sendMessage(plugin.getConfigManager().getMessage("player-not-found"));
                return true;
            }

            double balance = plugin.getEconomy().getBalance(target);
            String formatted = plugin.getEconomy().format(balance);
            sender.sendMessage(plugin.getConfigManager().getMessage("balance-other",
                    "%player%", target.getName(),
                    "%balance%", formatted));
            return true;
        }

        return false;
    }
}