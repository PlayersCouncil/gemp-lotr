import { boundVerdict } from './bound-check';

describe("the server's word on a bound typed", () => {
  it('counts what it finds, or says why it will not do, in the server’s words', () => {
    expect(boundVerdict({ ok: true, why: null, total: 7 })).toEqual({ kind: 'count', total: 7 });
    expect(boundVerdict({ ok: false, why: "'colour:red': no 'colour'", total: null })).toEqual({
      kind: 'refused',
      why: "'colour:red': no 'colour'",
    });
    expect(boundVerdict({ ok: false })).toEqual({ kind: 'refused', why: 'Refused' });
  });

  it('says nothing of an answer it does not understand', () => {
    for (const odd of [null, 'text', { ok: true }, { ok: true, total: 'x' }, { total: 3 }]) {
      expect(boundVerdict(odd)).toEqual({ kind: 'none' });
    }
  });
});
