// The gestures every check makes with the mouse and the keyboard (the deck builder's matrix, the game's matrix): one
// function per gesture, given the page and the element. Shared by deckbuilder/ and game/.

/** A pause for the page to settle, then the check's checkpoint, if any (a step the report shows). */
export const pause = async (page, ms = 800) => {
  await page.waitForTimeout(ms);
  await page.checkpoint?.();
};

async function centre(locator) {
  const box = await locator.boundingBox({ timeout: 5000 });
  return { x: box.x + box.width / 2, y: box.y + box.height / 2 };
}

async function moveTo(page, target) {
  const { x, y } = await centre(target);
  await page.mouse.move(x, y);
  return { x, y };
}

/** With the mouse, at the element's centre: a real press lands on whatever is there, even on a disabled control. */
const click = async (page, target, options) => {
  await moveTo(page, target);
  await page.mouse.down(options);
  await page.mouse.up(options);
};

const withShift = async (page, action) => {
  await page.keyboard.down('Shift');
  await action();
  await page.keyboard.up('Shift');
};

export const GESTURES = {
  hover: async (page, target) => {
    await moveTo(page, target);
    await pause(page, 700);
  },
  leave: async (page, target) => {
    await moveTo(page, target);
    await pause(page, 300);
    await page.mouse.move(1, 1);
    await pause(page, 300);
  },
  /** Held down (the pressed look), then released (which is also the click). */
  press: async (page, target) => {
    await moveTo(page, target);
    await page.mouse.down();
    await pause(page, 300);
    await page.mouse.up();
    await pause(page, 800);
  },
  rightClick: async (page, target) => {
    await click(page, target, { button: 'right' });
    await pause(page, 500);
  },
  shiftClick: async (page, target) => {
    await withShift(page, () => click(page, target));
    await pause(page, 500);
  },
  // Both presses at one point (the first may close or replace what was under the mouse), as quickly as a person does
  doubleClick: async (page, target) => {
    const { x, y } = await moveTo(page, target);
    await page.mouse.click(x, y);
    await page.waitForTimeout(120);
    await page.mouse.down({ clickCount: 2 });
    await page.mouse.up({ clickCount: 2 });
    await pause(page, 800);
  },
  focus: async (page, target) => {
    await target.focus({ timeout: 5000 });
    await pause(page, 300);
  },
  enter: async (page, target) => {
    await target.focus({ timeout: 5000 });
    await page.keyboard.press('Enter');
    await pause(page, 800);
  },
  space: async (page, target) => {
    await target.focus({ timeout: 5000 });
    await page.keyboard.press('Space');
    await pause(page, 800);
  },
  escape: async (page, target) => {
    await target.focus({ timeout: 5000 });
    await page.keyboard.press('Escape');
    await pause(page, 500);
  },
  type: async (page, target) => {
    await target.focus({ timeout: 5000 });
    await page.keyboard.type('gandalf');
    await pause(page, 300);
    await page.keyboard.press('Tab');
    await pause(page, 800);
  },
  arrows: async (page, target) => {
    await target.focus({ timeout: 5000 });
    for (const key of ['ArrowRight', 'ArrowRight', 'ArrowLeft', 'Home', 'End']) {
      await page.keyboard.press(key);
      await pause(page, 500);
    }
  },
  dragSideways: async (page, target) => {
    const { x, y } = await moveTo(page, target);
    await page.mouse.down();
    await page.mouse.move(x - 60, y, { steps: 6 });
    await pause(page, 300);
    await page.mouse.up();
    await pause(page, 800);
  },
  dragUp: async (page, target) => {
    const { x, y } = await moveTo(page, target);
    await page.mouse.down();
    await page.mouse.move(x, y - 40, { steps: 4 });
    await page.mouse.up();
    await pause(page, 800);
  },
  hoverThenShift: async (page, target) => {
    await moveTo(page, target);
    await pause(page, 800);
    await page.keyboard.down('Shift');
    await page.keyboard.up('Shift');
    await pause(page, 300);
  },
  eachOption: async (page, target) => {
    const values = await target.locator('option').evaluateAll((options) => options.map((o) => o.value));
    for (const value of values) {
      // Choosing one can change the list: the lists themselves are compared after each step
      const available = await target.evaluate(
        (select, wanted) => [...select.options].some((option) => option.value === wanted && !option.disabled),
        value,
      );
      if (!available) continue;
      await target.selectOption(value, { timeout: 5000 });
      await pause(page, 300);
    }
  },
  tap: async (page, target) => {
    const { x, y } = await centre(target);
    await page.touchscreen.tap(x, y);
    await pause(page, 800);
  },
};
