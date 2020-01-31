package com.ybuy.statemachine.config;

import java.util.UUID;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.statemachine.StateMachinePersist;
import org.springframework.statemachine.persist.DefaultStateMachinePersister;
import org.springframework.statemachine.persist.StateMachinePersister;

import com.ybuy.statemachine.persist.InMemoryStateMachinePersist;
import com.ybuy.statemachine.statemachine.Events;
import com.ybuy.statemachine.statemachine.States;

@Configuration
public class PersistConfig {

    @Bean
    @Profile({"in-memory", "default"})
    public StateMachinePersist<States, Events, UUID> inMemoryPersist() {
        return new InMemoryStateMachinePersist();
    }

    @Bean
    public StateMachinePersister<States, Events, UUID> persister(StateMachinePersist<States, Events, UUID> defaultPersist) {
        return new DefaultStateMachinePersister<>(defaultPersist);
    }
}
