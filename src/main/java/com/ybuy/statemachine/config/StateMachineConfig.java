package com.ybuy.statemachine.config;

import java.util.Optional;

import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.guard.Guard;
import org.springframework.statemachine.state.State;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.listener.StateMachineListener;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.transition.Transition;

import com.ybuy.statemachine.statemachine.Events;
import com.ybuy.statemachine.statemachine.States;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@EnableStateMachineFactory
public class StateMachineConfig extends EnumStateMachineConfigurerAdapter<States, Events> {

    @Override
    public void configure(StateMachineConfigurationConfigurer<States, Events> config) throws Exception {
        config.withConfiguration()
            .listener(listener())
            .autoStartup(true);
    }

    private StateMachineListener<States, Events> listener() {
        return new StateMachineListenerAdapter<States, Events>() {
            @Override
            public void eventNotAccepted(Message<Events> event) {
                log.error("Not accepted event: {}", event);
            }

            @Override
            public void transition(Transition<States, Events> transition) {
                log.warn("MOVE from: {} to: {}",
                    ofNullableState(transition.getSource()),
                    ofNullableState(transition.getTarget()));
            }

            private Object ofNullableState(State s) {
                return Optional.ofNullable(s)
                    .map(State::getId)
                    .orElse(null);
            }
        };
    }

    @Override
    public void configure(StateMachineStateConfigurer<States, Events> states) throws Exception{
        states.withStates()
            .initial(States.BACKLOG)
            .state(States.IN_PROGRESS)
            .state(States.TESTING)
            .end(States.DONE);
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<States, Events> transition) throws Exception{
        transition.withExternal()
            .source(States.BACKLOG)
            .target(States.IN_PROGRESS)
            .event(Events.START_FEATURE)

            .and()
            .withExternal()
            .source(States.IN_PROGRESS)
            .target(States.TESTING)
            .event(Events.FINISH_FEATURE)
            .guard(checkDeployGuard())

            .and()
            .withExternal()
            .source(States.TESTING)
            .target(States.IN_PROGRESS)
            .event(Events.QA_TEAM_REJECT)

            .and()
            .withExternal()
            .source(States.TESTING)
            .target(States.DONE)
            .event(Events.QA_TEAM_APPROVE)

            .and()
            .withExternal()
            .source(States.BACKLOG)
            .target(States.TESTING)
            .guard(checkDeployGuard())
            .event(Events.ROCK_STAR_MAKE_ALL_IN_ONE)

            .and()
            .withInternal()
            .source(States.BACKLOG)
            .event(Events.DEPLOY)
            .action(deployAction())

            .and()
            .withInternal()
            .source(States.IN_PROGRESS)
            .event(Events.DEPLOY)
            .action(deployAction())

        ;

    }

    private Guard<States, Events> checkDeployGuard() {
        return stateContext -> {
            Boolean flag = (Boolean) stateContext.getExtendedState()
                                                .getVariables()
                                                .get("deployed");
            return flag == null ? false : flag;
        };
    }

    private Action<States, Events> deployAction() {
        return stateContext -> {
            log.warn("DEPLOYING: {}", stateContext.getEvent());
            stateContext.getExtendedState()
                        .getVariables()
                        .put("deployed", true);
        };
    }


}
