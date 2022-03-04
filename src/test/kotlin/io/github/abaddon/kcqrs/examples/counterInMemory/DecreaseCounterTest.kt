package io.github.abaddon.kcqrs.examples.counterInMemory

import io.github.abaddon.kcqrs.core.IIdentity
import io.github.abaddon.kcqrs.core.domain.messages.commands.ICommand
import io.github.abaddon.kcqrs.core.domain.messages.events.IDomainEvent
import io.github.abaddon.kcqrs.examples.counterInMemory.commands.DecreaseCounterCommand
import io.github.abaddon.kcqrs.examples.counterInMemory.entities.CounterAggregateId
import io.github.abaddon.kcqrs.examples.counterInMemory.entities.CounterAggregateRoot
import io.github.abaddon.kcqrs.examples.counterInMemory.events.CounterDecreaseEvent
import io.github.abaddon.kcqrs.examples.counterInMemory.events.CounterIncreasedEvent
import io.github.abaddon.kcqrs.examples.counterInMemory.events.CounterInitialisedEvent
import io.github.abaddon.kcqrs.test.KcqrsAggregateTestSpecification
import java.util.*

class DecreaseCounterTest() : KcqrsAggregateTestSpecification<CounterAggregateRoot>() {

    override val aggregateId: CounterAggregateId = CounterAggregateId(UUID.randomUUID())
    private val initialValue = 5
    private val incrementValue = 2
    private val decrementValue = 3


    override fun given(): List<IDomainEvent> {
        return listOf(
            CounterInitialisedEvent(aggregateId,initialValue),
            CounterIncreasedEvent(aggregateId,incrementValue)
        )
    }

    override fun `when`(): ICommand<CounterAggregateRoot> {
        return DecreaseCounterCommand(aggregateId,decrementValue)
    }

    override fun expected(): List<IDomainEvent> {
        return listOf(CounterDecreaseEvent(aggregateId,decrementValue))
    }

    override fun expectedException(): Exception? {
        return null
    }

    override fun emptyAggregate(): (IIdentity) -> CounterAggregateRoot ={
        CounterAggregateRoot(it as CounterAggregateId)
    }

    override fun streamNameRoot(): String = "DecreaseCounterTest"
}