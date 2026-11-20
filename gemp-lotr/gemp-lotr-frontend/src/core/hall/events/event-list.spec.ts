import { of } from 'rxjs';
import { EventDrawer, eventRow } from './event-drawer';
import { EventList } from './event-list';

describe('EventList', () => {
  const drawer = () =>
    new EventDrawer<string>({
      load: () => of('x'),
      failure: () => 'failed',
      unreadableText: '',
      emptyText: '',
    });

  it('drops a key listed twice and treats a null answer as no items', async () => {
    let next: { id: string }[] | null = [{ id: 'a' }, { id: 'a' }];
    const list = new EventList<{ id: string }, string>({
      fetch: () => of(next),
      failed: (message) => `failed: ${message}`,
      key: (item) => item.id,
      title: (item) => item.id,
      date: () => '',
      create: (item) => eventRow(item.id, item.id, '', drawer()),
    });
    await list.load();
    expect(list.rows().map((row) => row.key)).toEqual(['a']);
    next = null;
    await list.load();
    expect(list.rows()).toEqual([]);
    expect(list.state()).toEqual({ kind: 'loaded' });
  });
});
