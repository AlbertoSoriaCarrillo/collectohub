package com.collectohub.catalog.application;

import com.collectohub.auth.security.AuthenticatedUser;
import com.collectohub.users.domain.Role;
import com.collectohub.users.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EditorialCatalogSupportTest {

    @Test
    void editorialAuthorizationAllowsOnlyAdminAndEditorialAdmin() {
        assertThat(EditorialCatalogSupport.isEditorialAdmin(user("ADMIN"))).isTrue();
        assertThat(EditorialCatalogSupport.isEditorialAdmin(user("EDITORIAL_ADMIN"))).isTrue();
        assertThat(EditorialCatalogSupport.isEditorialAdmin(user("USER"))).isFalse();
        assertThat(EditorialCatalogSupport.isEditorialAdmin(user("SHOP_OWNER"))).isFalse();
        assertThat(EditorialCatalogSupport.isEditorialAdmin(user("CONTENT_CREATOR"))).isFalse();
        assertThat(EditorialCatalogSupport.isEditorialAdmin(null)).isFalse();
    }

    @Test
    void editorialAuthorizationRejectsNonEditorialRoles() {
        EditorialCatalogSupport.ensureEditorialAdmin(user("ADMIN"));
        EditorialCatalogSupport.ensureEditorialAdmin(user("EDITORIAL_ADMIN"));

        assertThatThrownBy(() -> EditorialCatalogSupport.ensureEditorialAdmin(user("USER")))
                .isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> EditorialCatalogSupport.ensureEditorialAdmin(user("SHOP_OWNER")))
                .isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> EditorialCatalogSupport.ensureEditorialAdmin(user("CONTENT_CREATOR")))
                .isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> EditorialCatalogSupport.ensureEditorialAdmin(null))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void authenticatedUserExposesEditorialAdminAsRoleAndAuthority() {
        AuthenticatedUser user = user("EDITORIAL_ADMIN");

        assertThat(user.roles()).containsExactly("EDITORIAL_ADMIN");
        assertThat(user.getAuthorities()).extracting("authority").containsExactly("EDITORIAL_ADMIN");
    }

    private AuthenticatedUser user(String roleCode) {
        User user = User.register("editor@example.com", "hash", "Editor", new Role(roleCode, roleCode));
        return AuthenticatedUser.from(user);
    }
}
