package com.ybuy.statemachine;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.persist.StateMachinePersister;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.ybuy.statemachine.statemachine.Events;
import com.ybuy.statemachine.statemachine.States;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class StatemachineApplicationTests {

	@Autowired
	private StateMachineFactory<States, Events> stateMachineFactory;

	@Autowired
	private StateMachinePersister<States, Events, UUID> persister;

	private StateMachine<States, Events> stateMachine;


	@BeforeEach
	public void setUp()  {
		stateMachine = stateMachineFactory.getStateMachine();
	}

	@Test
	void initTest() {
		assertThat(stateMachine.getState().getId()).isEqualTo(States.BACKLOG);
		assertThat(stateMachine).isNotNull();
	}

	@Test
	void testGreenFlow() {
		//Arrange and act
		stateMachine.sendEvent(Events.START_FEATURE);
		stateMachine.sendEvent(Events.DEPLOY);
		stateMachine.sendEvent(Events.FINISH_FEATURE);
		stateMachine.sendEvent(Events.QA_TEAM_APPROVE);

		//assert
		assertThat(stateMachine.getState().getId()).isEqualTo(States.DONE);
	}



	@Test
	void testWrongWay() {
		// Arrange
		// Act
		stateMachine.sendEvent(Events.START_FEATURE);
		stateMachine.sendEvent(Events.QA_TEAM_APPROVE);
		// Asserts
		assertThat(stateMachine.getState().getId())
			.isEqualTo(States.IN_PROGRESS);
	}


	@Test
	void testRockStar() {
		// Arrange
		// Act
		stateMachine.sendEvent(Events.DEPLOY);
		stateMachine.sendEvent(Events.ROCK_STAR_MAKE_ALL_IN_ONE);

		assertThat(stateMachine.getState().getId()).isEqualTo(States.TESTING);
	}

	@Test
	void testGuard() {
		// Arrange and act
		stateMachine.sendEvent(Events.START_FEATURE);
		stateMachine.sendEvent(Events.FINISH_FEATURE);
		stateMachine.sendEvent(Events.QA_TEAM_APPROVE);	// not accepted

		assertThat(stateMachine.getState().getId()).isEqualTo(States.IN_PROGRESS);
	}

	@Test
	public void testPersist() throws Exception {
		// Arrange
		StateMachine<States, Events> firstStateMachine = stateMachineFactory.getStateMachine();

		StateMachine<States, Events> secondStateMachine = stateMachineFactory.getStateMachine();

		firstStateMachine.sendEvent(Events.START_FEATURE);
		firstStateMachine.sendEvent(Events.DEPLOY);

		// precondition
		assertThat(secondStateMachine.getState().getId()).isEqualTo(States.BACKLOG);

		// Act
		persister.persist(firstStateMachine, firstStateMachine.getUuid());
		persister.restore(secondStateMachine, firstStateMachine.getUuid());

		// Asserts
		assertThat(secondStateMachine.getState().getId()).isEqualTo(States.IN_PROGRESS);
	}

}
