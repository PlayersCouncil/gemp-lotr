import { NgTemplateOutlet } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import {
  CARD_TYPE_OPTIONS,
  COMPARE_OPTIONS,
  CULTURE_GROUPS,
  FilterFields,
  ITEM_CLASS_OPTIONS,
  KEYWORD_OPTIONS,
  NumericFilter,
  PHASE_OPTIONS,
  PRODUCT_OPTIONS,
  RACE_OPTIONS,
  RARITY_OPTIONS,
  SelectOption,
  SIDE_OPTIONS,
  SIGNET_OPTIONS,
  SORT_OPTIONS,
} from '../core/collection/card-filter';
import {
  CollectionService,
  PAGE_SIZE_MAX,
  PAGE_SIZE_MIN,
  PAGE_SIZE_SLIDER_START,
} from '../core/collection/collection.service';
import { UiButtonDirective } from '../shared/ui-button.directive';
import { UiSliderComponent } from '../shared/ui-slider.component';

type TextField = Exclude<keyof FilterFields, 'cultures'>;

/**
 * The collection filter (#filter-main): paging and page size, the filter table, the reset buttons and the culture
 * buttons. Same markup as the legacy CardFilter.buildUi after jQuery UI had turned the buttons into widgets.
 */
@Component({
  selector: 'div[gempCardFilter]',
  imports: [NgTemplateOutlet, UiButtonDirective, UiSliderComponent],
  templateUrl: './card-filter.component.html',
})
export class CardFilterComponent {
  protected readonly collection = inject(CollectionService);

  protected readonly options = {
    product: PRODUCT_OPTIONS,
    rarity: RARITY_OPTIONS,
    side: SIDE_OPTIONS,
    sort: SORT_OPTIONS,
    cardType: CARD_TYPE_OPTIONS,
    keyword: KEYWORD_OPTIONS,
    phase: PHASE_OPTIONS,
    race: RACE_OPTIONS,
    itemClass: ITEM_CLASS_OPTIONS,
    signet: SIGNET_OPTIONS,
    compare: COMPARE_OPTIONS,
  };
  protected readonly cultureGroups = CULTURE_GROUPS;
  protected readonly firstCulture = CULTURE_GROUPS[0].cultures[0].code;
  protected readonly lastCulture = CULTURE_GROUPS.at(-1)!.cultures.at(-1)!.code;
  protected readonly pageSize = {
    min: PAGE_SIZE_MIN,
    max: PAGE_SIZE_MAX,
    start: PAGE_SIZE_SLIDER_START,
  };

  /** Hover and keyboard focus of the culture buttons (jQuery UI toggle buttons). */
  protected readonly hoveredCulture = signal<string | null>(null);
  protected readonly focusedCulture = signal<string | null>(null);
  /** jQuery UI ignored a click on a toggle button when the mouse moved between press and release. */
  private pressedAt: { x: number; y: number } | null = null;
  private clickDragged = false;

  protected value(field: TextField): string {
    return this.collection.fields()[field];
  }

  protected set(field: TextField, event: Event): void {
    this.collection.setField(field, (event.target as HTMLInputElement | HTMLSelectElement).value);
  }

  protected isSelected(field: TextField, option: SelectOption): boolean {
    return this.collection.fields()[field] === option.value;
  }

  protected compareField(stat: NumericFilter): TextField {
    return `${stat}Compare`;
  }

  protected valueField(stat: NumericFilter): TextField {
    return `${stat}Value`;
  }

  protected isCultureActive(code: string): boolean {
    return this.collection.fields().cultures.includes(code);
  }

  protected cultureMouseDown(event: MouseEvent): void {
    this.clickDragged = false;
    this.pressedAt = { x: event.pageX, y: event.pageY };
  }

  protected cultureMouseUp(event: MouseEvent): void {
    if (this.pressedAt && (this.pressedAt.x !== event.pageX || this.pressedAt.y !== event.pageY)) {
      this.clickDragged = true;
    }
  }

  protected cultureClick(event: MouseEvent): void {
    if (this.clickDragged) {
      event.preventDefault();
    }
  }
}
