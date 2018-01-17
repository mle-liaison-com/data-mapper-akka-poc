package com.liaison.service.akka.bootstrap.cluster;

import akka.actor.AbstractActor;
import akka.cluster.Cluster;
import akka.cluster.ClusterEvent;
import akka.event.Logging;
import akka.event.LoggingAdapter;

public class SimpleClusterListener extends AbstractActor {

    private LoggingAdapter log = Logging.getLogger(getContext().system(), this);
    private Cluster cluster = Cluster.get(getContext().system());

    //subscribe to cluster changes
    @Override
    public void preStart() {
        cluster.subscribe(getSelf(), ClusterEvent.initialStateAsEvents(), ClusterEvent.MemberEvent.class,
                ClusterEvent.UnreachableMember.class);
    }

    //re-subscribe when restart
    @Override
    public void postStop() {
        cluster.unsubscribe(getSelf());
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(ClusterEvent.MemberUp.class, event -> log.info("Member is Up: {}", event.member()))
                .match(ClusterEvent.UnreachableMember.class, event -> log.info("Member detected as unreachable: {}", event.member()))
                .match(ClusterEvent.MemberRemoved.class, event -> log.info("Member is Removed: {}", event.member()))
                .match(ClusterEvent.MemberEvent.class, event -> { })
                .matchAny(this::unhandled)
                .build();
    }
}
