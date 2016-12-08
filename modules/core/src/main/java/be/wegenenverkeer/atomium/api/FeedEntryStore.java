package be.wegenenverkeer.atomium.api;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Future;

/**
 * Created by Karel Maesen, Geovise BVBA on 19/11/16.
 */
public interface FeedEntryStore<T> {

    void push(List<Entry<T>> entries);

    default void push(Entry<T>... entries) {
        push(Arrays.asList(entries));
    }

    Publisher<Entry<T>> getEntries(long startNum, long size);

    CompletableFuture<Long> totalNumberOfEntries();

}