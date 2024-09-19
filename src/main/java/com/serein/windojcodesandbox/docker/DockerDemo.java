package com.serein.windojcodesandbox.docker;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.*;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.api.model.PullResponseItem;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.command.LogContainerResultCallback;

import java.util.Arrays;
import java.util.List;

/**
 * @author: serein
 * @date: 2024/9/12 15:48
 * @description: java操作docker的demo
 */
public class DockerDemo {

    public static void main(String[] args) throws InterruptedException {
        // 配置 Docker Client 使用特定的镜像仓库
//        DefaultDockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
//                .withRegistryUrl("https://yzetwj14.mirror.aliyuncs.com")
//                .build();
        // 获取配置好的 Docker Client
        DockerClient dockerClient = DockerClientBuilder.getInstance().build();
        PingCmd pingCmd = dockerClient.pingCmd();
        pingCmd.exec();

        // 1. 拉取镜像
        String image = "nginx:latest";
        PullImageCmd pullImageCmd = dockerClient.pullImageCmd(image);
        PullImageResultCallback pullImageResultCallback = new PullImageResultCallback() {
            @Override
            public void onNext(PullResponseItem item) {
                System.out.println("下载镜像中，处理：" + item.getStatus());
                super.onNext(item);
            }
        };
        pullImageCmd
                .exec(pullImageResultCallback)
                .awaitCompletion();
        System.out.println("下载完成");

        // 2. 创建容器
        CreateContainerCmd createContainerCmd = dockerClient.createContainerCmd(image);
        CreateContainerResponse createContainerResponse = createContainerCmd
                .withCmd("echo", "Hello Docker")
                .exec();
        System.out.println(createContainerResponse);
        String containerId = createContainerResponse.getId();

        // 3. 查看容器状态
        ListContainersCmd listContainersCmd = dockerClient.listContainersCmd();
        List<Container> containerList = listContainersCmd.withShowAll(true).exec();
        for (Container container : containerList) {
            System.out.println(container);
        }

        // 4. 启动容器
        dockerClient.startContainerCmd(containerId).exec();
        // Thread.sleep(5000L);

        // 5. 查看日志，exec中有一个回调函数，每读一批日志 输出一些内容，而不是读完后整体返回
        LogContainerResultCallback logContainerResultCallback = new LogContainerResultCallback() {
            @Override
            public void onNext(Frame item) {
                System.out.println(item.getStreamType());
                System.out.println("日志：" + new String(item.getPayload()));
                super.onNext(item);
            }
        };
        dockerClient
                .logContainerCmd(containerId)
                .withStdErr(true)
                .withStdOut(true)
                .exec(logContainerResultCallback)
                .awaitCompletion(); // 阻塞等待日志输出

        // 删除容器
//        dockerClient.removeContainerCmd(containerId).withForce(true).exec();

        // 删除镜像
//        dockerClient.removeImageCmd(image).exec();
    }
}
