--liquibase formatted sql

--changeset codex:008-add-editorial-references-to-collection-items
ALTER TABLE collection_items
    ADD COLUMN catalog_item_id BIGINT,
    ADD COLUMN catalog_item_edition_id BIGINT,
    ADD COLUMN editorial_reference_source VARCHAR(40) NOT NULL DEFAULT 'LEGACY',
    ALTER COLUMN master_product_id DROP NOT NULL;

ALTER TABLE collection_items
    ADD CONSTRAINT fk_collection_items_catalog_item
        FOREIGN KEY (catalog_item_id) REFERENCES catalog_items (id),
    ADD CONSTRAINT fk_collection_items_catalog_item_edition
        FOREIGN KEY (catalog_item_edition_id) REFERENCES catalog_item_editions (id),
    ADD CONSTRAINT chk_collection_items_reference
        CHECK (master_product_id IS NOT NULL OR catalog_item_id IS NOT NULL),
    ADD CONSTRAINT chk_collection_items_edition_requires_item
        CHECK (catalog_item_edition_id IS NULL OR catalog_item_id IS NOT NULL),
    ADD CONSTRAINT chk_collection_items_editorial_reference_source
        CHECK (editorial_reference_source IN ('LEGACY', 'VERIFIED_BRIDGE', 'MANUAL_EDITORIAL'));

CREATE INDEX idx_collection_items_catalog_item_id
    ON collection_items (catalog_item_id);
CREATE INDEX idx_collection_items_catalog_item_edition_id
    ON collection_items (catalog_item_edition_id);
CREATE INDEX idx_collection_items_editorial_reference_source
    ON collection_items (editorial_reference_source);

UPDATE collection_items AS collection_item
SET catalog_item_id = verified_link.catalog_item_id,
    catalog_item_edition_id = verified_link.catalog_item_edition_id,
    editorial_reference_source = 'VERIFIED_BRIDGE'
FROM master_product_catalog_links AS verified_link
WHERE collection_item.deleted_at IS NULL
  AND collection_item.master_product_id = verified_link.master_product_id
  AND collection_item.catalog_item_id IS NULL
  AND verified_link.link_status = 'VERIFIED'
  AND verified_link.deleted_at IS NULL;

--rollback ALTER TABLE collection_items DROP CONSTRAINT IF EXISTS fk_collection_items_catalog_item, DROP CONSTRAINT IF EXISTS fk_collection_items_catalog_item_edition, DROP CONSTRAINT IF EXISTS chk_collection_items_reference, DROP CONSTRAINT IF EXISTS chk_collection_items_edition_requires_item, DROP CONSTRAINT IF EXISTS chk_collection_items_editorial_reference_source, DROP COLUMN IF EXISTS catalog_item_id, DROP COLUMN IF EXISTS catalog_item_edition_id, DROP COLUMN IF EXISTS editorial_reference_source;
