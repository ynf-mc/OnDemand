package top.ynfmc.ondemand;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import net.kyori.adventure.text.Component;


public class OnDemandCommand {
    public static BrigadierCommand build() {
        return new BrigadierCommand(LiteralArgumentBuilder.<CommandSource>literal("ondemand")
                .then(LiteralArgumentBuilder.<CommandSource>literal("start")
                        .then(RequiredArgumentBuilder.<CommandSource, String>argument("server", StringArgumentType.string())
                                .executes(context -> {
                                    String serverName = context.getArgument("server", String.class);
                                    int result = OnDemand.startServer(serverName);
                                    if (result == 2) {
                                        context.getSource().sendMessage(Component.text("Server not found."));
                                    }
                                    return result;
                                })))
                .then(LiteralArgumentBuilder.<CommandSource>literal("stop")
                        .then(RequiredArgumentBuilder.<CommandSource, String>argument("server", StringArgumentType.string())
                                .executes(context -> {
                                    String serverName = context.getArgument("server", String.class);
                                    int result = OnDemand.stopServer(serverName);
                                    if (result == 2) {
                                        context.getSource().sendMessage(Component.text("Server not found."));
                                    }
                                    return result;
                                }))).then(LiteralArgumentBuilder.<CommandSource>literal("list").executes(ctx -> {
                    ctx.getSource().sendMessage(Component.text("Servers:"));
                    for (String serverName : OnDemand.controllers.keySet()) {
                        ctx.getSource().sendMessage(Component.text("-> " + serverName));
                    }
                    return 0;
                })).build());
    }
}
