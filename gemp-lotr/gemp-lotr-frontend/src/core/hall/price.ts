/** A price in the hall's money: 100 silver to the gold (common.js formatPrice). Events and My Account show them. */
export interface Price {
  readonly gold: number;
  /** Always two digits ("05"), as the legacy page wrote it. */
  readonly silver: string;
}

/** formatPrice: 100 silver to the gold, the silver in two digits. */
export function price(amount: number): Price {
  const silver = amount % 100;
  return { gold: Math.floor(amount / 100), silver: silver < 10 ? `0${silver}` : String(silver) };
}
