package net.starly.shopplus.message.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum MessageType {
    NORMAL("messages"),
    ERROR("errorMessages");

    @Getter
    private final String path;
}
