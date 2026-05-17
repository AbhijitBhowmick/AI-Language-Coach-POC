package com.platform.diagnostic.dto;

import java.util.List;

public record ValidationResult(
    boolean valid,
    List<String> errors,
    List<String> warnings
) {
    public static ValidationResult success() {
        return new ValidationResult(true, List.of(), List.of());
    }

    public static ValidationResult failure(String error) {
        return new ValidationResult(false, List.of(error), List.of());
    }

    public static ValidationResult failure(List<String> errors) {
        return new ValidationResult(false, errors, List.of());
    }

    public static ValidationResult withWarnings(boolean valid, List<String> warnings) {
        return new ValidationResult(valid, List.of(), warnings);
    }
}