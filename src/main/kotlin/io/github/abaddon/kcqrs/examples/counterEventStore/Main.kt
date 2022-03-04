package io.github.abaddon.kcqrs.examples.counterEventStore

import com.eventstore.dbclient.Position
import io.github.abaddon.kcqrs.core.domain.SimpleAggregateCommandHandler
import io.github.abaddon.kcqrs.core.domain.messages.commands.Command
import io.github.abaddon.kcqrs.core.domain.messages.events.IDomainEvent
import io.github.abaddon.kcqrs.core.persistence.InMemoryProjectionRepository
import io.github.abaddon.kcqrs.eventstoredb.config.EventStoreDBConfig
import io.github.abaddon.kcqrs.eventstoredb.config.SubscriptionFilterConfig
import io.github.abaddon.kcqrs.eventstoredb.eventstore.EventStoreDBRepository
import io.github.abaddon.kcqrs.eventstoredb.eventstore.EventStoreDBRepositoryConfig
import io.github.abaddon.kcqrs.eventstoredb.projection.EventStoreProjectionHandler
import io.github.abaddon.kcqrs.examples.counterEventStore.commands.DecreaseCounterCommand
import io.github.abaddon.kcqrs.examples.counterEventStore.commands.IncreaseCounterCommand
import io.github.abaddon.kcqrs.examples.counterEventStore.commands.InitialiseCounterCommand
import io.github.abaddon.kcqrs.examples.counterEventStore.entities.CounterAggregateId
import io.github.abaddon.kcqrs.examples.counterEventStore.entities.CounterAggregateRoot
import io.github.abaddon.kcqrs.examples.counterEventStore.projection.EventTypesCounterProjection
import io.github.abaddon.kcqrs.examples.counterEventStore.projection.EventTypesCounterProjectionKey
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.test.assertEquals

const val streamName = "stream-counterEventStore"
const val eventStoreDBConnectionString = "esdb://127.0.0.1:2113?tls=false&tlsVerifyCert=false"
val eventTypesCounterProjectionKey = EventTypesCounterProjectionKey("eventTypesCounter_view")
val log: Logger = LoggerFactory.getLogger("Main")


fun main() {

    //EventStore Setup
    val eventStoreDBConfig = EventStoreDBConfig(eventStoreDBConnectionString)
    val repository = EventStoreDBRepository(
        EventStoreDBRepositoryConfig(eventStoreDBConfig, streamName, 500, 500)){
        CounterAggregateRoot(CounterAggregateId(), 0L, 0, ArrayList<IDomainEvent>())
    }

    //Projection Repository setup
    val projectionRepository = InMemoryProjectionRepository<EventTypesCounterProjection>{
        EventTypesCounterProjection(it as EventTypesCounterProjectionKey,0,0)
    }

    //Projection Handler setup
    val subscriptionFilterConfig = SubscriptionFilterConfig(
        SubscriptionFilterConfig.SUBSCRIPTION_FILTER_STREAM_NAME_PREFIX,
        "$streamName."
    )
    val eventStoreProjectionHandler = EventStoreProjectionHandler<EventTypesCounterProjection>(
        projectionRepository,
        eventTypesCounterProjectionKey,
        subscriptionFilterConfig,
        Position.END
    )

    //Subscribe the Handler
    repository.subscribe(eventStoreProjectionHandler)


    //Aggregate Handler setup
    val aggregateHandler = SimpleAggregateCommandHandler<CounterAggregateRoot>(repository)
    val counterAggregateId = CounterAggregateId()

    val commandsToSend = listOf(
        IncreaseCounterCommand(counterAggregateId, 1),
        IncreaseCounterCommand(counterAggregateId, 2),
        IncreaseCounterCommand(counterAggregateId, 3),
        IncreaseCounterCommand(counterAggregateId, 4),
        DecreaseCounterCommand(counterAggregateId, 5),
        IncreaseCounterCommand(counterAggregateId, 6),
        DecreaseCounterCommand(counterAggregateId, 7),
    )

    runBlocking {

        //Launch a coroutine to sent commands to the aggregate
        launch { //Coroutine to sent commands to the aggregate
            aggregateHandler.handle(InitialiseCounterCommand(counterAggregateId, 10))
            sendCommands(commandsToSend, aggregateHandler, 1000)
        }

        //Launch a coroutine to print the projection every 500ms to see how it's updated when new events are published
        launch { //Projection querying
            (0..15).forEach { _ ->
                delay(500)
                printProjection(projectionRepository, eventTypesCounterProjectionKey)
            }

        }
    }

    val projection = runBlocking {
        return@runBlocking projectionRepository.getByKey(eventTypesCounterProjectionKey)
    }

    assertEquals(commandsToSend.filterIsInstance<IncreaseCounterCommand>().count(), projection.numIncreasedEvent)
    assertEquals(commandsToSend.filterIsInstance<DecreaseCounterCommand>().count(), projection.numDecreaseEvent)

}

suspend fun sendCommands(
    commands: List<Command<CounterAggregateRoot>>,
    aggregateHandler: SimpleAggregateCommandHandler<CounterAggregateRoot>,
    delayMs: Long = 2000
) {
    var counter = 0;
    commands.forEach { command ->
        counter++;
        aggregateHandler.handle(command)
        log.info("Command num $counter with ID sent: ${command.messageId}")
        delay(delayMs)
    }
}

suspend fun printProjection(
    projectionRepository: InMemoryProjectionRepository<EventTypesCounterProjection>,
    dummyProjectionKey: EventTypesCounterProjectionKey
) {
    val projection = projectionRepository.getByKey(dummyProjectionKey)
    val numDecreaseEvent = stringPadding(projection.numDecreaseEvent.toString(), 24)
    val numIncreasedEvent = stringPadding(projection.numIncreasedEvent.toString(), 24)
    log.info(
        "Current ${dummyProjectionKey.key} Projection : \n" +
                "   +------------------------+------------------------+\n" +
                "   |    NumDecreaseEvent    |    NumIncreasedEvent   |\n" +
                "   +------------------------+------------------------+\n" +
                "   |${numDecreaseEvent}|${numIncreasedEvent}|\n" +
                "   +------------------------+------------------------+\n"
    )

}

fun stringPadding(text: String, sizeExpected: Int): String {
    require(sizeExpected > text.count())
    val margin = (sizeExpected - text.count()) / 2
    check(margin > 0)
    return text.padStart(margin).padEnd(sizeExpected)
}


