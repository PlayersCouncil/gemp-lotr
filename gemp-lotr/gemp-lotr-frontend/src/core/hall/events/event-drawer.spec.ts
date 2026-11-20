import { Subject } from 'rxjs';
import { ApiError, INVALID_RESPONSE_STATUS } from '../../api/api-error';
import { EventDrawer, eventRow, updateRows } from './event-drawer';

function harness(options: { fallback?: boolean; fetch?: boolean } = {}) {
  const requests: Subject<string | null>[] = [];
  const drawer = new EventDrawer<string>({
    load: () => {
      const request = new Subject<string | null>();
      requests.push(request);
      return request;
    },
    failure: (error) => `failed ${error.status}`,
    unreadableText: 'unreadable',
    emptyText: 'empty',
    fallback: options.fallback ?? false,
    fetch: options.fetch ?? true,
  });
  const answer = (value: string | null, index = requests.length - 1) => {
    requests[index]!.next(value);
    requests[index]!.complete();
  };
  const fail = (status: number, index = requests.length - 1) => requests[index]!.error(new ApiError(status, 'x'));
  return { drawer, requests, answer, fail };
}

describe('EventDrawer', () => {
  it('fetches on the first opening only, then just opens and shuts', () => {
    const { drawer, requests, answer } = harness();
    expect(drawer.content()).toBeNull();
    expect(drawer.toggleLabel()).toBe('+ Details');
    drawer.open();
    expect(drawer.toggleLabel()).toBe('− Details');
    expect(drawer.content()).toEqual({ kind: 'loading', text: 'Loading details…' });
    answer('detail');
    expect(drawer.content()).toEqual({ kind: 'data', data: 'detail' });
    drawer.toggle();
    drawer.toggle();
    expect(requests).toHaveLength(1);
    expect(drawer.status).toBe('loaded');
  });

  it('shows a failure and fetches again at the next opening', () => {
    const { drawer, requests, answer, fail } = harness();
    drawer.open();
    fail(404);
    expect(drawer.content()).toEqual({ kind: 'error', text: 'failed 404' });
    drawer.close();
    drawer.open();
    expect(requests).toHaveLength(2);
    expect(drawer.content()?.kind).toBe('loading');
    answer(null);
    expect(drawer.content()).toEqual({ kind: 'empty', text: 'empty' });
  });

  it('says an unreadable answer could not be displayed, even with a fallback', () => {
    const { drawer, fail } = harness({ fallback: true });
    drawer.open();
    fail(INVALID_RESPONSE_STATUS);
    expect(drawer.content()).toEqual({ kind: 'error', text: 'unreadable' });
  });

  it('shows the fallback for a failed fetch, or at once when told not to fetch', () => {
    const fetched = harness({ fallback: true });
    fetched.drawer.open();
    fetched.fail(500);
    expect(fetched.drawer.content()).toEqual({ kind: 'fallback' });
    const unasked = harness({ fallback: true, fetch: false });
    unasked.drawer.open();
    expect(unasked.requests).toHaveLength(0);
    expect(unasked.drawer.content()).toEqual({ kind: 'fallback' });
  });

  it('keeps the old content through a quiet refresh that fails, and fetches again when reopened', () => {
    const { drawer, requests, answer, fail } = harness();
    drawer.open();
    answer('old');
    drawer.refresh(true);
    expect(drawer.content()).toEqual({ kind: 'data', data: 'old' });
    fail(0);
    expect(drawer.content()).toEqual({ kind: 'data', data: 'old' });
    expect(drawer.status).toBe('stale');
    drawer.close();
    drawer.open();
    expect(requests).toHaveLength(3);
    answer('new');
    expect(drawer.content()).toEqual({ kind: 'data', data: 'new' });
  });

  it('fetches a shut drawer marked stale when it opens again', () => {
    const { drawer, requests, answer } = harness();
    drawer.open();
    answer('old');
    drawer.close();
    drawer.invalidate();
    drawer.open();
    expect(requests).toHaveLength(2);
    expect(drawer.content()).toEqual({ kind: 'data', data: 'old' });
  });

  it('ignores an answer for content since replaced, and answers after destroy', () => {
    const { drawer, answer } = harness();
    drawer.open();
    drawer.refresh();
    answer('first', 0);
    expect(drawer.content()?.kind).toBe('loading');
    answer('second', 1);
    expect(drawer.content()).toEqual({ kind: 'data', data: 'second' });
    drawer.destroy();
    expect(drawer.alive).toBe(false);
    drawer.refresh();
    expect(drawer.status).toBe('loaded');
  });
});

describe('updateRows', () => {
  const spec = {
    key: (item: { id: string; name: string }) => item.id,
    title: (item: { id: string; name: string }) => item.name,
    date: () => '2026-11-20',
    create: (item: { id: string; name: string }) => eventRow(item.id, item.name, '2026-11-20', harness().drawer),
  };

  it('keeps rows still listed, in the new order, refreshing open drawers and marking shut ones stale', () => {
    const rows = updateRows(
      [],
      [
        { id: 'a', name: 'A' },
        { id: 'b', name: 'B' },
        { id: 'c', name: 'C' },
      ],
      spec,
    );
    const [a, b, c] = rows;
    const refresh = vi.spyOn(a!.drawer, 'refresh');
    const invalidate = vi.spyOn(b!.drawer, 'invalidate');
    const destroy = vi.spyOn(c!.drawer, 'destroy');
    a!.drawer.open();
    const next = updateRows(
      rows,
      [
        { id: 'b', name: 'B2' },
        { id: 'd', name: 'D' },
        { id: 'a', name: 'A' },
        { id: 'b', name: 'again' },
      ],
      spec,
    );
    expect(next.map((row) => row.key)).toEqual(['b', 'd', 'a']);
    expect(next[0]).toBe(b);
    expect(b!.title()).toBe('B2');
    expect(refresh).toHaveBeenCalledWith(true);
    expect(invalidate).toHaveBeenCalled();
    expect(destroy).toHaveBeenCalled();
  });
});
