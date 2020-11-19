package net.teamfruit.assay;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public final class AsSay extends JavaPlugin {
    private List<String> getFrom(String[] args, int index) {
        if (args.length > index)
            return Arrays.asList(args).subList(index, args.length);
        return Collections.emptyList();
    }

    private Optional<String> checkPermission(CommandSender sender, String name) {
        if (sender.hasPermission("assay.passthrough." + name))
            return Optional.empty();

        if (!sender.hasPermission("assay.offline")) {
            Player player = Bukkit.getPlayer(name);
            if (player == null)
                return Optional.of("オフラインの人として発言する");
        }

        if (!sender.hasPermission("assay.outside")) {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(name);
            if (!offlinePlayer.hasPlayedBefore())
                return Optional.of("鯖に入ったことのない人として発言する");
        }

        if (!sender.hasPermission("assay.as." + name))
            return Optional.of("ホワイトリストに追加されていない人として発言する");

        return Optional.empty();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 2)
            return false;

        String name = args[0];
        Optional<String> reason = checkPermission(sender, name);
        if (reason.isPresent()) {
            sender.sendMessage(new ComponentBuilder()
                    .append("[かめすたプラグイン] ").color(ChatColor.LIGHT_PURPLE)
                    .append(reason.get() + "ためには権限が足りません").color(ChatColor.RED)
                    .create()
            );
            return true;
        }

        String text = String.join(" ", getFrom(args, 1));
        Bukkit.broadcast(new TranslatableComponent(
                "chat.type.text",
                new TextComponent(new ComponentBuilder()
                        .append(new TextComponent(new ComponentBuilder()
                                .append(name)
                                .append("...?")
                                .create()
                        ))
                        .event(new HoverEvent(
                                HoverEvent.Action.SHOW_TEXT,
                                new ComponentBuilder()
                                        .append("実は ").color(ChatColor.WHITE)
                                        .append(sender.getName()).color(ChatColor.YELLOW)
                                        .append(" で～す m9(^Д^)").color(ChatColor.WHITE)
                                        .create()
                        ))
                        .create()
                ),
                new TextComponent(text)
        ));

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        switch (args.length) {
            case 1:
                return Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .filter(e -> !checkPermission(sender, e).isPresent())
                        .filter(e -> e.startsWith(args[0]))
                        .collect(Collectors.toList());
            case 2:
                return Collections.singletonList("<text>");
        }

        return Collections.emptyList();
    }
}
