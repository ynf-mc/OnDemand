package top.ynfmc.ondemand;

import com.google.inject.Inject;
import com.moandjiezana.toml.Toml;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.ComponentLike;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.Map;

@Plugin(
        id = "ondemand",
        name = "OnDemand",
        version = "0.0.0",
        description = "Start your server on demand.",
        authors = {"Yi Cao"}
)
public class OnDemand {
    public static ProxyServer server = null;
    public static Logger logger = null;
    public static Path configPath = null;
    public static Map<String, CloudVMController> controllers = null;

    @Inject
    public OnDemand(ProxyServer server, Logger logger, @DataDirectory Path configPath) {
        this.server = server;
        this.logger = logger;
        this.configPath = configPath;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        server.getCommandManager().register(OnDemandCommand.build());

        // Read from config file and initialize controllers.
        Path configFilePath = configPath.resolve("config.yml");
        Toml config = new Toml().read(configFilePath.toFile());
        controllers = ConfigReader.fromConfig(config);

        logger.info("OnDemand loaded.");
    }

    // Broadcast a message to all players and the console.
    public static void broadcast(ComponentLike message) {
        OnDemand.server.getConsoleCommandSource().sendMessage(message);
        for (Player p : OnDemand.server.getAllPlayers()) {
            p.sendMessage(message);
        }
    }

    // Start a server.
    // 0 -> success
    // 1 -> failed to run server
    // 2 -> server not found
    public static int startServer(String serverName) {
        CloudVMController controller = OnDemand.controllers.get(serverName);
        if (controller == null) {
            return 2;
        }
        return controller.runServer();
    }

    // Stop a server.
    // 0 -> success
    // 1 -> failed to run server
    // 2 -> server not found
    public static int stopServer(String serverName) {
        CloudVMController controller = OnDemand.controllers.get(serverName);
        if (controller == null) {
            return 2;
        }
        return controller.stopServer();
    }
}
