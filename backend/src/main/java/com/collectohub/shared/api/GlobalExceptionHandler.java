package com.collectohub.shared.api;

import com.collectohub.auth.application.EmailAlreadyExistsException;
import com.collectohub.auth.application.UnsupportedInterfaceLanguageException;
import com.collectohub.catalog.application.DuplicateMasterProductException;
import com.collectohub.catalog.application.DuplicateEditorialCatalogException;
import com.collectohub.catalog.application.CatalogFranchiseNotFoundException;
import com.collectohub.catalog.application.CatalogSeriesNotFoundException;
import com.collectohub.catalog.application.CatalogItemNotFoundException;
import com.collectohub.catalog.application.CatalogItemEditionNotFoundException;
import com.collectohub.catalog.application.InvalidCatalogFilterException;
import com.collectohub.catalog.application.InvalidEditorialCatalogRequestException;
import com.collectohub.catalog.application.MasterProductNotFoundException;
import com.collectohub.catalog.application.ProductCategoryNotFoundException;
import com.collectohub.catalog.application.PublisherNotFoundException;
import com.collectohub.collections.application.CollectionItemNotFoundException;
import com.collectohub.collections.application.CollectionNotFoundException;
import com.collectohub.inventory.application.ShopProductNotFoundException;
import com.collectohub.reservations.application.InvalidReservationFilterException;
import com.collectohub.reservations.application.InvalidReservationRequestException;
import com.collectohub.reservations.application.InvalidReservationTransitionException;
import com.collectohub.reservations.application.ReservationNotFoundException;
import com.collectohub.reservations.application.ReservationUnavailableException;
import com.collectohub.shops.application.ShopNotFoundException;
import com.collectohub.shared.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<String> details = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .toList();
        return build(HttpStatus.BAD_REQUEST, "Validation failed", request, details);
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    ResponseEntity<ErrorResponse> handleEmailAlreadyExists(
            EmailAlreadyExistsException ex,
            HttpServletRequest request
    ) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), request, List.of());
    }

    @ExceptionHandler(DuplicateMasterProductException.class)
    ResponseEntity<ErrorResponse> handleDuplicateMasterProduct(
            DuplicateMasterProductException ex,
            HttpServletRequest request
    ) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), request, List.of());
    }

    @ExceptionHandler(DuplicateEditorialCatalogException.class)
    ResponseEntity<ErrorResponse> handleDuplicateEditorialCatalog(
            DuplicateEditorialCatalogException ex,
            HttpServletRequest request
    ) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), request, List.of());
    }

    @ExceptionHandler({
            ProductCategoryNotFoundException.class,
            InvalidCatalogFilterException.class,
            InvalidEditorialCatalogRequestException.class,
            InvalidReservationFilterException.class,
            InvalidReservationRequestException.class,
            UnsupportedInterfaceLanguageException.class
    })
    ResponseEntity<ErrorResponse> handleCatalogBadRequest(RuntimeException ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), request, List.of());
    }

    @ExceptionHandler(BadCredentialsException.class)
    ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex, HttpServletRequest request) {
        return build(HttpStatus.UNAUTHORIZED, "Invalid credentials", request, List.of());
    }

    @ExceptionHandler(ShopNotFoundException.class)
    ResponseEntity<ErrorResponse> handleShopNotFound(ShopNotFoundException ex, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), request, List.of());
    }

    @ExceptionHandler(MasterProductNotFoundException.class)
    ResponseEntity<ErrorResponse> handleMasterProductNotFound(
            MasterProductNotFoundException ex,
            HttpServletRequest request
    ) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), request, List.of());
    }

    @ExceptionHandler({
            PublisherNotFoundException.class,
            CatalogFranchiseNotFoundException.class,
            CatalogSeriesNotFoundException.class,
            CatalogItemNotFoundException.class,
            CatalogItemEditionNotFoundException.class
    })
    ResponseEntity<ErrorResponse> handleEditorialCatalogNotFound(
            RuntimeException ex,
            HttpServletRequest request
    ) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), request, List.of());
    }

    @ExceptionHandler(ShopProductNotFoundException.class)
    ResponseEntity<ErrorResponse> handleShopProductNotFound(
            ShopProductNotFoundException ex,
            HttpServletRequest request
    ) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), request, List.of());
    }

    @ExceptionHandler(CollectionNotFoundException.class)
    ResponseEntity<ErrorResponse> handleCollectionNotFound(CollectionNotFoundException ex, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), request, List.of());
    }

    @ExceptionHandler(CollectionItemNotFoundException.class)
    ResponseEntity<ErrorResponse> handleCollectionItemNotFound(
            CollectionItemNotFoundException ex,
            HttpServletRequest request
    ) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), request, List.of());
    }

    @ExceptionHandler(ReservationNotFoundException.class)
    ResponseEntity<ErrorResponse> handleReservationNotFound(
            ReservationNotFoundException ex,
            HttpServletRequest request
    ) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), request, List.of());
    }

    @ExceptionHandler({
            ReservationUnavailableException.class,
            InvalidReservationTransitionException.class
    })
    ResponseEntity<ErrorResponse> handleReservationConflict(RuntimeException ex, HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), request, List.of());
    }

    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        return build(HttpStatus.FORBIDDEN, "Access denied", request, List.of());
    }

    private ResponseEntity<ErrorResponse> build(
            HttpStatus status,
            String message,
            HttpServletRequest request,
            List<String> details
    ) {
        return ResponseEntity.status(status).body(ErrorResponse.of(
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI(),
                details
        ));
    }
}
