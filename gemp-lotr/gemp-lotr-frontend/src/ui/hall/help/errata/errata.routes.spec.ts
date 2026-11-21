import { UrlSegment } from '@angular/router';
import { errataMatcher } from './errata.routes';

const segments = (...paths: string[]) => paths.map((path) => new UrlSegment(path, {}));

describe('errata routes', () => {
  it('takes errata and errata/<cardId> as one route', () => {
    expect(errataMatcher(segments('errata'))?.posParams).toBeUndefined();
    expect(errataMatcher(segments('errata'))?.consumed).toHaveLength(1);
    expect(errataMatcher(segments('errata', '51_45'))?.posParams?.['cardId']?.path).toBe('51_45');
    expect(errataMatcher(segments('errata', '1', '2'))).toBeNull();
    expect(errataMatcher(segments('formats'))).toBeNull();
    expect(errataMatcher([])).toBeNull();
  });
});
