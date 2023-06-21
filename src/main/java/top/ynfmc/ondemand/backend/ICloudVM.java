package top.ynfmc.ondemand.backend;

public interface ICloudVM {
    // Run the server and return the IP address.
    String runServer() throws Exception;
    // Stop the server.
    void stopServer() throws Exception;
}
