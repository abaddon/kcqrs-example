package io.github.abaddon.kcqrs.examples.counterInMemory

import io.github.abaddon.kcqrs.core.IIdentity
import io.github.abaddon.kcqrs.core.domain.messages.events.IDomainEvent
import io.github.abaddon.kcqrs.examples.counterInMemory.commands.InitialiseCounterCommand
import io.github.abaddon.kcqrs.examples.counterInMemory.entities.CounterAggregateId
import io.github.abaddon.kcqrs.examples.counterInMemory.entities.CounterAggregateRoot
import io.github.abaddon.kcqrs.examples.counterInMemory.events.CounterInitialisedEvent
import io.github.abaddon.kcqrs.test.KcqrsAggregateTestSpecification
import java.util.*

class InitialiseCounterTest: KcqrsAggregateTestSpecification<CounterAggregateRoot>() {

    override val aggregateId: CounterAggregateId = CounterAggregateId(UUID.randomUUID())
    private val messageId = UUID.randomUUID()
    private val initialValue = 5

    override fun given(): List<IDomainEvent> {
        return listOf()
    }

    override fun `when`(): InitialiseCounterCommand {
        return InitialiseCounterCommand(aggregateId,initialValue)
    }

    override fun expected(): List<IDomainEvent> {
        return listOf(CounterInitialisedEvent(aggregateId,initialValue))
    }

    override fun expectedException(): Exception? {
        return null
    }

    override fun emptyAggregate(): (IIdentity) -> CounterAggregateRoot ={
        CounterAggregateRoot(it as CounterAggregateId)
    }

    override fun streamNameRoot(): String = "InitialiseCounterTest"}