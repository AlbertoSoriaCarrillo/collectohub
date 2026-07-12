import { TestBed } from '@angular/core/testing';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { of, throwError } from 'rxjs';
import { EditorialAdminService } from '../../../../core/services/editorial-admin.service';
import { AdminDataQualityComponent } from './admin-data-quality.component';

describe('AdminDataQualityComponent', () => {
  const report = { generatedAt: '2026-01-01T00:00:00Z', scope: 'ALL' as const, totalChecks: 1, totalFindings: 1, checks: [{ key: 'CREATOR_NAME', entityType: 'CREATOR' as const, severity: 'HIGH' as const, title: 'Duplicate creators', description: '', totalFindings: 1, findings: [{ groupKey: 'akira', displayValue: 'Akira', recordIds: [1, 2], recordLabels: ['Akira', 'Akira'], recommendation: 'Review' }] }] };
  function configure(response = of(report)) { const mock = { getEditorialDataQualityReport: vi.fn(() => response) }; TestBed.configureTestingModule({ imports: [AdminDataQualityComponent], providers: [provideAnimationsAsync('noop'), { provide: EditorialAdminService, useValue: mock }] }); return mock; }
  afterEach(() => TestBed.resetTestingModule());
  it('loads and renders findings', () => { const service = configure(); const fixture = TestBed.createComponent(AdminDataQualityComponent); fixture.detectChanges(); expect(service.getEditorialDataQualityReport).toHaveBeenCalledWith('ALL'); expect(fixture.nativeElement.textContent).toContain('Akira'); expect(fixture.nativeElement.textContent).toContain('Review'); });
  it('shows error state', () => { configure(throwError(() => new Error('failed'))); const fixture = TestBed.createComponent(AdminDataQualityComponent); fixture.detectChanges(); expect(fixture.nativeElement.querySelector('[role="alert"]')).toBeTruthy(); });
});
