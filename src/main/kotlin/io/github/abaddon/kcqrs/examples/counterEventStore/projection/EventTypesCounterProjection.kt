package io.github.abaddon.kcqrs.examples.counterEventStore.projection

import io.github.abaddon.kcqrs.core.domain.messages.events.IDomainEvent
import io.github.abaddon.kcqrs.core.projections.IProjection
import io.github.abaddon.kcqrs.core.projections.IProjectionKey
import io.github.abaddon.kcqrs.examples.counterEventStore.events.CounterDecreaseEvent
import io.github.abaddon.kcqrs.examples.counterEventStore.events.CounterIncreasedEvent
import org.slf4j.Logger
import org.slf4j.LoggerFactory

data class EventTypesCounterProjectionKey(val key: String) : IProjectionKey {
    override fun key(): String = key

}

data class EventTypesCounterProjection(
    override val key: EventTypesCounterProjectionKey,
    val numIncreasedEvent: Int,
    val numDecreaseEvent: Int,
) : IProjection {
    private val log: Logger = LoggerFactory.getLogger(this.javaClass.simpleName)

    override fun applyEvent(event: IDomainEvent): IProjection {
        log.info("applying event with messageId: ${event.messageId}")
        return when (event) {
            is CounterIncreasedEvent -> copy(numIncreasedEvent = this.numIncreasedEvent + 1)
            is CounterDecreaseEvent -> copy(numDecreaseEvent = this.numDecreaseEvent + 1)
            else -> this
        }
    }
}