package top.ynfmc.ondemand.backend;

import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.cvm.v20170312.CvmClient;
import com.tencentcloudapi.cvm.v20170312.models.*;
import com.tencentcloudapi.common.Credential;
import top.ynfmc.ondemand.OnDemand;

public class TencentCVM implements ICloudVM {

    private final CvmClient client;
    private final String instanceId;

    public TencentCVM(String secretId, String secretKey, String region, String instanceId) {
        Credential credential = new Credential(secretId, secretKey);
        this.client = new CvmClient(credential, region);
        this.instanceId = instanceId;
    }

    @Override
    public String runServer() throws TencentCloudSDKException {
        StartInstancesRequest startReq = new StartInstancesRequest();
        startReq.setInstanceIds(new String[]{this.instanceId});
        StartInstancesResponse startResp = this.client.StartInstances(startReq);
        OnDemand.logger.info(StartInstancesResponse.toJsonString(startResp));

        // Poll until the server is started (or timeout in 60s and raise an exception).
        pollUntilSuccessOrFailure();

        // Get the IP address of the server.
        DescribeInstancesRequest descReq = new DescribeInstancesRequest();
        descReq.setInstanceIds(new String[]{this.instanceId});
        DescribeInstancesResponse descResp = this.client.DescribeInstances(descReq);
        OnDemand.logger.info(DescribeInstancesResponse.toJsonString(descResp));
        return descResp.getInstanceSet()[0].getPublicIpAddresses()[0];
    }

    @Override
    public void stopServer() throws TencentCloudSDKException {
        StopInstancesRequest stopReq = new StopInstancesRequest();
        stopReq.setInstanceIds(new String[]{this.instanceId});
        stopReq.setStopType("SOFT_FIRST");
        stopReq.setStoppedMode("STOP_CHARGING");  // Apparently that's why we're here.
        StopInstancesResponse stopResp = this.client.StopInstances(stopReq);
        OnDemand.logger.info(StopInstancesResponse.toJsonString(stopResp));

        // Poll until the server is stopped (or timeout in 60s and raise an exception).
        pollUntilSuccessOrFailure();
    }

    private void pollUntilSuccessOrFailure() throws TencentCloudSDKException {
        DescribeInstancesResponse descResp;
        do {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
                return;
            }
            DescribeInstancesRequest descReq = new DescribeInstancesRequest();
            descReq.setInstanceIds(new String[]{this.instanceId});
            descResp = this.client.DescribeInstances(descReq);
            OnDemand.logger.info(DescribeInstancesResponse.toJsonString(descResp));
        } while (descResp.getInstanceSet()[0].getLatestOperationState().equals("OPERATING"));

        if (!descResp.getInstanceSet()[0].getLatestOperationState().equals("SUCCESS")) {
            throw new TencentCloudSDKException("Operation failed.");
        }
    }
}
