import { TestBed } from '@angular/core/testing';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { ActivatedRoute, convertToParamMap, provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { ProductCategoryResponse } from '../../../core/models/catalog.model';
import { CatalogService } from '../../../core/services/catalog.service';
import { InventoryService } from '../../../core/services/inventory.service';
import { ShopProductCreateComponent } from './shop-product-create.component';

describe('ShopProductCreateComponent', () => {
  const category: ProductCategoryResponse = {
    id: 1,
    code: 'MANGA_COMIC',
    name: 'Manga / Comic',
    parentId: null
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ShopProductCreateComponent],
      providers: [
        provideAnimationsAsync('noop'),
        provideRouter([]),
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              paramMap: convertToParamMap({ shopId: '9' })
            }
          }
        },
        {
          provide: CatalogService,
          useValue: {
            searchMasterProducts: vi.fn(() =>
              of([
                {
                  id: 5,
                  name: 'One Piece 1',
                  description: null,
                  category,
                  franchise: 'One Piece',
                  collectionName: 'One Piece',
                  volumeNumber: '1',
                  publisher: null,
                  isbn: null,
                  ean: null,
                  releaseDate: null,
                  editionStartDate: null,
                  editionEndDate: null,
                  language: 'es',
                  limitedEdition: false,
                  limitedEditionTotalUnits: null,
                  publicationCountries: [],
                  coverImageUrl: null,
                  status: 'ACTIVE',
                  attributes: {}
                }
              ])
            )
          }
        },
        {
          provide: InventoryService,
          useValue: {
            createShopProduct: vi.fn()
          }
        }
      ]
    }).compileComponents();
  });

  it('validates required product, price, stock and condition fields', () => {
    const fixture = TestBed.createComponent(ShopProductCreateComponent);
    const component = fixture.componentInstance;

    component.form.patchValue({
      masterProductId: null,
      priceAmount: null,
      stockQuantity: null,
      physicalCondition: ''
    });
    component.submit();

    expect(component.form.controls.masterProductId.hasError('required')).toBe(true);
    expect(component.form.controls.priceAmount.hasError('required')).toBe(true);
    expect(component.form.controls.stockQuantity.hasError('required')).toBe(true);
    expect(component.form.controls.physicalCondition.hasError('required')).toBe(true);
  });
});
