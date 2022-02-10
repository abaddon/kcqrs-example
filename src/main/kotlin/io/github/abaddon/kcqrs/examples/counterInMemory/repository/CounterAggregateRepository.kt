package io.github.abaddon.kcqrs.examples.counterInMemory.repository

import io.github.abaddon.kcqrs.core.IIdentity
import io.github.abaddon.kcqrs.core.domain.messages.events.IDomainEvent
import io.github.abaddon.kcqrs.core.persistence.EventStoreRepository
import io.github.abaddon.kcqrs.core.projections.IProjection
import io.github.abaddon.kcqrs.core.projections.IProjectionHandler
import io.github.abaddon.kcqrs.examples.counterInMemory.entities.CounterAggregateId
import io.github.abaddon.kcqrs.examples.counterInMemory.entities.CounterAggregateRoot
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class CounterAggregateRepository() : EventStoreRepository<CounterAggregateRoot>() {

    override val log: Logger = LoggerFactory.getLogger(this.javaClass.simpleName)

    private val storage = mutableMapOf<String, MutableList<IDomainEvent>>()

    override fun <TProjection : IProjection> subscribe(projectionHandler: IProjectionHandler<TProjection>) {
        TODO("Not yet implemented")
    }

    override fun emptyAggregate(aggregateId: IIdentity): CounterAggregateRoot =
        CounterAggregateRoot(aggregateId as CounterAggregateId, 0L, 0, ArrayList<IDomainEvent>())

    override fun aggregateIdStreamName(aggregateId: IIdentity): String = aggregateId.valueAsString()

    override fun load(streamName: String, startFrom: Long): List<IDomainEvent> {
        return storage.getOrDefault(streamName,listOf())
    }

    override fun persist(
        streamName: String,
        uncommittedEvents: List<IDomainEvent>,
        header: Map<String, String>,
        currentVersion: Long
    ) {
        val currentEvents=storage.getOrDefault(streamName,listOf()).toMutableList()
        currentEvents.addAll(uncommittedEvents.toMutableList())
        storage[streamName]= currentEvents
    }

    override fun publish(events: List<IDomainEvent>) {}
}