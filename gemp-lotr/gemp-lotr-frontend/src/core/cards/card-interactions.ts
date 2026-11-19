import { Provider, Type } from '@angular/core';
import { AUTO_ZOOM_COOKIE_NAME, AutoZoomSetting } from '../platform/auto-zoom-setting';
import { CardActions } from './card-actions';
import { CardHover } from './card-hover.service';
import { CardInfoService } from './card-info.service';
import { CardViews } from './card-views.service';

export interface CardInteractionsOptions {
  /** What using a card does on this page. */
  actions: Type<CardActions>;
  /** The cookie this page remembers its hover zoom setting in (the legacy pages each have their own). */
  autoZoomCookie: string;
}

/**
 * What any page that shows cards needs, for its component's `providers`: `button[gdCard]`, `gd-card-info` and
 * `gd-auto-zoom` work with these and nothing else. Created and destroyed with the page.
 */
export function provideCardInteractions({ actions, autoZoomCookie }: CardInteractionsOptions): Provider[] {
  return [
    actions,
    { provide: CardActions, useExisting: actions },
    { provide: AUTO_ZOOM_COOKIE_NAME, useValue: autoZoomCookie },
    AutoZoomSetting,
    CardHover,
    CardInfoService,
    CardViews,
  ];
}
