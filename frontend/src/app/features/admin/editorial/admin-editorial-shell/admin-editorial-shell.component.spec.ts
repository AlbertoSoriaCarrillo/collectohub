import { TestBed } from '@angular/core/testing';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { AdminEditorialShellComponent } from './admin-editorial-shell.component';

describe('AdminEditorialShellComponent', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AdminEditorialShellComponent],
      providers: [provideAnimationsAsync('noop')]
    }).compileComponents();
  });

  it('shows the editorial admin title and upcoming sections', () => {
    const fixture = TestBed.createComponent(AdminEditorialShellComponent);
    fixture.detectChanges();

    const compiled = fixture.nativeElement as HTMLElement;

    expect(compiled.textContent).toContain('Editorial admin');
    expect(compiled.textContent).toContain('Publishers');
    expect(compiled.textContent).toContain('Franchises');
    expect(compiled.textContent).toContain('Series');
    expect(compiled.textContent).toContain('Items');
    expect(compiled.textContent).toContain('Editions');
    expect(compiled.textContent).toContain('Creators');
    expect(compiled.textContent).toContain('Credits');
    expect(compiled.textContent).toContain('Relationships');
    expect(compiled.textContent).toContain('Legacy reconciliation');
  });

  it('states that CRUD forms will come in later EPICs', () => {
    const fixture = TestBed.createComponent(AdminEditorialShellComponent);
    fixture.detectChanges();

    const compiled = fixture.nativeElement as HTMLElement;

    expect(compiled.textContent).toContain(
      'CRUD forms will be implemented in the next EPICs.'
    );
  });
});
