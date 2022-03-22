package fr.ght1pc9kc.scraphead.core.model.twitter;

public sealed interface CardCreator permits TwitterCreator, TwitterCreatorID {
    /**
     * Create a Twitter Site from {@code twitter:site}
     *
     * @param creatorId The Twitter Site ID
     * @return The CardSite
     */
    static CardCreator of(String creatorId) {
        return new TwitterCreator(creatorId);
    }

    /**
     * Create a Twitter Site from {@code twitter:site:id}
     *
     * @param creatorId The Twitter Site ID
     * @return The CardSite
     */
    static CardCreator of(long creatorId) {
        return new TwitterCreatorID(creatorId);
    }

    String creator();

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
