import { TestBed } from '@angular/core/testing';
import { PathPopups } from '../table-edit/path-popups';
import { AdventurePathComponent } from './adventure-path.component';
import { GameCardViews } from '../../../core/game/table/game-card-views';

describe('AdventurePathComponent', () => {
  it('ends with the open spot for the next site while one can be laid, counted with the sites', () => {
    TestBed.configureTestingModule({ providers: [PathPopups, { provide: GameCardViews, useValue: {} }] });
    const fixture = TestBed.createComponent(AdventurePathComponent);
    fixture.componentRef.setInput('sites', []);
    fixture.detectChanges();
    const host = fixture.nativeElement as HTMLElement;
    expect(host.style.getPropertyValue('--gd-path-count')).toBe('0');
    expect(host.querySelector('gd-site-spot')).toBeNull();
    fixture.componentRef.setInput('spot', 1);
    fixture.detectChanges();
    expect(host.style.getPropertyValue('--gd-path-count')).toBe('1');
    const spot = host.querySelector<HTMLElement>('gd-site-spot.gd-game-path__spot')!;
    expect(spot.style.getPropertyValue('--gd-path-index')).toBe('0');
    expect(spot.querySelector('button')?.dataset['site']).toBe('1');
  });
});
