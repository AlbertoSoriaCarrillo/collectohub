package com.collectohub.catalog.application;

import com.collectohub.auth.security.AuthenticatedUser;
import com.collectohub.catalog.domain.*;
import com.collectohub.catalog.dto.*;
import com.collectohub.catalog.infrastructure.*;
import com.collectohub.users.domain.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CatalogItemCreatorServiceTest {
    @Mock CatalogItemCreatorRepository repository;
    @Mock CatalogItemRepository itemRepository;
    @Mock CreatorService creatorService;
    CatalogItemCreatorService service;
    AuthenticatedUser admin;
    CatalogItem item;
    Creator creator;

    @BeforeEach void setUp() {
        service = new CatalogItemCreatorService(repository, itemRepository, creatorService);
        admin = user(); item = mock(CatalogItem.class); creator = mock(Creator.class);
        lenient().when(item.getId()).thenReturn(20L); lenient().when(creator.getId()).thenReturn(30L);
    }

    @Test void createsCredit() {
        when(itemRepository.findByIdAndDeletedAtIsNull(20L)).thenReturn(Optional.of(item));
        when(creatorService.find(30L)).thenReturn(creator);
        when(creator.getName()).thenReturn("Akira"); when(creator.getSlug()).thenReturn("akira");
        when(repository.save(any())).thenAnswer(i -> withId(i.getArgument(0), 40L));
        var response = service.create(20L, admin,
                new CreateCatalogItemCreatorRequest(30L, CreatorCreditRole.AUTHOR, 1, "Story"));
        assertThat(response.creditRole()).isEqualTo("AUTHOR");
    }

    @Test void rejectsMissingItemOrCreator() {
        when(itemRepository.findByIdAndDeletedAtIsNull(20L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.create(20L, admin,
                new CreateCatalogItemCreatorRequest(30L, CreatorCreditRole.AUTHOR, 1, null)))
                .isInstanceOf(CatalogItemNotFoundException.class);
    }

    @Test void rejectsDuplicateCredit() {
        when(itemRepository.findByIdAndDeletedAtIsNull(20L)).thenReturn(Optional.of(item));
        when(creatorService.find(30L)).thenReturn(creator);
        when(repository.existsByCatalogItem_IdAndCreator_IdAndCreditRoleAndDeletedAtIsNull(
                20L, 30L, CreatorCreditRole.AUTHOR)).thenReturn(true);
        assertThatThrownBy(() -> service.create(20L, admin,
                new CreateCatalogItemCreatorRequest(30L, CreatorCreditRole.AUTHOR, 1, null)))
                .isInstanceOf(DuplicateEditorialCatalogException.class);
    }

    @Test void publicListFiltersInactiveCreatorsAndPreservesRepositoryOrder() {
        CatalogItemCreator visible = credit(40L, CreatorCreditRole.AUTHOR, 1);
        Creator hiddenCreator = mock(Creator.class);
        CatalogItemCreator hidden = withId(CatalogItemCreator.create(item, hiddenCreator,
                CreatorCreditRole.EDITOR, 2, null, 1L), 41L);
        when(itemRepository.findByIdAndDeletedAtIsNull(20L)).thenReturn(Optional.of(item));
        when(item.isPubliclyVisible()).thenReturn(true);
        when(creator.isPubliclyVisible()).thenReturn(true);
        when(creator.getName()).thenReturn("Akira"); when(creator.getSlug()).thenReturn("akira");
        when(hiddenCreator.isPubliclyVisible()).thenReturn(false);
        when(repository.findByCatalogItem_IdAndDeletedAtIsNullOrderByCreditOrderAscCreator_NameAscIdAsc(20L))
                .thenReturn(List.of(visible, hidden));
        assertThat(service.listPublic(20L)).extracting("id").containsExactly(40L);
    }

    @Test void updatesAndSoftDeletesCredit() {
        CatalogItemCreator credit = credit(40L, CreatorCreditRole.AUTHOR, 1);
        when(itemRepository.findByIdAndDeletedAtIsNull(20L)).thenReturn(Optional.of(item));
        when(repository.findByIdAndCatalogItem_IdAndDeletedAtIsNull(40L, 20L)).thenReturn(Optional.of(credit));
        when(creator.getName()).thenReturn("Akira"); when(creator.getSlug()).thenReturn("akira");
        var updated = service.update(20L, 40L, admin,
                new UpdateCatalogItemCreatorRequest(CreatorCreditRole.WRITER, 2, "Script"));
        assertThat(updated.creditRole()).isEqualTo("WRITER");
        service.delete(20L, 40L, admin);
        assertThat(credit.isActive()).isFalse();
    }

    private CatalogItemCreator credit(Long id, CreatorCreditRole role, int order) {
        return withId(CatalogItemCreator.create(item, creator, role, order, null, 1L), id);
    }
    private AuthenticatedUser user() {
        User user = User.register("admin@example.com", "hash", "Admin", new Role("ADMIN", "ADMIN"));
        ReflectionTestUtils.setField(user, "id", 1L); return AuthenticatedUser.from(user);
    }
    private <T> T withId(T value, Long id) { ReflectionTestUtils.setField(value, "id", id); return value; }
}
