package com.android.masterdistributormdl.gskDistributor.swipyrefresh;

public enum SwipyDirection {

    TOP(0),
    BOTTOM(1),
    BOTH(2);

    private final int mValue;

    SwipyDirection(int value) {
        this.mValue = value;
    }

    public static SwipyDirection getFromInt(int value) {
        for (SwipyDirection direction : SwipyDirection.values()) {
            if (direction.mValue == value) {
                return direction;
            }
        }
        return BOTH;
    }

}
