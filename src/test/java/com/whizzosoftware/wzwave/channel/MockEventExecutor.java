/*
 *******************************************************************************
 * Copyright (c) 2016 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************
 */
package com.whizzosoftware.wzwave.channel;

import io.netty.util.concurrent.*;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class MockEventExecutor implements EventExecutor {
    @Override
    public boolean isShuttingDown() {
        return false;
    }

    @Override
    public Future<?> shutdownGracefully() {
        return null;
    }

    @Override
    public Future<?> shutdownGracefully(long l, long l1, TimeUnit timeUnit) {
        return null;
    }

    @Override
    public Future<?> terminationFuture() {
        return null;
    }

    @Override
    public void shutdown() {

    }

    @Override
    public List<Runnable> shutdownNow() {
        return null;
    }

    @Override
    public boolean isShutdown() {
        return false;
    }

    @Override
    public boolean isTerminated() {
        return false;
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return false;
    }

    @Override
    public EventExecutor next() {
        return null;
    }

    @Override
    public Iterator<EventExecutor> iterator() {
        return null;
    }

    @Override
    public Future<?> submit(Runnable runnable) {
        return null;
    }

    @Override
    public <T> List<java.util.concurrent.Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
        return null;
    }

    @Override
    public <T> List<java.util.concurrent.Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException {
        return null;
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
        return null;
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return null;
    }

    @Override
    public <T> Future<T> submit(Runnable runnable, T t) {
        return null;
    }

    @Override
    public <T> Future<T> submit(Callable<T> callable) {
        return null;
    }

    @Override
    public ScheduledFuture<?> schedule(Runnable runnable, long l, TimeUnit timeUnit) {
        return null;
    }

    @Override
    public <V> ScheduledFuture<V> schedule(Callable<V> callable, long l, TimeUnit timeUnit) {
        return null;
    }

    @Override
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable runnable, long l, long l1, TimeUnit timeUnit) {
        return null;
    }

    @Override
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable runnable, long l, long l1, TimeUnit timeUnit) {
        return null;
    }

    @Override
    public EventExecutorGroup parent() {
        return null;
    }

    @Override
    public boolean inEventLoop() {
        return false;
    }

    @Override
    public boolean inEventLoop(Thread thread) {
        return false;
    }

    @Override
    public <V> Promise<V> newPromise() {
        return null;
    }

    @Override
    public <V> ProgressivePromise<V> newProgressivePromise() {
        return null;
    }

    @Override
    public <V> Future<V> newSucceededFuture(V v) {
        return null;
    }

    @Override
    public <V> Future<V> newFailedFuture(Throwable throwable) {
        return null;
    }

    @Override
    public void execute(Runnable command) {

    }
}
