package com.collectohub.catalog.application;

import com.collectohub.auth.security.AuthenticatedUser;
import com.collectohub.catalog.domain.CatalogRecordStatus;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;

import java.util.Locale;
import java.util.Set;

public final class EditorialCatalogSupport {

    private static final String ADMIN_ROLE = "ADMIN";
    private static final String EDITORIAL_ADMIN_ROLE = "EDITORIAL_ADMIN";
    private static final int MAX_PAGE_SIZE = 100;

    private EditorialCatalogSupport() {
    }

    static void ensureEditorialAdmin(AuthenticatedUser user) {
        if (!isEditorialAdmin(user)) {
            throw new AccessDeniedException("Only editorial administrators can manage the editorial catalog");
        }
    }

    static boolean isAdmin(AuthenticatedUser user) {
        return user != null && user.roles().contains(ADMIN_ROLE);
    }

    static boolean isEditorialAdmin(AuthenticatedUser user) {
        return user != null && (user.roles().contains(ADMIN_ROLE) || user.roles().contains(EDITORIAL_ADMIN_ROLE));
    }

    static CatalogRecordStatus resolveRecordStatus(AuthenticatedUser user, String requestedStatus) {
        if (requestedStatus == null || requestedStatus.isBlank()) {
            return CatalogRecordStatus.ACTIVE;
        }
        if (!isEditorialAdmin(user)) {
            throw new AccessDeniedException("recordStatus filter requires editorial administrator authority");
        }
        return parseEnum(requestedStatus, CatalogRecordStatus.class, "recordStatus");
    }

    static <E extends Enum<E>> E parseOptionalEnum(String value, Class<E> enumType, String filterName) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return parseEnum(value, enumType, filterName);
    }

    static PageRequest pageRequest(
            int page,
            int size,
            String sort,
            String defaultField,
            Set<String> allowedFields
    ) {
        if (page < 0) {
            throw new InvalidCatalogFilterException("page must be greater than or equal to 0");
        }
        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw new InvalidCatalogFilterException("size must be between 1 and " + MAX_PAGE_SIZE);
        }

        String normalizedSort = sort == null || sort.isBlank() ? defaultField + ",asc" : sort.trim();
        String[] parts = normalizedSort.split(",", -1);
        if (parts.length > 2 || parts[0].isBlank() || !allowedFields.contains(parts[0])) {
            throw new InvalidCatalogFilterException("Unsupported sort: " + normalizedSort);
        }

        Sort.Direction direction = Sort.Direction.ASC;
        if (parts.length == 2) {
            try {
                direction = Sort.Direction.fromString(parts[1]);
            } catch (IllegalArgumentException ex) {
                throw new InvalidCatalogFilterException("Unsupported sort direction: " + parts[1]);
            }
        }
        return PageRequest.of(page, size, Sort.by(direction, parts[0]));
    }

    static String normalizeRequired(String value) {
        return value.trim();
    }

    static String normalizeNullable(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    static String normalizeCountry(String value) {
        String normalized = normalizeNullable(value);
        return normalized == null ? null : normalized.toUpperCase(Locale.ROOT);
    }

    static String normalizeLanguage(String value) {
        String normalized = normalizeNullable(value);
        return normalized == null ? null : normalized.toLowerCase(Locale.ROOT);
    }

    static String normalizeSlug(String value) {
        return normalizeRequired(value).toLowerCase(Locale.ROOT);
    }

    private static <E extends Enum<E>> E parseEnum(String value, Class<E> enumType, String filterName) {
        try {
            return Enum.valueOf(enumType, value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new InvalidCatalogFilterException("Unsupported " + filterName + ": " + value);
        }
    }
}
