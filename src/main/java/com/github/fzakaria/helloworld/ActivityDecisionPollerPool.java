package com.github.fzakaria.helloworld;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflow;
import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflowClient;
import com.github.fzakaria.waterflow.Workflow;
import com.github.fzakaria.waterflow.converter.DataConverter;
import com.github.fzakaria.waterflow.converter.ImmutableJacksonDataConverter;
import com.github.fzakaria.waterflow.immutable.Domain;
import com.github.fzakaria.waterflow.immutable.TaskListName;
import com.github.fzakaria.waterflow.poller.ActivityPollerPool;
import com.github.fzakaria.waterflow.poller.DecisionPollerPool;
import com.github.fzakaria.waterflow.poller.ImmutableActivityPollerPool;
import com.github.fzakaria.waterflow.poller.ImmutableDecisionPollerPool;
import com.github.fzakaria.waterflow.swf.SwfConstants;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;

public class ActivityDecisionPollerPool {

    protected static final Logger log = LoggerFactory.getLogger(ActivityDecisionPollerPool.class);

    private final Config config = new Config();

    private final  ActivityPollerPool activityPollerPool =
            ImmutableActivityPollerPool.builder().domain(config.domain)
                .taskList(config.taskListName)
                .service(new ScheduledThreadPoolExecutor(config.numberOfWorkers))
                .swf(config.swf)
                .dataConverter(config.dataConverter)
                .addActivities(new HelloWorldActivities()).build();

    private final  DecisionPollerPool decisionPollerPool =
            ImmutableDecisionPollerPool.builder().domain(config.domain)
                    .taskList(config.taskListName)
                    .service(new ScheduledThreadPoolExecutor(config.numberOfWorkers))
                    .swf(config.swf)
                    .dataConverter(config.dataConverter)
                    .workflows(Lists.newArrayList(new HelloWorldWorkflow())).build();


    public void start() {
        activityPollerPool.start();
        decisionPollerPool.start();
    }

    public void stop() {
        activityPollerPool.stop();
        decisionPollerPool.stop();
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        ActivityDecisionPollerPool activityAndDecisionPollerPool =
                new ActivityDecisionPollerPool();

        activityAndDecisionPollerPool.start();

        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                log.info("Shutting down pool and exiting.");
                activityAndDecisionPollerPool.stop();
            }
        });
        log.info("activity pollers started:");
    }
}
