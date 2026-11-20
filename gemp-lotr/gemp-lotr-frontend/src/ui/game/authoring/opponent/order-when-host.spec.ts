import { signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { draftState } from '../../../../testing/authoring';
import { OrderWhenHost } from './order-when-host';
import { OrdersEditor } from './orders-editor';

describe('OrderWhenHost', () => {
  it('works on the open order’s When only, puts a picker’s change into its form, and sends nothing itself', () => {
    const form = signal<{ when: string } | null>(null);
    const type = vi.fn();
    TestBed.configureTestingModule({
      providers: [
        OrderWhenHost,
        {
          provide: OrdersEditor,
          useValue: { form, type, authoring: { draft: () => draftState({ twoSided: true }) } },
        },
      ],
    });
    const host = TestBed.inject(OrderWhenHost);
    expect(host.whenText('when')).toBeNull();
    form.set({ when: 'opponent, action' });
    expect(host.whenText('when')).toBe('opponent, action');
    expect(host.whenText('until')).toBeNull();
    host.putWhen('until', 'x');
    expect(type).not.toHaveBeenCalled();
    host.putWhen('when', 'opponent, assign');
    expect(type).toHaveBeenCalledWith({ when: 'opponent, assign' });
    host.pickedWhen();
    expect(type).toHaveBeenCalledTimes(1);
    expect(host.whenFacts()).toEqual({ twoSided: true, opening: false });
  });
});
