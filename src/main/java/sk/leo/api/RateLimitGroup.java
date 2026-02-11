package sk.leo.api;

import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

public enum RateLimitGroup {
    TWELVE_DATA;

    public Set<ServiceCallType> getCallTypes() {
        return EnumSet.allOf(ServiceCallType.class).stream()
                .filter(t -> t.getRateLimitGroup() == this)
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(ServiceCallType.class)));
    }
}
