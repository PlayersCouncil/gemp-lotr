import { Component } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { ShareLinks } from '../../../core/hall/share-links';
import { ClipboardWriter } from '../../../core/platform/clipboard.service';
import { PAGE_ORIGIN } from '../../../core/platform/page-location';
import { HallShareButtonComponent } from './hall-share-button.component';

@Component({
  imports: [HallShareButtonComponent],
  template: '<gd-hall-share-button kind="format" id="pc_movie" label="Share" ariaLabel="Copy a share link to Movie" />',
})
class HostComponent {}

describe('HallShareButtonComponent', () => {
  let works: boolean;

  beforeEach(() => {
    works = true;
    TestBed.configureTestingModule({
      providers: [
        ShareLinks,
        { provide: PAGE_ORIGIN, useValue: 'https://gemp.test' },
        { provide: ClipboardWriter, useValue: { copy: () => Promise.resolve(works) } },
      ],
    });
  });

  async function click() {
    const fixture = TestBed.createComponent(HostComponent);
    fixture.detectChanges();
    const el = fixture.nativeElement as HTMLElement;
    el.querySelector<HTMLButtonElement>('.gd-hall-share__button')!.click();
    await fixture.whenStable();
    fixture.detectChanges();
    return el;
  }

  it('is a labelled button with the share icon', async () => {
    const fixture = TestBed.createComponent(HostComponent);
    fixture.detectChanges();
    const button = (fixture.nativeElement as HTMLElement).querySelector<HTMLButtonElement>('.gd-hall-share__button')!;
    expect(button.textContent?.trim()).toBe('Share');
    expect(button.title).toBe('Copy a link to share (it shows a preview where you paste it)');
    expect(button.getAttribute('aria-label')).toBe('Copy a share link to Movie');
    expect(button.querySelector('img')?.getAttribute('src')).toBe(
      '/gemp-lotr/images/icons/share-from-square-solid.svg',
    );
  });

  it('says "Link copied" once copied', async () => {
    const el = await click();
    expect(el.querySelector('.gd-hall-share__note')?.textContent?.trim()).toBe('Link copied');
  });

  it('shows the link, selected, when the browser would not copy it', async () => {
    works = false;
    const el = await click();
    const field = el.querySelector<HTMLInputElement>('.gd-hall-share__note[data-manual] input')!;
    expect(field.value).toBe('https://gemp.test/gemp-lotr/share/format/pc_movie');
    expect(document.activeElement === field || !document.body.contains(field)).toBe(true);
  });
});
