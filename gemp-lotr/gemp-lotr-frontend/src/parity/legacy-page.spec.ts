import { FakeGempServer } from '../testing/parity/fake-gemp-server';
import { LegacyPage, openLegacyPage } from '../testing/parity/legacy-page';

describe('the legacy deck builder, run for the parity tests', () => {
  let server: FakeGempServer;
  let page: LegacyPage;

  beforeEach(async () => {
    server = new FakeGempServer();
    page = await openLegacyPage(server);
  });

  afterEach(() => page.close());

  it('starts without errors and asks the server for what the page shows', () => {
    expect(page.errors).toEqual([]);
    expect(server.requests()).toContain('POST /deck/formats includeEvents=false');
    expect(server.requests().some((request) => request.startsWith('GET /collection/default '))).toBe(true);
    expect(page.$('#collectionDiv .card').length).toBe(12);
  });
});
