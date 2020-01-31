package com.ybuy.statemachine.persist;

import java.util.HashMap;
import java.util.UUID;

import org.springframework.statemachine.StateMachineContext;
import org.springframework.statemachine.StateMachinePersist;

import com.ybuy.statemachine.statemachine.Events;
import com.ybuy.statemachine.statemachine.States;

public class InMemoryStateMachinePersist implements StateMachinePersist<States, Events, UUID> {

    private HashMap<UUID, StateMachineContext<States,Events>> storage = new HashMap<>();

    @Override
    public void write(final StateMachineContext<States, Events> context, final UUID contextObj) throws Exception {
        storage.put(contextObj, context);
    }

    @Override
    public StateMachineContext<States, Events> read(final UUID contextObj) throws Exception {
        return storage.get(contextObj);
    }
}
