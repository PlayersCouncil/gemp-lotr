import { Provider } from '@angular/core';
import { CardGestures } from '../../shared/card/card-gestures';
import { CardPicker } from './card-picker';
import { EditPopups, TableEditGestures } from './edit-popups';
import { PathPopups } from './path-popups';
import { CounterPopups } from './counter-popups';

/**
 * The edit controls' view state, for the game page's providers: which popup is open (EditPopups), picking a card on
 * the table (CardPicker), and a card's right-click while the table is edited (CardGestures: the card menu).
 */
export function provideTableEditUi(): Provider[] {
  return [
    EditPopups,
    CardPicker,
    TableEditGestures,
    { provide: CardGestures, useExisting: TableEditGestures },
    PathPopups,
    CounterPopups,
  ];
}
