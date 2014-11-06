package be.vlaanderen.awv.atom.java;

import be.vlaanderen.awv.atom.FeedPosition;
import be.vlaanderen.awv.atom.FeedProcessingException;

public class FeedProcessor<E> {

    private final be.vlaanderen.awv.atom.FeedProcessor underlying;

    public FeedProcessor(FeedProvider<E> feedProvider, EntryConsumer<E> entryConsumer) {
        underlying = new be.vlaanderen.awv.atom.FeedProcessor<E>(
            new FeedProviderWrapper<E>(feedProvider),
            new EntryConsumerWrapper<E>(entryConsumer)
        );
    }

    public void start() throws FeedProcessingException {
        underlying.start().get();
    }

}