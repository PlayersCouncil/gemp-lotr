/** A control on each page: the legacy page's selector and the Angular page's. */
export interface ControlSelectors {
  legacy: string;
  angular: string;
}

export const CONTROLS: Record<string, ControlSelectors> & {
  culture(code: string): ControlSelectors;
  cultureInput(code: string): ControlSelectors;
  deckCard(image: string): ControlSelectors;
};
