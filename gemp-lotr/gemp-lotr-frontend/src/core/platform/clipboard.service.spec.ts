import { DOCUMENT } from '@angular/common';
import { TestBed } from '@angular/core/testing';
import { ClipboardWriter } from './clipboard.service';

describe('ClipboardWriter', () => {
  function writer(clipboard: { writeText: (text: string) => Promise<void> } | undefined, secure = true) {
    TestBed.overrideProvider(DOCUMENT, {
      useValue: { defaultView: { navigator: { clipboard }, isSecureContext: secure } },
    });
    return TestBed.inject(ClipboardWriter);
  }

  it('copies with the Clipboard API', async () => {
    const writeText = vi.fn(() => Promise.resolve());
    expect(await writer({ writeText }).copy('https://x/share/format/pc_movie')).toBe(true);
    expect(writeText).toHaveBeenCalledWith('https://x/share/format/pc_movie');
  });

  it('says so when the API refuses, is missing or may not be used', async () => {
    expect(await writer({ writeText: () => Promise.reject(new Error('denied')) }).copy('a')).toBe(false);
    TestBed.resetTestingModule();
    expect(await writer(undefined).copy('a')).toBe(false);
    TestBed.resetTestingModule();
    const writeText = vi.fn(() => Promise.resolve());
    expect(await writer({ writeText }, false).copy('a')).toBe(false);
    expect(writeText).not.toHaveBeenCalled();
  });
});
