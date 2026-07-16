package com.practice.mealplanner.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class SyncResult {
    private final int totalFetched;
    private final int savedCount;
    private final String source;
    private final String message;
    private final LocalDateTime syncedAt;
}
