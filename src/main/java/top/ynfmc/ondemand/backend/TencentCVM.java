package top.ynfmc.ondemand.backend;

import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.cvm.v20170312.CvmClient;
import com.tencentcloudapi.cvm.v20170312.models.*;
import com.tencentcloudapi.common.Credential;
import top.ynfmc.ondemand.OnDemand;

public class TencentCVM implements ICloudVM {
    private Credential credential;

    private CvmClient client;
    private String instanceId;

    public TencentCVM(String secretId, String secretKey, String region, String instanceId) {
        this.credential = new Credential(secretId, secretKey);
        this.client = new CvmClient(this.credential, region);
        this.instanceId = instanceId;
    }

    @Override
    public String runServer() throws TencentCloudSDKException {
        // TODO: Error handling
        StartInstancesRequest startReq = new StartInstancesRequest();
        startReq.setInstanceIds(new String[]{this.instanceId});
        StartInstancesResponse startResp = this.client.StartInstances(startReq);
        OnDemand.logger.info(StartInstancesResponse.toJsonString(startResp));

        DescribeInstancesRequest descReq = new DescribeInstancesRequest();
        descReq.setInstanceIds(new String[]{this.instanceId});
        String ip = this.client.DescribeInstances(descReq).getInstanceSet()[0].getPublicIpAddresses()[0];
        OnDemand.logger.info("Server started. IP: " + ip);
        return ip;
    }

    @Override
    public void stopServer() throws TencentCloudSDKException {
        StopInstancesRequest stopReq = new StopInstancesRequest();
        stopReq.setInstanceIds(new String[]{this.instanceId});
        stopReq.setStopType("SOFT_FIRST");
        stopReq.setStoppedMode("STOP_CHARGING");  // Apparently that's why we're here.
        StopInstancesResponse stopResp = this.client.StopInstances(stopReq);
        OnDemand.logger.info(StopInstancesResponse.toJsonString(stopResp));
    }
}
