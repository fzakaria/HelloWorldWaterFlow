package com.github.fzakaria.helloworld;

import com.amazonaws.services.simpleworkflow.model.StartWorkflowExecutionRequest;
import com.github.fzakaria.waterflow.Workflow;
import com.github.fzakaria.waterflow.action.ActivityActions.StringActivityAction;
import com.github.fzakaria.waterflow.converter.DataConverter;
import com.github.fzakaria.waterflow.converter.ImmutableJacksonDataConverter;
import com.github.fzakaria.waterflow.immutable.ActionId;
import com.github.fzakaria.waterflow.immutable.DecisionContext;
import com.github.fzakaria.waterflow.immutable.Description;
import com.github.fzakaria.waterflow.immutable.Name;
import com.github.fzakaria.waterflow.immutable.Version;
import com.github.fzakaria.waterflow.swf.WorkflowExecutionRequestBuilder;
import com.google.common.reflect.TypeToken;
import org.immutables.value.Value;

import java.io.IOException;
import java.util.concurrent.CompletionStage;

@Value.Immutable
public class HelloWorldWorkflow extends Workflow<String,String> {

    @Override
    public Name name() {
        return Name.of("Hello World");
    }

    @Override
    public Version version() {
        return Version.of("1.0");
    }

    @Override
    public TypeToken<String> inputType() {
        return TypeToken.of(String.class);
    }

    @Override
    public TypeToken<String> outputType() {
        return TypeToken.of(String.class);
    }

    @Override
    public DataConverter dataConverter() {
        return ImmutableJacksonDataConverter.builder().build();
    }

    final StringActivityAction step1 = StringActivityAction.builder().actionId(ActionId.of("step1"))
            .name(Name.of("Hello World")).version(Version.of("1.0")).workflow(this).build();

    @Override
    public CompletionStage decide(DecisionContext decisionContext) {
        CompletionStage<String> input = workflowInput(decisionContext.events());

        return input.thenCompose(i -> step1.withInput(i).decide(decisionContext));

    }

    public static void main(String[] args) throws IOException, InterruptedException {
        Config config = new Config();

        HelloWorldWorkflow workflow = ImmutableHelloWorldWorkflow.builder()
                .description(Description.of("Starting my first workflow!")).build();

        config.submit(workflow, "Jane Doe");
    }

}
