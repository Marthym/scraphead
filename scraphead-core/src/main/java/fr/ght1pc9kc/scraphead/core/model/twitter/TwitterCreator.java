package fr.ght1pc9kc.scraphead.core.model.twitter;

public record TwitterCreator(
        String creator
) implements CardCreator {
    @Override
    public long id() {
        try {
            return Long.parseLong(creator);
        } catch (Exception e) {
            return -1;
        }
    }

    @Override
    public boolean isID() {
        return false;
    }
}
