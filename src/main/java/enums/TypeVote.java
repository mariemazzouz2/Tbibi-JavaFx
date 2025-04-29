package enums;

public enum TypeVote {
    Like,
    Dislike,;

    // You can add methods if needed
    public boolean isUpvote() {
        return this == Like;
    }

    public boolean isDownvote() {
        return this == Dislike;
    }
}
