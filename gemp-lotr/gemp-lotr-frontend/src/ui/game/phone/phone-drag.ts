import { capturePointer } from '../../shared/pointer-drag/pointer-drag.directive';

/**
 * Follows a drag that sizes the hand: from a press on `element` (the divider, or the hand's own grip), how far the
 * pointer has gone up since the press (px; negative: down), while it moves (`move`) and where it is let go (`drop`).
 * The hand grows by as much as the pointer goes up, wherever the element is (held sideways, the divider is beside the
 * hand, not over it).
 */
export function followHandDrag(
  element: HTMLElement,
  event: PointerEvent,
  move: (up: number) => void,
  drop: (up: number) => void,
): void {
  event.preventDefault();
  capturePointer(element, event.pointerId);
  const start = event.clientY;
  const moved = (e: PointerEvent) => move(start - e.clientY);
  const up = (e: PointerEvent) => {
    element.removeEventListener('pointermove', moved);
    element.removeEventListener('pointerup', up);
    element.removeEventListener('pointercancel', up);
    drop(start - e.clientY);
  };
  element.addEventListener('pointermove', moved);
  element.addEventListener('pointerup', up);
  element.addEventListener('pointercancel', up);
}
