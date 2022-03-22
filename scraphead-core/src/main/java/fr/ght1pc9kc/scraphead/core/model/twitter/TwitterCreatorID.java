package fr.ght1pc9kc.scraphead.core.model.twitter;

public record TwitterCreatorID(
        long id
) implements CardCreator {
    @Override
    public String creator() {
        return Long.toString(id);
    }

    @Override
    public boolean isID() {
        return true;
    }
}
