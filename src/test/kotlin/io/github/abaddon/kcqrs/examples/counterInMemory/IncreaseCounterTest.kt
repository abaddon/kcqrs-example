package io.github.abaddon.kcqrs.examples.counterInMemory

import io.github.abaddon.kcqrs.core.IIdentity
import io.github.abaddon.kcqrs.core.domain.messages.events.IDomainEvent
import io.github.abaddon.kcqrs.examples.counterInMemory.commands.IncreaseCounterCommand
import io.github.abaddon.kcqrs.examples.counterInMemory.entities.CounterAggregateId
import io.github.abaddon.kcqrs.examples.counterInMemory.entities.CounterAggregateRoot
import io.github.abaddon.kcqrs.examples.counterInMemory.events.CounterIncreasedEvent
import io.github.abaddon.kcqrs.examples.counterInMemory.events.CounterInitialisedEvent
import io.github.abaddon.kcqrs.test.KcqrsAggregateTestSpecification
import java.util.*

class IncreaseCounterTest: KcqrsAggregateTestSpecification<CounterAggregateRoot>() {

    override val aggregateId: CounterAggregateId = CounterAggregateId(UUID.randomUUID())
    private val initialValue = 5
    private val incrementValue = 2

    override fun given(): List<IDomainEvent> {
        return listOf(
            CounterInitialisedEvent(aggregateId,initialValue),
        )
    }

    override fun `when`(): IncreaseCounterCommand {
        return IncreaseCounterCommand(aggregateId,incrementValue)
    }

    override fun expected(): List<IDomainEvent> {
        return listOf(CounterIncreasedEvent(aggregateId,incrementValue))
    }

    override fun expectedException(): Exception? {
        return null
    }

    override fun emptyAggregate(): (IIdentity) -> CounterAggregateRoot ={
        CounterAggregateRoot(it as CounterAggregateId)
    }

    override fun streamNameRoot(): String = "IncreaseCounterTest"
}