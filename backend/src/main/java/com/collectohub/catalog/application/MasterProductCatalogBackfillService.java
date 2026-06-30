package com.collectohub.catalog.application;

import com.collectohub.auth.security.AuthenticatedUser;
import com.collectohub.catalog.domain.CatalogItem;
import com.collectohub.catalog.domain.CatalogItemEdition;
import com.collectohub.catalog.domain.MasterProduct;
import com.collectohub.catalog.domain.MasterProductCatalogLinkSource;
import com.collectohub.catalog.dto.BackfillMasterProductCatalogLinksResponse;
import com.collectohub.catalog.infrastructure.CatalogItemEditionRepository;
import com.collectohub.catalog.infrastructure.CatalogItemRepository;
import com.collectohub.catalog.infrastructure.MasterProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

@Service
public class MasterProductCatalogBackfillService {

    private static final BigDecimal IDENTIFIER_CONFIDENCE = new BigDecimal("0.9500");
    private static final BigDecimal TITLE_CONFIDENCE = new BigDecimal("0.7000");
    private static final BigDecimal TITLE_VOLUME_CONFIDENCE = new BigDecimal("0.8000");

    private final MasterProductRepository masterProductRepository;
    private final CatalogItemRepository itemRepository;
    private final CatalogItemEditionRepository editionRepository;
    private final MasterProductCatalogLinkService linkService;

    public MasterProductCatalogBackfillService(
            MasterProductRepository masterProductRepository,
            CatalogItemRepository itemRepository,
            CatalogItemEditionRepository editionRepository,
            MasterProductCatalogLinkService linkService
    ) {
        this.masterProductRepository = masterProductRepository;
        this.itemRepository = itemRepository;
        this.editionRepository = editionRepository;
        this.linkService = linkService;
    }

    @Transactional
    public BackfillMasterProductCatalogLinksResponse run(AuthenticatedUser user) {
        EditorialCatalogSupport.ensureAdmin(user);
        List<MasterProduct> products = masterProductRepository.findAllByDeletedAtIsNull();
        int proposed = 0;
        int skipped = 0;
        int ambiguous = 0;

        for (MasterProduct product : products) {
            Candidate candidate = findCandidate(product);
            if (candidate == null) {
                skipped++;
            } else if (candidate.ambiguous()) {
                ambiguous++;
            } else if (linkService.createProposal(
                    product, candidate.item(), candidate.edition(), candidate.source(),
                    candidate.confidence(), candidate.reason(), user.id())) {
                proposed++;
            } else {
                skipped++;
            }
        }
        return new BackfillMasterProductCatalogLinksResponse(products.size(), proposed, skipped, ambiguous);
    }

    private Candidate findCandidate(MasterProduct product) {
        String isbn = normalizeIdentifier(product.getIsbn());
        if (isbn != null) {
            List<CatalogItemEdition> matches = editionRepository.findAllByIsbnAndDeletedAtIsNull(isbn);
            if (matches.size() == 1) {
                return editionCandidate(matches.getFirst(), MasterProductCatalogLinkSource.ISBN, "Unique normalized ISBN match");
            }
            if (matches.size() > 1) {
                return Candidate.ambiguousCandidate();
            }
        }

        String ean = normalizeIdentifier(product.getEan());
        if (ean != null) {
            List<CatalogItemEdition> matches = editionRepository.findAllByEanAndDeletedAtIsNull(ean);
            if (matches.size() == 1) {
                return editionCandidate(matches.getFirst(), MasterProductCatalogLinkSource.EAN, "Unique normalized EAN match");
            }
            if (matches.size() > 1) {
                return Candidate.ambiguousCandidate();
            }
        }

        List<CatalogItem> titleMatches = itemRepository.findAllByTitleIgnoreCaseAndDeletedAtIsNull(product.getName());
        String volume = normalizeText(product.getVolumeNumber());
        if (volume != null) {
            titleMatches = titleMatches.stream()
                    .filter(item -> volume.equals(normalizeText(item.getSequenceLabel())))
                    .toList();
        }
        if (titleMatches.size() == 1) {
            MasterProductCatalogLinkSource source = volume == null
                    ? MasterProductCatalogLinkSource.TITLE
                    : MasterProductCatalogLinkSource.TITLE_AND_VOLUME;
            return new Candidate(
                    titleMatches.getFirst(), null, source,
                    volume == null ? TITLE_CONFIDENCE : TITLE_VOLUME_CONFIDENCE,
                    volume == null ? "Unique normalized title match" : "Unique title and volume match",
                    false
            );
        }
        return titleMatches.size() > 1 ? Candidate.ambiguousCandidate() : null;
    }

    private Candidate editionCandidate(
            CatalogItemEdition edition,
            MasterProductCatalogLinkSource source,
            String reason
    ) {
        return new Candidate(edition.getCatalogItem(), edition, source, IDENTIFIER_CONFIDENCE, reason, false);
    }

    private String normalizeIdentifier(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.replaceAll("[\\s-]", "").toUpperCase(Locale.ROOT);
    }

    private String normalizeText(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim().toLowerCase(Locale.ROOT);
    }

    private record Candidate(
            CatalogItem item,
            CatalogItemEdition edition,
            MasterProductCatalogLinkSource source,
            BigDecimal confidence,
            String reason,
            boolean ambiguous
    ) {
        static Candidate ambiguousCandidate() {
            return new Candidate(null, null, null, null, null, true);
        }
    }
}
