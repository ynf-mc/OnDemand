package top.ynfmc.ondemand;

import com.velocitypowered.api.proxy.server.ServerInfo;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import org.jetbrains.annotations.NotNull;
import top.ynfmc.ondemand.backend.ICloudVM;

import java.net.InetSocketAddress;
import java.time.Duration;

public class CloudVMController {
    private final ICloudVM cloudVM;
    private final String serverName;
    private String ip;
    private final int port;
    private final Duration hibernationTime;
    private Thread pollingThread;


    public CloudVMController(ICloudVM cloudVM, String serverName, int port, Duration hibernationTime) {
        this.cloudVM = cloudVM;
        this.serverName = serverName;
        this.ip = null;
        this.port = port;
        this.hibernationTime = hibernationTime;
    }

    public int runServer() {
        try {
            this.ip = this.cloudVM.runServer();
        } catch (Exception e) {
            OnDemand.logger.error("Failed to run server: ", e);
            return 1;
        }

        // Add the newly started server to the server list.
        if (this.ip != null) {
            InetSocketAddress address = new InetSocketAddress(this.ip, this.port);
            OnDemand.server.registerServer(new ServerInfo(this.serverName, address));
            ComponentLike message = Component.text()
                    .append(Component.text("Server started at "))
                    .append(Component.text(this.serverName))
                    .build();
            OnDemand.broadcast(message);
        } else {
            OnDemand.logger.error("Failed to run server: IP address is null.");
            return 1;
        }

        // Start a new thread polling the server status.
        // If the player count is 0 for more than $hibernationTime, stop the server.
        this.pollingThread = new Thread(getPollServerStatusFunc());

        return 0;
    }

    @NotNull
    private Runnable getPollServerStatusFunc() {
        return () -> {
            while (true) {
                try {
                    Thread.sleep(1000);
                    if (OnDemand.server.getServer(this.serverName).get().getPlayersConnected().size() == 0) {

                        // Warn players that the server will be stopped.
                        ComponentLike message = Component.text()
                                .append(Component.text("The server will be stopped in "))
                                .append(Component.text(this.hibernationTime.toSeconds()))
                                .append(Component.text(" seconds."))
                                .build();
                        OnDemand.broadcast(message);

                        Thread.sleep(this.hibernationTime.toMillis());
                        if (OnDemand.server.getServer(this.serverName).get().getPlayersConnected().size() == 0) {
                            try {
                                this.cloudVM.stopServer();
                            } catch (Exception e) {
                                OnDemand.logger.error("Failed to stop server: ", e);
                                break;
                            }
                            break;
                        }
                    }
                } catch (Exception e) {
                    OnDemand.logger.error("Failed to poll server status: ", e);
                    break;
                }
            }
        };
    }

    public int stopServer() {
        this.pollingThread.interrupt();
        try {
            this.cloudVM.stopServer();
        } catch (Exception e) {
            OnDemand.logger.error("Failed to stop server: ", e);
            return 1;
        }
        return 0;
    }
}