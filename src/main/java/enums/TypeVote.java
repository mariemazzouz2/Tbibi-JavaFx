package enums;

public enum TypeVote {
    Like,
    Dislike,;

    // You can add methods if needed
    public boolean LIKE() {
        return this == Like;
    }

    public boolean DISLIKE() {
        return this == Dislike;
    }
}
