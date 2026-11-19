import { draftState } from '../../../testing/authoring';
import {
  bareName,
  canonicalOf,
  copyIdOf,
  filingPreview,
  folderOf,
  MAX_PATH,
  nameLength,
  suggestedName,
  tidyPath,
} from './filing';

describe('filing a draft (Save as, Rename)', () => {
  it("says a file's place without its kind's folder or its ending", () => {
    expect(bareName('tutorials/basics/basics-02.hjson')).toBe('basics/basics-02');
    expect(bareName('Puzzles/x.HJSON')).toBe('x');
    expect(bareName('mine/x.hjson')).toBe('mine/x');
    expect(bareName('/tutorials/x')).toBe('/tutorials/x');
    expect(bareName('plain')).toBe('plain');
    expect(bareName(null)).toBe('');
    expect(bareName(undefined)).toBe('');
  });

  it('tidies what is typed into a path the server takes, with .hjson on the end', () => {
    expect(tidyPath('  basics\\basics-02-the-ring ')).toBe('basics/basics-02-the-ring.hjson');
    expect(tidyPath('my copy!.hjson')).toBe('my copy.hjson');
    expect(tidyPath('a//b/./-c-/')).toBe('a/b/c.hjson');
    expect(tidyPath('é tude/ça va?')).toBe('tude/a va.hjson');
    expect(tidyPath('x.HJSON')).toBe('x.hjson');
    expect(tidyPath(' !! ')).toBe('');
    expect(tidyPath('')).toBe('');
  });

  it("gives a copy the id the server will: from the file's name", () => {
    expect(copyIdOf('basics/My Copy 2.hjson')).toBe('my-copy-2');
    expect(copyIdOf('--x__y--.hjson')).toBe('x-y');
  });

  it("suggests where it is now, or its id; a copy's with -copy after it", () => {
    const state = draftState({ file: 'tutorials/basics/b-02.hjson' });
    expect(suggestedName(state, 'rename')).toBe('basics/b-02');
    expect(suggestedName(state, 'copy')).toBe('basics/b-02-copy');
    expect(suggestedName(draftState({ file: null }), 'copy')).toBe('test-copy');
  });

  it("keeps the name within the server's most, less the folder and the ending", () => {
    expect(folderOf(draftState({ kindFolder: 'puzzles' }))).toBe('puzzles/');
    expect(folderOf(draftState({ kindFolder: '' }))).toBe('tutorials/');
    expect(nameLength(draftState({ kindFolder: 'puzzles' }))).toBe(MAX_PATH - 'puzzles/'.length - '.hjson'.length);
  });

  it("keeps a public scenario public, and anything else in the author's own folder", () => {
    expect(canonicalOf(draftState({ canonical: true, canSaveAnywhere: true }))).toBe(true);
    expect(canonicalOf(draftState({ canonical: false, canSaveAnywhere: true }))).toBe(false);
    expect(canonicalOf(draftState({ canonical: false, canSaveAnywhere: false }))).toBe(true);
  });

  it('says where the file will be as it is typed, and for a copy the id it will have', () => {
    const own = draftState({ canonical: false });
    expect(filingPreview(own, 'copy', 'My Copy!')).toBe('your own folder: tutorials/My Copy.hjson  (id: my-copy)');
    expect(filingPreview(draftState(), 'rename', 'b/c')).toBe('tutorials/b/c.hjson');
    expect(filingPreview(own, 'rename', '  ')).toBe('');
  });
});
