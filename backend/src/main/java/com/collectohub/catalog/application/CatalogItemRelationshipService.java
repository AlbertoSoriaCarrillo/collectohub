package com.collectohub.catalog.application;

import com.collectohub.auth.security.AuthenticatedUser;
import com.collectohub.catalog.domain.*;
import com.collectohub.catalog.dto.*;
import com.collectohub.catalog.infrastructure.CatalogItemRelationshipRepository;
import com.collectohub.catalog.infrastructure.CatalogItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

@Service
public class CatalogItemRelationshipService {
    private final CatalogItemRelationshipRepository repository;
    private final CatalogItemRepository itemRepository;

    public CatalogItemRelationshipService(CatalogItemRelationshipRepository repository,
            CatalogItemRepository itemRepository) {
        this.repository = repository;
        this.itemRepository = itemRepository;
    }

    @Transactional(readOnly = true)
    public List<CatalogItemRelationshipResponse> listRelationships(Long itemId, AuthenticatedUser user,
            String requestedStatus) {
        CatalogItem item = findItem(itemId);
        CatalogRecordStatus status = EditorialCatalogSupport.resolveRecordStatus(user, requestedStatus);
        if (!EditorialCatalogSupport.isEditorialAdmin(user) && !item.isPubliclyVisible()) {
            throw new CatalogItemNotFoundException(itemId);
        }
        return Stream.concat(
                        repository.findBySourceCatalogItem_IdAndDeletedAtIsNullOrderByRelationshipOrderAscIdAsc(itemId).stream(),
                        repository.findByTargetCatalogItem_IdAndDeletedAtIsNullOrderByRelationshipOrderAscIdAsc(itemId).stream())
                .filter(relationship -> relationship.getRecordStatus() == status)
                .filter(relationship -> EditorialCatalogSupport.isEditorialAdmin(user) || publiclyVisible(relationship))
                .sorted(relationshipComparator(itemId))
                .map(relationship -> CatalogItemRelationshipResponse.from(relationship, itemId))
                .toList();
    }

    @Transactional(readOnly = true)
    public CatalogItemRelationshipResponse get(Long itemId, Long relationshipId, AuthenticatedUser user,
            String requestedStatus) {
        CatalogItem item = findItem(itemId);
        CatalogRecordStatus status = EditorialCatalogSupport.resolveRecordStatus(user, requestedStatus);
        CatalogItemRelationship relationship = findRelationship(itemId, relationshipId);
        if (relationship.getRecordStatus() != status
                || !EditorialCatalogSupport.isEditorialAdmin(user) && (!item.isPubliclyVisible() || !publiclyVisible(relationship))) {
            throw new CatalogItemRelationshipNotFoundException(relationshipId);
        }
        return CatalogItemRelationshipResponse.from(relationship, itemId);
    }

    @Transactional
    public CatalogItemRelationshipResponse create(Long sourceItemId, AuthenticatedUser user,
            CreateCatalogItemRelationshipRequest request) {
        EditorialCatalogSupport.ensureEditorialAdmin(user);
        CatalogItem source = findItem(sourceItemId);
        CatalogItem target = findItem(request.targetCatalogItemId());
        ensureDifferent(source, target);
        ensureUnique(sourceItemId, target.getId(), request.relationshipType(), null);
        CatalogItemRelationship relationship = CatalogItemRelationship.create(
                source, target, request.relationshipType(), request.relationshipOrder() == null ? 1 : request.relationshipOrder(),
                EditorialCatalogSupport.normalizeNullable(request.description()),
                request.recordStatus() == null ? CatalogRecordStatus.DRAFT : request.recordStatus(), user.id());
        return CatalogItemRelationshipResponse.from(repository.save(relationship), sourceItemId);
    }

    @Transactional
    public CatalogItemRelationshipResponse update(Long sourceItemId, Long relationshipId, AuthenticatedUser user,
            UpdateCatalogItemRelationshipRequest request) {
        EditorialCatalogSupport.ensureEditorialAdmin(user);
        findItem(sourceItemId);
        CatalogItemRelationship relationship = findRelationshipAsSource(sourceItemId, relationshipId);
        CatalogItem target = request.targetCatalogItemId() == null
                ? relationship.getTargetCatalogItem() : findItem(request.targetCatalogItemId());
        ensureDifferent(relationship.getSourceCatalogItem(), target);
        ensureUnique(sourceItemId, target.getId(), request.relationshipType(), relationshipId);
        relationship.update(target, request.relationshipType(), request.relationshipOrder(),
                EditorialCatalogSupport.normalizeNullable(request.description()),
                request.recordStatus() == null ? relationship.getRecordStatus() : request.recordStatus(), user.id());
        return CatalogItemRelationshipResponse.from(relationship, sourceItemId);
    }

    @Transactional
    public void delete(Long sourceItemId, Long relationshipId, AuthenticatedUser user) {
        EditorialCatalogSupport.ensureEditorialAdmin(user);
        findItem(sourceItemId);
        findRelationshipAsSource(sourceItemId, relationshipId).softDelete(user.id());
    }

    private CatalogItem findItem(Long id) {
        return itemRepository.findByIdAndDeletedAtIsNull(id).orElseThrow(() -> new CatalogItemNotFoundException(id));
    }

    private CatalogItemRelationship findRelationship(Long itemId, Long relationshipId) {
        CatalogItemRelationship relationship = repository.findByIdAndDeletedAtIsNull(relationshipId)
                .orElseThrow(() -> new CatalogItemRelationshipNotFoundException(relationshipId));
        if (!relationship.getSourceCatalogItem().getId().equals(itemId)
                && !relationship.getTargetCatalogItem().getId().equals(itemId)) {
            throw new CatalogItemRelationshipNotFoundException(relationshipId);
        }
        return relationship;
    }

    private CatalogItemRelationship findRelationshipAsSource(Long sourceItemId, Long relationshipId) {
        CatalogItemRelationship relationship = findRelationship(sourceItemId, relationshipId);
        if (!relationship.getSourceCatalogItem().getId().equals(sourceItemId)) {
            throw new CatalogItemRelationshipNotFoundException(relationshipId);
        }
        return relationship;
    }

    private void ensureDifferent(CatalogItem source, CatalogItem target) {
        if (source.getId().equals(target.getId())) {
            throw new InvalidEditorialCatalogRequestException("Source and target catalog items must be different");
        }
    }

    private void ensureUnique(Long sourceId, Long targetId, CatalogItemRelationshipType type, Long excludedId) {
        boolean duplicate = excludedId == null
                ? repository.existsBySourceCatalogItem_IdAndTargetCatalogItem_IdAndRelationshipTypeAndDeletedAtIsNull(
                        sourceId, targetId, type)
                : repository.existsBySourceCatalogItem_IdAndTargetCatalogItem_IdAndRelationshipTypeAndDeletedAtIsNullAndIdNot(
                        sourceId, targetId, type, excludedId);
        if (duplicate) {
            throw new DuplicateEditorialCatalogException("catalog item relationship", "relationship already exists");
        }
    }

    private boolean publiclyVisible(CatalogItemRelationship relationship) {
        return relationship.isPubliclyVisible()
                && relationship.getSourceCatalogItem().isPubliclyVisible()
                && relationship.getTargetCatalogItem().isPubliclyVisible();
    }

    private Comparator<CatalogItemRelationship> relationshipComparator(Long itemId) {
        return Comparator.comparing(CatalogItemRelationship::getRelationshipOrder)
                .thenComparing(relationship -> relatedItem(relationship, itemId).getTitle(), String.CASE_INSENSITIVE_ORDER)
                .thenComparing(CatalogItemRelationship::getId);
    }

    private CatalogItem relatedItem(CatalogItemRelationship relationship, Long itemId) {
        return relationship.getSourceCatalogItem().getId().equals(itemId)
                ? relationship.getTargetCatalogItem() : relationship.getSourceCatalogItem();
    }
}
