package com.skungee.spigot.commands;

import com.skungee.spigot.SpigotSkungee;
import com.skungee.spigot.tasks.ServerDataTask;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class SkungeeSync implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!sender.hasPermission("skungee.commands.sync")) {
            sender.sendMessage(ChatColor.RED+"You do not have a permission.");
            return true;
        }

        sender.sendMessage(ChatColor.GREEN+"Start sync with BungeeCord..");
        Bukkit.getScheduler().runTaskAsynchronously(SpigotSkungee.getInstance(), new ServerDataTask(SpigotSkungee.getInstance(), SpigotSkungee.getInstance().getJapsonClient()));

        return true;
    }
}
