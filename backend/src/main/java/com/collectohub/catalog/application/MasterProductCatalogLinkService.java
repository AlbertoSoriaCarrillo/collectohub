package com.collectohub.catalog.application;

import com.collectohub.auth.security.AuthenticatedUser;
import com.collectohub.catalog.domain.CatalogItem;
import com.collectohub.catalog.domain.CatalogItemEdition;
import com.collectohub.catalog.domain.MasterProduct;
import com.collectohub.catalog.domain.MasterProductCatalogLink;
import com.collectohub.catalog.domain.MasterProductCatalogLinkSource;
import com.collectohub.catalog.domain.MasterProductCatalogLinkStatus;
import com.collectohub.catalog.dto.CreateMasterProductCatalogLinkRequest;
import com.collectohub.catalog.dto.MasterProductCatalogLinkResponse;
import com.collectohub.catalog.dto.UpdateMasterProductCatalogLinkRequest;
import com.collectohub.catalog.infrastructure.CatalogItemEditionRepository;
import com.collectohub.catalog.infrastructure.CatalogItemRepository;
import com.collectohub.catalog.infrastructure.MasterProductCatalogLinkRepository;
import com.collectohub.catalog.infrastructure.MasterProductRepository;
import com.collectohub.shared.dto.PageResponse;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Set;

@Service
public class MasterProductCatalogLinkService {

    private static final Set<String> SORT_FIELDS = Set.of(
            "createdAt", "updatedAt", "linkStatus", "linkSource", "confidenceScore"
    );

    private final MasterProductCatalogLinkRepository linkRepository;
    private final MasterProductRepository masterProductRepository;
    private final CatalogItemRepository itemRepository;
    private final CatalogItemEditionRepository editionRepository;

    public MasterProductCatalogLinkService(
            MasterProductCatalogLinkRepository linkRepository,
            MasterProductRepository masterProductRepository,
            CatalogItemRepository itemRepository,
            CatalogItemEditionRepository editionRepository
    ) {
        this.linkRepository = linkRepository;
        this.masterProductRepository = masterProductRepository;
        this.itemRepository = itemRepository;
        this.editionRepository = editionRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<MasterProductCatalogLinkResponse> search(
            AuthenticatedUser user,
            Long masterProductId,
            Long catalogItemId,
            Long catalogItemEditionId,
            String status,
            String source,
            int page,
            int size,
            String sort
    ) {
        EditorialCatalogSupport.ensureAdmin(user);
        MasterProductCatalogLinkStatus parsedStatus = EditorialCatalogSupport.parseOptionalEnum(
                status, MasterProductCatalogLinkStatus.class, "linkStatus");
        MasterProductCatalogLinkSource parsedSource = EditorialCatalogSupport.parseOptionalEnum(
                source, MasterProductCatalogLinkSource.class, "linkSource");
        PageRequest pageRequest = EditorialCatalogSupport.pageRequest(
                page, size, sort, "createdAt", SORT_FIELDS);
        Specification<MasterProductCatalogLink> specification = (root, query, builder) ->
                builder.isNull(root.get("deletedAt"));

        if (masterProductId != null) {
            specification = specification.and((root, query, builder) ->
                    builder.equal(root.get("masterProduct").get("id"), masterProductId));
        }
        if (catalogItemId != null) {
            specification = specification.and((root, query, builder) ->
                    builder.equal(root.get("catalogItem").get("id"), catalogItemId));
        }
        if (catalogItemEditionId != null) {
            specification = specification.and((root, query, builder) ->
                    builder.equal(root.get("catalogItemEdition").get("id"), catalogItemEditionId));
        }
        if (parsedStatus != null) {
            specification = specification.and((root, query, builder) ->
                    builder.equal(root.get("linkStatus"), parsedStatus));
        }
        if (parsedSource != null) {
            specification = specification.and((root, query, builder) ->
                    builder.equal(root.get("linkSource"), parsedSource));
        }
        return PageResponse.from(
                linkRepository.findAll(specification, pageRequest).map(MasterProductCatalogLinkResponse::from)
        );
    }

    @Transactional(readOnly = true)
    public MasterProductCatalogLinkResponse get(Long id, AuthenticatedUser user) {
        EditorialCatalogSupport.ensureAdmin(user);
        return MasterProductCatalogLinkResponse.from(findLink(id));
    }

    @Transactional
    public MasterProductCatalogLinkResponse create(
            AuthenticatedUser user,
            CreateMasterProductCatalogLinkRequest request
    ) {
        EditorialCatalogSupport.ensureAdmin(user);
        MasterProduct masterProduct = findMasterProduct(request.masterProductId());
        CatalogItem item = findItem(request.catalogItemId());
        CatalogItemEdition edition = findEdition(request.catalogItemEditionId());
        validateEditionBelongsToItem(item, edition);
        ensureAvailable(masterProduct.getId(), item.getId(), edition, request.linkStatus(), null);

        MasterProductCatalogLink link = MasterProductCatalogLink.create(
                masterProduct, item, edition, request.linkStatus(), request.linkSource(),
                request.confidenceScore(), normalize(request.matchReason()), normalize(request.reviewNote()), user.id()
        );
        return MasterProductCatalogLinkResponse.from(linkRepository.save(link));
    }

    @Transactional
    public MasterProductCatalogLinkResponse update(
            Long id,
            AuthenticatedUser user,
            UpdateMasterProductCatalogLinkRequest request
    ) {
        EditorialCatalogSupport.ensureAdmin(user);
        MasterProductCatalogLink link = findLink(id);
        CatalogItem item = findItem(request.catalogItemId());
        CatalogItemEdition edition = findEdition(request.catalogItemEditionId());
        validateEditionBelongsToItem(item, edition);
        ensureAvailable(link.getMasterProduct().getId(), item.getId(), edition, request.linkStatus(), id);
        link.update(
                item, edition, request.linkStatus(), request.linkSource(), request.confidenceScore(),
                normalize(request.matchReason()), normalize(request.reviewNote()), user.id()
        );
        return MasterProductCatalogLinkResponse.from(link);
    }

    @Transactional
    public MasterProductCatalogLinkResponse verify(Long id, AuthenticatedUser user) {
        EditorialCatalogSupport.ensureAdmin(user);
        MasterProductCatalogLink link = findLink(id);
        ensureVerifiedAvailable(link.getMasterProduct().getId(), id);
        link.changeStatus(MasterProductCatalogLinkStatus.VERIFIED, user.id());
        return MasterProductCatalogLinkResponse.from(link);
    }

    @Transactional
    public MasterProductCatalogLinkResponse reject(Long id, AuthenticatedUser user) {
        EditorialCatalogSupport.ensureAdmin(user);
        MasterProductCatalogLink link = findLink(id);
        link.changeStatus(MasterProductCatalogLinkStatus.REJECTED, user.id());
        return MasterProductCatalogLinkResponse.from(link);
    }

    @Transactional
    boolean createProposal(
            MasterProduct masterProduct,
            CatalogItem item,
            CatalogItemEdition edition,
            MasterProductCatalogLinkSource source,
            BigDecimal confidence,
            String reason,
            Long actorId
    ) {
        if (linkRepository.existsByMasterProduct_IdAndLinkStatusAndDeletedAtIsNull(
                masterProduct.getId(), MasterProductCatalogLinkStatus.VERIFIED)) {
            return false;
        }
        if (isExactDuplicate(
                masterProduct.getId(), item.getId(), edition,
                MasterProductCatalogLinkStatus.PROPOSED, null)) {
            return false;
        }
        linkRepository.save(MasterProductCatalogLink.create(
                masterProduct, item, edition, MasterProductCatalogLinkStatus.PROPOSED,
                source, confidence, reason, null, actorId
        ));
        return true;
    }

    private MasterProductCatalogLink findLink(Long id) {
        return linkRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new MasterProductCatalogLinkNotFoundException(id));
    }

    private MasterProduct findMasterProduct(Long id) {
        return masterProductRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new MasterProductNotFoundException(id));
    }

    private CatalogItem findItem(Long id) {
        return itemRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new CatalogItemNotFoundException(id));
    }

    private CatalogItemEdition findEdition(Long id) {
        if (id == null) {
            return null;
        }
        return editionRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new CatalogItemEditionNotFoundException(id));
    }

    private void validateEditionBelongsToItem(CatalogItem item, CatalogItemEdition edition) {
        if (edition != null && !edition.getCatalogItem().getId().equals(item.getId())) {
            throw new InvalidEditorialCatalogRequestException(
                    "Catalog item edition does not belong to the selected catalog item"
            );
        }
    }

    private void ensureAvailable(
            Long masterProductId,
            Long itemId,
            CatalogItemEdition edition,
            MasterProductCatalogLinkStatus status,
            Long excludedId
    ) {
        if (status == MasterProductCatalogLinkStatus.VERIFIED) {
            ensureVerifiedAvailable(masterProductId, excludedId);
        }
        if (isExactDuplicate(masterProductId, itemId, edition, status, excludedId)) {
            throw new DuplicateEditorialCatalogException("master product catalog link", "link already exists");
        }
    }

    private void ensureVerifiedAvailable(Long masterProductId, Long excludedId) {
        boolean exists = excludedId == null
                ? linkRepository.existsByMasterProduct_IdAndLinkStatusAndDeletedAtIsNull(
                        masterProductId, MasterProductCatalogLinkStatus.VERIFIED)
                : linkRepository.existsByMasterProduct_IdAndLinkStatusAndDeletedAtIsNullAndIdNot(
                        masterProductId, MasterProductCatalogLinkStatus.VERIFIED, excludedId);
        if (exists) {
            throw new DuplicateEditorialCatalogException(
                    "master product catalog link", "verified link already exists for master product"
            );
        }
    }

    private boolean isExactDuplicate(
            Long masterProductId,
            Long itemId,
            CatalogItemEdition edition,
            MasterProductCatalogLinkStatus status,
            Long excludedId
    ) {
        if (edition == null) {
            return excludedId == null
                    ? linkRepository.existsByMasterProduct_IdAndCatalogItem_IdAndCatalogItemEditionIsNullAndLinkStatusAndDeletedAtIsNull(
                            masterProductId, itemId, status)
                    : linkRepository.existsByMasterProduct_IdAndCatalogItem_IdAndCatalogItemEditionIsNullAndLinkStatusAndDeletedAtIsNullAndIdNot(
                            masterProductId, itemId, status, excludedId);
        }
        return excludedId == null
                ? linkRepository.existsByMasterProduct_IdAndCatalogItem_IdAndCatalogItemEdition_IdAndLinkStatusAndDeletedAtIsNull(
                        masterProductId, itemId, edition.getId(), status)
                : linkRepository.existsByMasterProduct_IdAndCatalogItem_IdAndCatalogItemEdition_IdAndLinkStatusAndDeletedAtIsNullAndIdNot(
                        masterProductId, itemId, edition.getId(), status, excludedId);
    }

    private String normalize(String value) {
        return EditorialCatalogSupport.normalizeNullable(value);
    }
}
