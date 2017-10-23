package com.liaison.service.akka.core;

import akka.actor.Status;
import akka.annotation.ApiMayChange;
import com.liaison.service.akka.core.model.RoleBasedMessage;

/**
 * Actor that is used to perform any authentication/authorization using the message.
 * This should be the ONLY actor that is exposed via <a href="https://doc.akka.io/docs/akka/current/java/remoting.html">Actor Remoting</a>.
 * Otherwise, we may be exposing any actors for remoting and may let malicious messages to invoke service actors.
 *
 * Once the message is verified, service writer is free to do whatever they want with it.
 */
@ApiMayChange
public final class EntryActor extends BaseActor {

    private final ActorMessageConsumer<RoleBasedMessage> consumer;

    /**
     * Constructor for EntryActor. Post-action consumer needs to be passed in.
     *
     * @param consumer {@link ActorMessageConsumer} that defines post-actions to be taken once authorization is done
     */
    public EntryActor(ActorMessageConsumer<RoleBasedMessage> consumer) {
        this.consumer = consumer;
    }

    /**
     * Allows only {@link RoleBasedMessage}. All other messages will be rejected.
     *
     * @return Receive
     */
    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(RoleBasedMessage.class, msg -> consumer.accept(msg, getContext(), getSender()))
                .matchAny(any -> {
                    Exception exception = new IllegalStateException("Inbound message must extend " + RoleBasedMessage.class.getName());
                    getSender().tell(new Status.Failure(exception), getSelf());
                    throw exception;
                })
                .build();
    }
}
