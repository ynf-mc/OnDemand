package top.ynfmc.ondemand;

import com.moandjiezana.toml.Toml;
import top.ynfmc.ondemand.backend.ICloudVM;
import top.ynfmc.ondemand.backend.TencentCVM;

import java.time.Duration;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;

public class ConfigReader {
    public static class GeneralConfig {
        public String serverName;
        public int port;
        public int hibernationTime;

        public CloudVMController toController() {
            return null;
        }
    }

    public static class TencentCVMConfig extends GeneralConfig {
        public String secretId;
        public String secretKey;
        public String region;
        public String instanceId;

        public CloudVMController toController() {
            ICloudVM cvm = new TencentCVM(this.secretId, this.secretKey, this.region, this.instanceId);
            return new CloudVMController(cvm, this.serverName, this.port, Duration.ofMinutes(this.hibernationTime));
        }
    }

    public static HashMap<String, CloudVMController> fromConfig(Toml toml) {
        HashMap<String, CloudVMController> controllers = new HashMap<>();
        for (Map.Entry<String, Object> entry : toml.entrySet()) {
            String serverName = entry.getKey();
            Toml serverConfig = (Toml) entry.getValue();
            String type = serverConfig.getString("type");
            if (type.equals("tencent-cvm")) {
                TencentCVMConfig config = serverConfig.to(TencentCVMConfig.class);
                CloudVMController controller = config.toController();
                controllers.put(serverName, controller);
            }
            // TODO: add more types
        }
        return controllers;
    }
}
