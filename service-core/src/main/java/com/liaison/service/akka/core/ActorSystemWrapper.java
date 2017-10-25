package com.liaison.service.akka.core;

import akka.actor.Actor;
import akka.actor.ActorPath;
import akka.actor.ActorRef;
import akka.actor.ActorRefProvider;
import akka.actor.ActorSystem;
import akka.actor.ActorSystemImpl;
import akka.actor.Extension;
import akka.actor.ExtensionId;
import akka.actor.InternalActorRef;
import akka.actor.Props;
import akka.actor.Scheduler;
import akka.actor.Terminated;
import akka.dispatch.Dispatchers;
import akka.dispatch.Mailboxes;
import akka.event.EventStream;
import akka.event.LoggingAdapter;
import scala.Function0;
import scala.collection.Iterable;
import scala.concurrent.ExecutionContextExecutor;
import scala.concurrent.Future;

import java.util.List;

/**
 * A decorator for {@link ActorSystem} to add additional features while performing the same ActorSystem duties.
 */
public final class ActorSystemWrapper extends ActorSystem {

    public static final String CONFIG_AKKA_REMOTE_UNTRUSTED_MODE = "akka.remote.untrusted-mode";
    public static final String CONFIG_AKKA_REMOTE_TRUSTED_SELECTION_PATHS = "akka.remote.trusted-selection-paths";

    private final ActorSystem system;

    /**
     * Constructor for ActorSystemWrapper.
     *
     * @param system Original {@link ActorSystem} instance
     */
    public ActorSystemWrapper(ActorSystem system) {
        this.system = system;
    }

    @Override
    public String name() {
        return system.name();
    }

    @Override
    public Settings settings() {
        return system.settings();
    }

    @Override
    public void logConfiguration() {
        system.logConfiguration();
    }

    @Override
    public ActorPath $div(String name) {
        return system.$div(name);
    }

    @Override
    public ActorPath $div(Iterable<String> name) {
        return system.$div(name);
    }

    @Override
    public EventStream eventStream() {
        return system.eventStream();
    }

    @Override
    public LoggingAdapter log() {
        return system.log();
    }

    @Override
    public ActorRef deadLetters() {
        return system.deadLetters();
    }

    @Override
    public Scheduler scheduler() {
        return system.scheduler();
    }

    @Override
    public Dispatchers dispatchers() {
        return system.dispatchers();
    }

    @Override
    public ActorSystemImpl systemImpl() {
        return system.systemImpl();
    }

    @Override
    public ActorRefProvider provider() {
        return system.provider();
    }

    @Override
    public ExecutionContextExecutor dispatcher() {
        return system.dispatcher();
    }

    @Override
    public InternalActorRef guardian() {
        return system.guardian();
    }

    @Override
    public InternalActorRef lookupRoot() {
        return system.lookupRoot();
    }

    @Override
    public ActorRef actorOf(Props props) {
        return system.actorOf(props);
    }

    /**
     * Strictly checks if specified path is in the list of {@value CONFIG_AKKA_REMOTE_TRUSTED_SELECTION_PATHS}.
     * If so, then this method verifies if {@link Actor} is an instance of {@link EntryActor}
     * to prevent {@link Actor} without authorization scheme from being exposed via remoting.
     * Once all verifications are done, then returns {@link ActorRef} created by {@link ActorSystem}
     *
     * @param props {@link Actor} {@link Props}
     * @param name name of the {@link Actor}
     * @return {@link ActorRef} of specified {@link Actor}
     */
    @Override
    public ActorRef actorOf(Props props, String name) {
        List<String> trustedSelectionPaths = settings().config().getStringList(CONFIG_AKKA_REMOTE_TRUSTED_SELECTION_PATHS);
        ActorRef ref = system.actorOf(props, name);
        Class<? extends Actor> clazz = props.actorClass();
        String address = ref.path().toStringWithoutAddress();
        for (String path : trustedSelectionPaths) {
            if (path.equals(address) && !clazz.isAssignableFrom(EntryActor.class)) {
                throw new IllegalStateException(String.format("Exposed Actor [%s] at path [%s] must be an instance of %s to enforce authorization", clazz.getName(), path, EntryActor.class.getName()));
            }
        }
        return ref;
    }

    @Override
    public void stop(ActorRef actor) {
        system.stop(actor);
    }

    @Override
    public Mailboxes mailboxes() {
        return system.mailboxes();
    }

    @Override
    public <T> void registerOnTermination(Function0<T> code) {
        system.registerOnTermination(code);
    }

    @Override
    public void registerOnTermination(Runnable code) {
        system.registerOnTermination(code);
    }

    @Override
    public Future<Terminated> terminate() {
        return system.terminate();
    }

    @Override
    public Future<Terminated> whenTerminated() {
        return system.whenTerminated();
    }

    @Override
    public <T extends Extension> T registerExtension(ExtensionId<T> ext) {
        return system.registerExtension(ext);
    }

    @Override
    public <T extends Extension> T extension(ExtensionId<T> ext) {
        return system.extension(ext);
    }

    @Override
    public boolean hasExtension(ExtensionId<? extends Extension> ext) {
        return system.hasExtension(ext);
    }
}
