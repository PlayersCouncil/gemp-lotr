import { TestBed } from '@angular/core/testing';
import { PAGE_QUERY_STRING } from '../platform/page-location';
import { SessionService } from './session.service';

describe('SessionService', () => {
  function participantFor(queryString: string): string | null {
    TestBed.configureTestingModule({ providers: [{ provide: PAGE_QUERY_STRING, useValue: queryString }] });
    return TestBed.inject(SessionService).participantId;
  }

  it('has no participant normally', () => {
    expect(participantFor('')).toBeNull();
  });

  it('reads participantId among other parameters', () => {
    expect(participantFor('?foo=1&participantId=bob&bar=2')).toBe('bob');
  });

  it('decodes the value once', () => {
    expect(participantFor('?participantId=Sm%C3%A9agol%20%26%20Co')).toBe('Sméagol & Co');
  });

  it('treats an empty value as no participant', () => {
    expect(participantFor('?participantId=')).toBeNull();
  });
});
