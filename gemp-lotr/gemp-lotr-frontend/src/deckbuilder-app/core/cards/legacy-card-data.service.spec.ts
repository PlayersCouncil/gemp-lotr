import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { readLegacyWebFile } from '../../../testing/files';
import { flushLegacyCardFiles } from '../../../testing/legacy-cards';
import { evaluateLegacyTable, LEGACY_CARD_DATA_FILES, LegacyCardDataService } from './legacy-card-data.service';

describe('LegacyCardDataService', () => {
  describe('evaluateLegacyTable', () => {
    it.each(LEGACY_CARD_DATA_FILES)('reads $table from $file', ({ file, table }) => {
      const images = evaluateLegacyTable(readLegacyWebFile(file), table);
      expect(Object.keys(images).length).toBeGreaterThan(0);
      for (const url of Object.values(images)) {
        expect(typeof url).toBe('string');
      }
    });

    it('works for const tables and leaves no globals behind', () => {
      const images = evaluateLegacyTable("const myTable = { '1_1': 'a.jpg' };", 'myTable');
      expect(images).toEqual({ '1_1': 'a.jpg' });
      expect((globalThis as Record<string, unknown>)['myTable']).toBeUndefined();
    });

    it('resolves values built from other variables in the same file (set40.js style)', () => {
      const source = "var base = 'http://x/'; var t = { '40_1': base + 'one.jpg' };";
      expect(evaluateLegacyTable(source, 't')).toEqual({ '40_1': 'http://x/one.jpg' });
    });

    it('rejects a file without the table', () => {
      expect(() => evaluateLegacyTable('var other = {};', 'missing')).toThrow();
    });

    it('rejects table names that are not identifiers', () => {
      expect(() => evaluateLegacyTable('', 'a; alert(1)')).toThrow();
    });
  });

  describe('load', () => {
    let service: LegacyCardDataService;
    let backend: HttpTestingController;

    beforeEach(() => {
      TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
      service = TestBed.inject(LegacyCardDataService);
      backend = TestBed.inject(HttpTestingController);
    });

    afterEach(() => backend.verify());

    it('loads every table from the legacy web root', async () => {
      const loading = service.load();
      flushLegacyCardFiles(backend);
      await loading;
      expect(service.tables.fixedImages['-1_1']).toBe('https://i.lotrtcgpc.net/decipher/LOTR00000.jpg');
      expect(service.tables.hobbit['30_1']).toBe('https://i.lotrtcgpc.net/hobbit/HDG30001.jpg');
      expect(Object.keys(service.tables.PCCards).length).toBeGreaterThan(100);
      expect(Object.keys(service.tables.packBlueprints).length).toBeGreaterThan(0);
      expect(Object.keys(service.tables.set40).length).toBeGreaterThan(0);
    });

    it('keeps going with an empty table when a file fails', async () => {
      const errors = vi.spyOn(console, 'error').mockImplementation(() => undefined);
      const loading = service.load();
      for (const { file } of LEGACY_CARD_DATA_FILES) {
        const req = backend.expectOne('/gemp-lotr/' + file);
        if (file.endsWith('hobbit.js')) {
          req.flush('', { status: 404, statusText: 'Not Found' });
        } else {
          req.flush(readLegacyWebFile(file));
        }
      }
      await loading;
      expect(service.tables.hobbit).toEqual({});
      expect(Object.keys(service.tables.PCCards).length).toBeGreaterThan(0);
      expect(errors).toHaveBeenCalledOnce();
      errors.mockRestore();
    });
  });
});
