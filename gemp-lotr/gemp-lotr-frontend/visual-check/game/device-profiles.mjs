// The devices the game's checks run on: a computer with a mouse; phones and tablets as their browsers are (touch, a
// mobile browser that reads the viewport tag, a sharp screen), each held upright and sideways.
export const DEVICES = {
  pc: { viewport: { width: 1366, height: 768 } },
  tablet: { viewport: { width: 768, height: 1024 }, isMobile: true, hasTouch: true, deviceScaleFactor: 2 },
  'tablet-sideways': { viewport: { width: 1024, height: 768 }, isMobile: true, hasTouch: true, deviceScaleFactor: 2 },
  phone: { viewport: { width: 390, height: 844 }, isMobile: true, hasTouch: true, deviceScaleFactor: 3 },
  'phone-sideways': { viewport: { width: 844, height: 390 }, isMobile: true, hasTouch: true, deviceScaleFactor: 3 },
};
