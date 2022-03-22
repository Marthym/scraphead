package fr.ght1pc9kc.scraphead.core.model.twitter;

public record TwitterSite(
        String site
) implements CardSite {
    @Override
    public long id() {
        try {
            return Long.parseLong(site);
        } catch (Exception e) {
            return -1;
        }
    }

    @Override
    public boolean isID() {
        return false;
    }
}
