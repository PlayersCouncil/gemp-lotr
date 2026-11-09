import { NgTemplateOutlet } from '@angular/common';
import { Component, Directive, inject, linkedSignal } from '@angular/core';
import { disabled, form, FormField, maxLength } from '@angular/forms/signals';
import {
  CollectionBrowser,
  PAGE_SIZE_MAX,
  PAGE_SIZE_MIN,
  PAGE_SIZE_SLIDER_START,
} from '../../../core/collection/collection-browser.service';
import { CollectionQuery, FilterFieldName } from '../../../core/collection/collection-query.service';
import { CULTURE_GROUPS } from '../../../core/collection/filter-cultures';
import { NUMERIC_FILTERS, NumericFilter } from '../../../core/collection/filter-fields';
import {
  CARD_TYPE_OPTIONS,
  COMPARE_OPTIONS,
  ITEM_CLASS_OPTIONS,
  KEYWORD_OPTIONS,
  PHASE_OPTIONS,
  PRODUCT_OPTIONS,
  RACE_OPTIONS,
  RARITY_OPTIONS,
  SelectOption,
  SIDE_OPTIONS,
  SIGNET_OPTIONS,
  SORT_OPTIONS,
} from '../../../core/collection/filter-options';

type TextField = FilterFieldName;

/** A select field of the filter, in the legacy filter table's order. */
interface SelectField {
  kind: 'select';
  field: TextField;
  label: string;
  options: () => readonly SelectOption[];
  /** Also usable when the Product filter is set to packs (only the product filter itself). */
  forPacks?: boolean;
  wide?: boolean;
}

interface TextInputField {
  kind: 'text';
  field: TextField;
  label: string;
  forPacks?: boolean;
}

interface NumericField {
  kind: 'numeric';
  stat: NumericFilter;
  label: string;
}

type Field = SelectField | TextInputField | NumericField;

/** What can't filter packs: every field but the Product and the name. */
const FIELDS_OFF_FOR_PACKS: readonly TextField[] = [
  'rarity',
  'block',
  'set',
  'side',
  'sort',
  'cardType',
  'gametext',
  'keyword',
  'phase',
  'race',
  'itemClass',
  'signet',
  ...NUMERIC_FILTERS.map((stat) => `${stat}Compare` as const),
];

/** Makes `let-field` in the field template a Field, for the template's type checks. */
@Directive({ selector: 'ng-template[gdFieldRow]' })
export class FieldRowTemplate {
  static ngTemplateContextGuard(_directive: FieldRowTemplate, context: unknown): context is { $implicit: Field } {
    return context !== null;
  }
}

/**
 * The collection filter: paging and page size, the filter fields (basic, then advanced), the two reset buttons and the
 * culture toggles. Every control is a native element, bound with Signal Forms; changes go to CollectionQuery and CollectionBrowser.
 *
 * DOM contract (`.gd-filter`):
 * - `.gd-pager`: `button.gd-pager__previous`, `input[type=range].gd-pager__size`, `button.gd-pager__next`, each
 *   button with `data-icon` (previous, next) for the theme to draw;
 * - `.gd-filter__fields` with `.gd-filter__basic` and `.gd-filter__advanced` (whose `.gd-filter__advanced-title`
 *   links the wiki search); each field a `label.gd-field` (`data-field`, `data-kind` = select, text or numeric,
 *   `data-wide` for the wide ones) holding `.gd-field__label` and `.gd-field__control` with the native control(s);
 * - `.gd-filter__resets` with two `button.gd-button` (`data-action` = reset-advanced, reset-all);
 * - `.gd-cultures`, groups `.gd-cultures__group` (`data-group`, hidden when the format doesn't use them) of
 *   `label.gd-culture` (`data-culture`, `data-active`, `data-icon-kind` = culture or other) holding a visually hidden
 *   `input[type=checkbox].gd-culture__input` and `img.gd-culture__icon`.
 */
@Component({
  selector: 'gd-collection-filter',
  imports: [NgTemplateOutlet, FormField, FieldRowTemplate],
  host: { class: 'gd-filter' },
  templateUrl: './collection-filter.component.html',
})
export class CollectionFilterComponent {
  protected readonly query = inject(CollectionQuery);
  protected readonly browser = inject(CollectionBrowser);

  protected readonly pageSize = { min: PAGE_SIZE_MIN, max: PAGE_SIZE_MAX, start: PAGE_SIZE_SLIDER_START };
  protected readonly cultureGroups = CULTURE_GROUPS;
  protected readonly compareOptions = COMPARE_OPTIONS;
  protected readonly wikiSearch = 'https://wiki.lotrtcgpc.net/wiki/Special:RunQuery/CardSearch';

  protected readonly basicFields: readonly Field[] = [
    { kind: 'select', field: 'product', label: 'Product:', options: () => PRODUCT_OPTIONS, forPacks: true },
    { kind: 'select', field: 'rarity', label: 'Rarity:', options: () => RARITY_OPTIONS },
    { kind: 'select', field: 'block', label: 'Block:', options: () => this.query.blockOptions() },
    { kind: 'select', field: 'set', label: 'Set:', options: () => this.query.setOptions() },
    { kind: 'select', field: 'side', label: 'Side:', options: () => SIDE_OPTIONS },
    { kind: 'select', field: 'sort', label: 'Sort by:', options: () => SORT_OPTIONS },
    { kind: 'select', field: 'cardType', label: 'Card\u00a0Type:', options: () => CARD_TYPE_OPTIONS, wide: true },
  ];

  protected readonly advancedFields: readonly Field[] = [
    { kind: 'text', field: 'name', label: 'Name\u00a0contains:', forPacks: true },
    { kind: 'text', field: 'gametext', label: 'Game\u00a0Text\u00a0contains:' },
    { kind: 'select', field: 'keyword', label: 'Keyword:', options: () => KEYWORD_OPTIONS },
    { kind: 'select', field: 'phase', label: 'Phase:', options: () => PHASE_OPTIONS },
    { kind: 'select', field: 'race', label: 'Race:', options: () => RACE_OPTIONS },
    { kind: 'select', field: 'itemClass', label: 'Item Class:', options: () => ITEM_CLASS_OPTIONS },
    { kind: 'numeric', stat: 'twilight', label: 'Twilight:' },
    { kind: 'numeric', stat: 'siteNumber', label: 'Site Number:' },
    { kind: 'numeric', stat: 'strength', label: 'Strength:' },
    { kind: 'select', field: 'signet', label: 'Signet:', options: () => SIGNET_OPTIONS },
    { kind: 'numeric', stat: 'vitality', label: 'Vitality:' },
    { kind: 'numeric', stat: 'resistance', label: 'Resistance:' },
  ];

  /**
   * The form's controls show this copy of the filter fields. It follows the query, and what the player does goes to the
   * query when a control's change is finished (so typing doesn't reload the collection at each key). The comparison
   * numbers stay plain inputs: the form's number limits need a numeric model, and these are text for the server.
   */
  private readonly formValues = linkedSignal(() => this.query.fields());

  protected readonly filterForm = form(this.formValues, (path) => {
    const packs = () => this.query.isPackProduct();
    for (const name of FIELDS_OFF_FOR_PACKS) {
      disabled(path[name], { when: packs });
    }
    maxLength(path.name, 100);
    maxLength(path.gametext, 100);
  });

  protected isWide(field: Field): boolean {
    return field.kind === 'text' || (field.kind === 'select' && field.wide === true);
  }

  protected fieldName(field: Field): string {
    return field.kind === 'numeric' ? field.stat : field.field;
  }

  protected control(field: TextField) {
    return this.filterForm[field];
  }

  protected value(field: TextField): string {
    return this.query.fields()[field];
  }

  protected set(field: TextField, event: Event): void {
    this.query.setField(field, (event.target as HTMLInputElement | HTMLSelectElement).value);
  }

  protected compareField(stat: NumericFilter): TextField {
    return `${stat}Compare`;
  }

  protected valueField(stat: NumericFilter): TextField {
    return `${stat}Value`;
  }

  protected setPageSize(event: Event): void {
    this.browser.setPageSize(Number((event.target as HTMLInputElement).value));
  }

  protected isCultureActive(code: string): boolean {
    return this.query.fields().cultures.includes(code);
  }
}
