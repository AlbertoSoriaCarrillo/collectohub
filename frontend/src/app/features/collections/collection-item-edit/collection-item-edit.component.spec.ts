import { TestBed } from '@angular/core/testing';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { ActivatedRoute, convertToParamMap, provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { CollectionItemResponse } from '../../../core/models/collection.model';
import { CollectionService } from '../../../core/services/collection.service';
import { CollectionItemEditComponent } from './collection-item-edit.component';

describe('CollectionItemEditComponent', () => {
  const item: CollectionItemResponse = {
    id: 7,
    collectionId: 3,
    masterProductId: 5,
    masterProductName: 'One Piece 1',
    masterProductCategoryCode: 'MANGA_COMIC',
    masterProductFranchise: 'One Piece',
    masterProductCollectionName: 'One Piece',
    masterProductVolumeNumber: '1',
    collectionStatus: 'MISSING',
    physicalCondition: 'NEW',
    unitNumber: null,
    totalLimitedUnits: null,
    notes: 'Missing item',
    acquiredAt: null
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CollectionItemEditComponent],
      providers: [
        provideAnimationsAsync('noop'),
        provideRouter([]),
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              paramMap: convertToParamMap({ collectionId: '3', itemId: '7' })
            }
          }
        },
        {
          provide: CollectionService,
          useValue: {
            getCollectionItems: vi.fn(() => of([item])),
            updateCollectionItem: vi.fn()
          }
        }
      ]
    }).compileComponents();
  });

  it('loads mock data and validates status', async () => {
    const fixture = TestBed.createComponent(CollectionItemEditComponent);
    const component = fixture.componentInstance;
    fixture.detectChanges();
    await fixture.whenStable();

    expect(component.item()?.masterProductName).toBe('One Piece 1');
    expect(component.form.controls.collectionStatus.value).toBe('MISSING');

    component.form.patchValue({ collectionStatus: '' });
    component.submit();

    expect(component.form.controls.collectionStatus.hasError('required')).toBe(true);
  });
});
