import { signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { ActivatedRoute, convertToParamMap, provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { AuthService } from '../../../core/auth/auth.service';
import { CollectionItemResponse, CollectionResponse } from '../../../core/models/collection.model';
import { CollectionService } from '../../../core/services/collection.service';
import { CollectionDetailComponent } from './collection-detail.component';

describe('CollectionDetailComponent', () => {
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

  const collection: CollectionResponse = {
    id: 3,
    userId: 2,
    name: 'Manga pendientes',
    description: 'Lista personal',
    visibility: 'PRIVATE',
    categoryCode: 'MANGA_COMIC',
    categoryName: 'Manga / Comic',
    items: [item]
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CollectionDetailComponent],
      providers: [
        provideAnimationsAsync('noop'),
        provideRouter([]),
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              paramMap: convertToParamMap({ collectionId: '3' })
            }
          }
        },
        {
          provide: CollectionService,
          useValue: {
            getCollection: vi.fn(() => of(collection)),
            getCollectionItems: vi.fn(() => of([item])),
            deleteCollectionItem: vi.fn(() => of(null))
          }
        },
        {
          provide: AuthService,
          useValue: {
            currentUser: signal({ id: 2, email: 'user@example.com', displayName: 'Ada', preferredInterfaceLanguage: 'es', roles: ['USER'] }),
            hasToken: vi.fn(() => true),
            getMe: vi.fn()
          }
        }
      ]
    }).compileComponents();
  });

  it('renders collection items', async () => {
    const fixture = TestBed.createComponent(CollectionDetailComponent);
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();

    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.textContent).toContain('Manga pendientes');
    expect(compiled.textContent).toContain('One Piece 1');
    expect(compiled.textContent).toContain('MISSING');
  });
});
