--liquibase formatted sql

--changeset codex:013-add-manual-collection-items
ALTER TABLE collection_items
    ADD COLUMN manual_title VARCHAR(160),
    ADD COLUMN manual_description VARCHAR(4000),
    ADD COLUMN manual_type VARCHAR(80);

ALTER TABLE collection_items
    DROP CONSTRAINT chk_collection_items_reference,
    DROP CONSTRAINT chk_collection_items_editorial_reference_source;

ALTER TABLE collection_items
    ADD CONSTRAINT chk_collection_items_manual_title_not_blank
        CHECK (manual_title IS NULL OR btrim(manual_title) <> ''),
    ADD CONSTRAINT chk_collection_items_reference
        CHECK (
            (manual_title IS NOT NULL AND master_product_id IS NULL AND catalog_item_id IS NULL
             AND catalog_item_edition_id IS NULL AND editorial_reference_source = 'MANUAL')
            OR
            (manual_title IS NULL AND manual_description IS NULL AND manual_type IS NULL
             AND (master_product_id IS NOT NULL OR catalog_item_id IS NOT NULL)
             AND editorial_reference_source <> 'MANUAL')
        ),
    ADD CONSTRAINT chk_collection_items_editorial_reference_source
        CHECK (editorial_reference_source IN ('LEGACY', 'VERIFIED_BRIDGE', 'MANUAL_EDITORIAL', 'MANUAL'));

--rollback DO $$ BEGIN IF EXISTS (SELECT 1 FROM collection_items WHERE manual_title IS NOT NULL OR manual_description IS NOT NULL OR manual_type IS NOT NULL OR editorial_reference_source = 'MANUAL') THEN RAISE EXCEPTION 'Cannot rollback manual collection item migration while manual data exists'; END IF; END $$; ALTER TABLE collection_items DROP CONSTRAINT chk_collection_items_manual_title_not_blank, DROP CONSTRAINT chk_collection_items_reference, DROP CONSTRAINT chk_collection_items_editorial_reference_source; ALTER TABLE collection_items ADD CONSTRAINT chk_collection_items_reference CHECK (master_product_id IS NOT NULL OR catalog_item_id IS NOT NULL), ADD CONSTRAINT chk_collection_items_editorial_reference_source CHECK (editorial_reference_source IN ('LEGACY', 'VERIFIED_BRIDGE', 'MANUAL_EDITORIAL')); ALTER TABLE collection_items DROP COLUMN manual_title, DROP COLUMN manual_description, DROP COLUMN manual_type;
