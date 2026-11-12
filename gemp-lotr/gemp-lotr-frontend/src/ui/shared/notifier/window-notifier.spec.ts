import { TestBed } from '@angular/core/testing';
import { WindowNotifier } from './window-notifier';

describe('WindowNotifier', () => {
  let notifier: WindowNotifier;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [WindowNotifier] });
    notifier = TestBed.inject(WindowNotifier);
  });

  afterEach(() => vi.restoreAllMocks());

  it("uses the browser's dialogs", async () => {
    const alert = vi.spyOn(window, 'alert').mockImplementation(() => undefined);
    const confirm = vi.spyOn(window, 'confirm').mockReturnValue(true);
    const prompt = vi.spyOn(window, 'prompt').mockReturnValue('typed');
    await notifier.alert('Hello');
    expect(alert).toHaveBeenCalledWith('Hello');
    expect(await notifier.confirm('Sure?')).toBe(true);
    expect(confirm).toHaveBeenCalledWith('Sure?');
    expect(await notifier.askText('Name?', 'Old')).toBe('typed');
    expect(prompt).toHaveBeenCalledWith('Name?', 'Old');
  });

  it('answers null when the prompt is cancelled', async () => {
    vi.spyOn(window, 'prompt').mockReturnValue(null);
    expect(await notifier.askText('Name?')).toBeNull();
  });
});
