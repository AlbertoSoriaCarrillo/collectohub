--liquibase formatted sql

--changeset codex:009-add-editorial-references-to-shop-products
ALTER TABLE shop_products
    ADD COLUMN catalog_item_id BIGINT,
    ADD COLUMN catalog_item_edition_id BIGINT,
    ADD COLUMN editorial_reference_source VARCHAR(40) NOT NULL DEFAULT 'LEGACY',
    ALTER COLUMN master_product_id DROP NOT NULL;

ALTER TABLE shop_products
    ADD CONSTRAINT fk_shop_products_catalog_item
        FOREIGN KEY (catalog_item_id) REFERENCES catalog_items (id),
    ADD CONSTRAINT fk_shop_products_catalog_item_edition
        FOREIGN KEY (catalog_item_edition_id) REFERENCES catalog_item_editions (id),
    ADD CONSTRAINT chk_shop_products_reference
        CHECK (master_product_id IS NOT NULL OR catalog_item_id IS NOT NULL),
    ADD CONSTRAINT chk_shop_products_edition_requires_item
        CHECK (catalog_item_edition_id IS NULL OR catalog_item_id IS NOT NULL),
    ADD CONSTRAINT chk_shop_products_editorial_reference_source
        CHECK (editorial_reference_source IN ('LEGACY', 'VERIFIED_BRIDGE', 'MANUAL_EDITORIAL'));

CREATE INDEX idx_shop_products_catalog_item_id
    ON shop_products (catalog_item_id);
CREATE INDEX idx_shop_products_catalog_item_edition_id
    ON shop_products (catalog_item_edition_id);
CREATE INDEX idx_shop_products_editorial_reference_source
    ON shop_products (editorial_reference_source);

UPDATE shop_products AS shop_product
SET catalog_item_id = verified_link.catalog_item_id,
    catalog_item_edition_id = verified_link.catalog_item_edition_id,
    editorial_reference_source = 'VERIFIED_BRIDGE'
FROM master_product_catalog_links AS verified_link
WHERE shop_product.deleted_at IS NULL
  AND shop_product.master_product_id = verified_link.master_product_id
  AND shop_product.catalog_item_id IS NULL
  AND verified_link.link_status = 'VERIFIED'
  AND verified_link.deleted_at IS NULL;

--rollback ALTER TABLE shop_products DROP CONSTRAINT IF EXISTS fk_shop_products_catalog_item, DROP CONSTRAINT IF EXISTS fk_shop_products_catalog_item_edition, DROP CONSTRAINT IF EXISTS chk_shop_products_reference, DROP CONSTRAINT IF EXISTS chk_shop_products_edition_requires_item, DROP CONSTRAINT IF EXISTS chk_shop_products_editorial_reference_source, DROP COLUMN IF EXISTS catalog_item_id, DROP COLUMN IF EXISTS catalog_item_edition_id, DROP COLUMN IF EXISTS editorial_reference_source;
