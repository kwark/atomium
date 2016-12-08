package be.wegenenverkeer.atomium.api;

import be.wegenenverkeer.atomium.format.Link;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Karel Maesen, Geovise BVBA on 05/12/16.
 */
public class FeedPageBuilder<T> {

    final private FeedPageProvider<T> provider;
    final private long page;
    private List<Entry<T>> pageEntries;
    private OffsetDateTime updated;
    private List<Link> links;
    private boolean hasPrevious;

    public FeedPageBuilder(FeedPageProvider<T> provider, long pageNum) {
        this.page = pageNum;
        this.provider = provider;
    }

    /**
     * Pass the entries for the page, orderd from most recent to least recent
     * <p>
     * <p>For purposes of determining links, there are usually more entries passsed. We take pageSize of the oldest</p>
     *
     * @param entries a list of entries
     * @return
     */
    public FeedPageBuilder<T> setEntries(List<Entry<T>> entries) {
        pageEntries = entries;
        Collections.reverse(pageEntries);
        checkForPreviousLink();
        selectOldestForPage();
        calcUpdated();
        calcLinks();
        return this;
    }

    private void selectOldestForPage() {
        long leastIndex = Math.max(0, pageEntries.size() - this.provider.getPageSize());
        pageEntries = pageEntries.subList((int)leastIndex, this.pageEntries.size());
    }

    private void calcUpdated() {
        updated = pageEntries.isEmpty() ? OffsetDateTime.now() : pageEntries.get(0).getUpdated();
    }

    private void checkForPreviousLink() {
        hasPrevious = (pageEntries.size() > provider.getPageSize());
    }


    private void calcLinks() {
        links = new ArrayList<>();
        String suffix = "/" + this.provider.getPageSize();
        links.add(new Link(Link.SELF, "/" + this.page + suffix));
        links.add(new Link(Link.LAST, "/0" + suffix));
        if (page > 0) {
            links.add(new Link(Link.NEXT, "/" + (page - 1) + suffix));
        }
        if (hasPrevious) {
            links.add(new Link(Link.PREVIOUS, "/" + (page + 1) + suffix));
        }

    }

    public FeedPage<T> build() {
        return new FeedPage<>(
                this.provider.getFeedName(),
                this.provider.getFeedUrl(),
                this.provider.getFeedName(),
                this.provider.getFeedGenerator(),
                updated,
                links,
                pageEntries
        );
    }


}