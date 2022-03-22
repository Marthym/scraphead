package fr.ght1pc9kc.scraphead.core.model.twitter;

public sealed interface CardSite permits TwitterSite, TwitterSiteID {
    /**
     * Create a Twitter Site from {@code twitter:site}
     *
     * @param siteId The Twitter Site ID
     * @return The CardSite
     */
    static CardSite of(String siteId) {
        return new TwitterSite(siteId);
    }

    /**
     * Create a Twitter Site from {@code twitter:site:id}
     *
     * @param siteId The Twitter Site ID
     * @return The CardSite
     */
    static CardSite of(long siteId) {
        return new TwitterSiteID(siteId);
    }

    String site();

    /**
     * Return the Site as long ID if possible. Return {@code -1} if not
     *
     * @return The Site ID as long, {@code -1} if not possible.
     */
    long id();

    /**
     * Allow to known if the CardSite is a Site or a SiteID.
     * Depending on the meta in the head :
     * <ul>
     *     <li>{@code twitter:site} is a Site</li>
     *     <li>{@code twitter:site:id} give a SiteID</li>
     * </ul>
     *
     * @return {@code true} or {@code false}
     */
    boolean isID();
}
