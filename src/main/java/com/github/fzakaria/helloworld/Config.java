package com.github.fzakaria.helloworld;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflow;
import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflowClient;
import com.amazonaws.services.simpleworkflow.model.Run;
import com.amazonaws.services.simpleworkflow.model.StartWorkflowExecutionRequest;
import com.amazonaws.services.simpleworkflow.model.WorkflowExecution;
import com.github.fzakaria.waterflow.Workflow;
import com.github.fzakaria.waterflow.converter.DataConverter;
import com.github.fzakaria.waterflow.converter.ImmutableJacksonDataConverter;
import com.github.fzakaria.waterflow.immutable.Domain;
import com.github.fzakaria.waterflow.immutable.Input;
import com.github.fzakaria.waterflow.immutable.TaskListName;
import com.github.fzakaria.waterflow.immutable.WorkflowId;
import com.github.fzakaria.waterflow.swf.SwfConstants;
import com.github.fzakaria.waterflow.swf.WorkflowExecutionRequestBuilder;

import java.time.Duration;
import java.util.Optional;

public class Config {

    public final Domain domain = Domain.of("swift");

    public final TaskListName taskListName = SwfConstants.DEFAULT_TASK_LIST;

    public final Integer numberOfWorkers = 2;

    public final DataConverter dataConverter = ImmutableJacksonDataConverter.builder().build();

    //SWF holds the connection for 60 seconds to see if a decision is available
    final Duration DEFAULT_CONNECTION_TIMEOUT = Duration.ofSeconds(60);
    final Duration DEFAULT_SOCKET_TIMEOUT = DEFAULT_CONNECTION_TIMEOUT.plusSeconds(10);
    public final AmazonSimpleWorkflow swf =  new AmazonSimpleWorkflowClient(new DefaultAWSCredentialsProviderChain(),
            new ClientConfiguration().withConnectionTimeout((int) DEFAULT_CONNECTION_TIMEOUT.toMillis())
                    .withSocketTimeout((int) DEFAULT_SOCKET_TIMEOUT.toMillis()));

    public WorkflowExecution submit(Workflow workflow, WorkflowId workflowId, Optional<Object> input) {
        Optional<Input> inputOptional = input.map( i -> dataConverter.toData(i)).map(Input::of);

        StartWorkflowExecutionRequest request =
                WorkflowExecutionRequestBuilder.builder().domain(domain)
                        .workflow(workflow).input(inputOptional)
                        .taskList(taskListName).workflowId(workflowId).build();

        Run run = swf.startWorkflowExecution(request);
        return new WorkflowExecution().withWorkflowId(workflowId.value()).withRunId(run.getRunId());
    }

    public <I, O> WorkflowExecution submit(Workflow<I,O> workflow, I input) {
        WorkflowId workflowId = WorkflowId.randomUniqueWorkflowId(workflow);
        return submit(workflow, workflowId, Optional.ofNullable(input));
    }
}
