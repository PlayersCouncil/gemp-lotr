export interface Rect {
  left: number;
  top: number;
  width: number;
  height: number;
}

/** Inline style for an absolutely positioned box, as the legacy code set it with jQuery .css(). */
export function rectStyle(rect: Rect): Record<string, string> {
  return {
    position: 'absolute',
    left: `${rect.left}px`,
    top: `${rect.top}px`,
    width: `${rect.width}px`,
    height: `${rect.height}px`,
  };
}
