package com.kainax00.simcitymod.data.enums; // [변경됨] 패키지 경로

import java.util.Arrays;

public enum PermissionLevel {
    NONE(0),
    ADMIN(1);

    private final int id;

    PermissionLevel(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static PermissionLevel fromId(int id) {
        if (id >= 1) return ADMIN;
        return Arrays.stream(values())
                .filter(level -> level.id == id)
                .findFirst()
                .orElse(NONE);
    }
}