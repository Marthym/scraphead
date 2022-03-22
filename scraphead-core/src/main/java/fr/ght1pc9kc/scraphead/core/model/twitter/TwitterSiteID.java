package fr.ght1pc9kc.scraphead.core.model.twitter;

public record TwitterSiteID(
        long id
) implements CardSite {
    @Override
    public String site() {
        return Long.toString(id);
    }

    @Override
    public boolean isID() {
        return true;
    }
}
