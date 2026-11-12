import { AngularPage, openAngularPage } from '../testing/parity/angular-page';
import { FakeGempServer } from '../testing/parity/fake-gemp-server';

describe('the Angular deck builder, run for the parity tests', () => {
  let server: FakeGempServer;
  let page: AngularPage;

  beforeEach(async () => {
    vi.useFakeTimers();
    server = new FakeGempServer();
    page = await openAngularPage(server);
  });

  afterEach(() => {
    page.close();
    vi.useRealTimers();
  });

  it('starts and asks the server for what the page shows', () => {
    expect(server.requests()).toContain('POST /deck/formats includeEvents=false');
    expect(server.requests().some((request) => request.startsWith('GET /collection/default '))).toBe(true);
    expect(page.element.querySelectorAll('.gd-collection .gd-card').length).toBe(12);
  });
});
