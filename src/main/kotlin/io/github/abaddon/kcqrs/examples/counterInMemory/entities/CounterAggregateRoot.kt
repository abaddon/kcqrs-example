package io.github.abaddon.kcqrs.examples.counterInMemory.entities

import io.github.abaddon.kcqrs.core.domain.AggregateRoot
import io.github.abaddon.kcqrs.core.domain.messages.events.IDomainEvent
import io.github.abaddon.kcqrs.core.exceptions.HandlerForDomainEventNotFoundException
import io.github.abaddon.kcqrs.examples.counterInMemory.events.CounterDecreaseEvent
import io.github.abaddon.kcqrs.examples.counterInMemory.events.CounterIncreasedEvent
import io.github.abaddon.kcqrs.examples.counterInMemory.events.CounterInitialisedEvent
import io.github.abaddon.kcqrs.examples.counterInMemory.events.DomainErrorEvent
import org.slf4j.LoggerFactory

data class CounterAggregateRoot constructor(
    override val id: CounterAggregateId,
    override val version: Long = 0,
    val counter: Int,
    override val uncommittedEvents: MutableCollection<IDomainEvent> = mutableListOf()
) : AggregateRoot() {

    private val log = LoggerFactory.getLogger(this::class.simpleName)

    constructor(id: CounterAggregateId) : this(id, 0L, 0, ArrayList<IDomainEvent>())

    fun increaseCounter(incrementValue: Int): CounterAggregateRoot {
        return try {
            check(incrementValue >= 0 && incrementValue < Int.MAX_VALUE) { "Value $incrementValue not valid, it has to be >= 0 and < ${Int.MAX_VALUE}" }
            val updatedCounter = counter + incrementValue
            check(updatedCounter < Int.MAX_VALUE) { "Aggregate value $updatedCounter is not valid, it has to be < ${Int.MAX_VALUE}" }
            raiseEvent(CounterIncreasedEvent(id, incrementValue)) as CounterAggregateRoot
        } catch (e: Exception) {
            raiseEvent(DomainErrorEvent(id, e)) as CounterAggregateRoot
        }
    }

    fun decreaseCounter(decrementValue: Int): CounterAggregateRoot {
        return try {
            check(decrementValue >= 0 && decrementValue < Int.MAX_VALUE) { "Value $decrementValue not valid, it has to be >= 0 and < ${Int.MAX_VALUE}" }
            val updatedCounter = counter - decrementValue
            check(updatedCounter >= 0) { "Aggregate value $updatedCounter is not valid, it has to be >= 0" }

            raiseEvent(CounterDecreaseEvent(id, decrementValue)) as CounterAggregateRoot
        } catch (e: HandlerForDomainEventNotFoundException) {
            raiseEvent(DomainErrorEvent(id, e)) as CounterAggregateRoot
        }
    }

    private fun apply(event: CounterInitialisedEvent): CounterAggregateRoot {
        log.info("The aggregate is applying the event ${event::class.simpleName} with id ${event.messageId}")
        return copy(id = event.aggregateId, version = version + 1, counter = event.value)
    }

    private fun apply(event: CounterIncreasedEvent): CounterAggregateRoot {
        log.info("The aggregate is applying the event ${event::class.simpleName} with id ${event.messageId}")
        val newCounter = counter + event.value;
        return copy(counter = newCounter, version = version + 1)
    }

    private fun apply(event: CounterDecreaseEvent): CounterAggregateRoot {
        log.info("The aggregate is applying the event ${event::class.simpleName} with id ${event.messageId}")
        val newCounter = counter - event.value;
        return copy(counter = newCounter, version = version + 1)
    }

    private fun apply(event: DomainErrorEvent): CounterAggregateRoot {
        log.info("The aggregate is applying the event ${event::class.simpleName} with id ${event.messageId}")
        return copy(version = version + 1)
    }


    companion object {

        fun initialiseCounter(id: CounterAggregateId, initialValue: Int): CounterAggregateRoot {
            val emptyAggregate = CounterAggregateRoot(id,0,initialValue)
            return try {
                check(initialValue >= 0 && initialValue < Int.MAX_VALUE) { "Value $initialValue not valid, it has to be >= 0 and < ${Int.MAX_VALUE}" }
                emptyAggregate.raiseEvent(CounterInitialisedEvent(id, initialValue)) as CounterAggregateRoot
            } catch (e: Exception) {
                emptyAggregate.raiseEvent(DomainErrorEvent(id, e)) as CounterAggregateRoot
            }
        }
    }

}
