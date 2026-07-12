package com.collectohub.catalog.application;

import com.collectohub.auth.security.AuthenticatedUser;
import com.collectohub.catalog.domain.*;
import com.collectohub.catalog.dto.*;
import com.collectohub.catalog.infrastructure.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class CatalogItemCreatorService {
    private final CatalogItemCreatorRepository repository;
    private final CatalogItemRepository itemRepository;
    private final CreatorService creatorService;

    public CatalogItemCreatorService(CatalogItemCreatorRepository repository, CatalogItemRepository itemRepository,
                                     CreatorService creatorService) {
        this.repository = repository; this.itemRepository = itemRepository; this.creatorService = creatorService;
    }

    @Transactional(readOnly = true)
    public List<CatalogItemCreatorResponse> listPublic(Long itemId) {
        CatalogItem item = findItem(itemId);
        if (!item.isPubliclyVisible()) throw new CatalogItemNotFoundException(itemId);
        return repository.findByCatalogItem_IdAndDeletedAtIsNullOrderByCreditOrderAscCreator_NameAscIdAsc(itemId)
                .stream().filter(credit -> credit.getCreator().isPubliclyVisible())
                .map(CatalogItemCreatorResponse::from).toList();
    }

    @Transactional
    public CatalogItemCreatorResponse create(Long itemId, AuthenticatedUser user, CreateCatalogItemCreatorRequest request) {
        EditorialCatalogSupport.ensureEditorialAdmin(user);
        CatalogItem item = findItem(itemId);
        Creator creator = creatorService.find(request.creatorId());
        ensureUnique(itemId, creator.getId(), request.creditRole(), null);
        CatalogItemCreator credit = CatalogItemCreator.create(item, creator, request.creditRole(), request.creditOrder(),
                EditorialCatalogSupport.normalizeNullable(request.creditLabel()), user.id());
        return CatalogItemCreatorResponse.from(repository.save(credit));
    }

    @Transactional
    public CatalogItemCreatorResponse update(Long itemId, Long creditId, AuthenticatedUser user,
                                             UpdateCatalogItemCreatorRequest request) {
        EditorialCatalogSupport.ensureEditorialAdmin(user);
        findItem(itemId);
        CatalogItemCreator credit = findCredit(itemId, creditId);
        ensureUnique(itemId, credit.getCreator().getId(), request.creditRole(), creditId);
        credit.update(request.creditRole(), request.creditOrder(),
                EditorialCatalogSupport.normalizeNullable(request.creditLabel()), user.id());
        return CatalogItemCreatorResponse.from(credit);
    }

    @Transactional
    public void delete(Long itemId, Long creditId, AuthenticatedUser user) {
        EditorialCatalogSupport.ensureEditorialAdmin(user);
        findItem(itemId);
        findCredit(itemId, creditId).softDelete(user.id());
    }

    private CatalogItem findItem(Long id) {
        return itemRepository.findByIdAndDeletedAtIsNull(id).orElseThrow(() -> new CatalogItemNotFoundException(id));
    }
    private CatalogItemCreator findCredit(Long itemId, Long id) {
        return repository.findByIdAndCatalogItem_IdAndDeletedAtIsNull(id, itemId)
                .orElseThrow(() -> new CatalogItemCreatorNotFoundException(id));
    }
    private void ensureUnique(Long itemId, Long creatorId, CreatorCreditRole role, Long excludedId) {
        boolean duplicate = excludedId == null
                ? repository.existsByCatalogItem_IdAndCreator_IdAndCreditRoleAndDeletedAtIsNull(itemId, creatorId, role)
                : repository.existsByCatalogItem_IdAndCreator_IdAndCreditRoleAndDeletedAtIsNullAndIdNot(
                        itemId, creatorId, role, excludedId);
        if (duplicate) throw new DuplicateEditorialCatalogException("catalog item creator", "credit already exists");
    }
}
