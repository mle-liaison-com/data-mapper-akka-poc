package com.liaison.service.akka.core;

import akka.actor.ActorContext;
import akka.actor.ActorRef;
import akka.annotation.ApiMayChange;

/**
 * Extension of {@link java.util.function.Consumer}, with Actor customization
 *
 * @param <T> Message Type
 */
@ApiMayChange
public interface ActorMessageConsumer<T> {

    /**
     * Any actions to be taken with the message. Usually involves authorization/message routing
     *
     * @param message Original message
     * @param context ActorContext
     * @param sender Original sender
     * @throws Exception Any exception that maybe raised during authorization/message routing
     */
    void accept(T message, ActorContext context, ActorRef sender) throws Exception;
}
