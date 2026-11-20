import { TestBed } from '@angular/core/testing';
import { ScenarioRoom } from './scenario-room';

describe('ScenarioRoom', () => {
  it('starts with nothing of the window taken at its right', () => {
    TestBed.configureTestingModule({ providers: [ScenarioRoom] });
    expect(TestBed.inject(ScenarioRoom).reservedRight()).toBe(0);
  });
});
