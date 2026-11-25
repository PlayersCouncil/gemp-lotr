/** The formatting elements server markup may keep (MarkdownParser's output, the MOTD's and system texts' tags). */
export const MARKUP_TAGS = [
  'b',
  'strong',
  'i',
  'em',
  'u',
  's',
  'del',
  'strike',
  'code',
  'pre',
  'blockquote',
  'p',
  'ul',
  'ol',
  'li',
  'h1',
  'h2',
  'h3',
  'h4',
  'h5',
  'h6',
] as const;

/**
 * The further elements rich server text keeps (the patch notes: PatchNoteRenderer's Jsoup safelist, `relaxed` plus
 * del, s, ins, hr, mark, kbd, figure, figcaption, details and summary), read only with `RICH_MARKUP`.
 */
export const RICH_MARKUP_TAGS = [
  'div',
  'hr',
  'sub',
  'sup',
  'small',
  'mark',
  'kbd',
  'ins',
  'q',
  'cite',
  'dl',
  'dt',
  'dd',
  'table',
  'caption',
  'thead',
  'tbody',
  'tfoot',
  'tr',
  'th',
  'td',
  'figure',
  'figcaption',
  'details',
  'summary',
] as const;
export type MarkupTag = (typeof MARKUP_TAGS)[number] | (typeof RICH_MARKUP_TAGS)[number];

/** The attributes an element keeps, rich markup only: a list's `start`, a table cell's `colspan` and `rowspan`. */
export type MarkupAttribute = 'start' | 'colspan' | 'rowspan';

/**
 * Server markup (a chat message, the MOTD) as data the view draws element by element: text, line breaks, the allowed
 * formatting elements and links to http(s) addresses. Nothing of it is ever inserted as HTML.
 *
 * Rich markup (`RICH_MARKUP`: the patch notes) has more: the RICH_MARKUP_TAGS (an element then may carry
 * `attributes`), links to the site's own pages (`href` an absolute path), pictures (`image`; `icon` for a culture or
 * twilight icon drawn in the text), cards (`card`: the card display opens on it) and links to places in the hall
 * (`hall-link`: the legacy hall's `#fragment`, which the page turns into its route).
 */
export type MarkupNode =
  | { readonly kind: 'text'; readonly text: string }
  | { readonly kind: 'break' }
  | {
      readonly kind: 'element';
      readonly tag: MarkupTag;
      readonly children: readonly MarkupNode[];
      readonly attributes?: Readonly<Partial<Record<MarkupAttribute, string>>>;
    }
  | { readonly kind: 'link'; readonly href: string; readonly children: readonly MarkupNode[] }
  | {
      readonly kind: 'image';
      readonly src: string;
      readonly alt: string;
      readonly title: string | null;
      readonly icon: boolean;
    }
  | {
      readonly kind: 'card';
      readonly blueprintId: string;
      readonly title: string | null;
      readonly children: readonly MarkupNode[];
    }
  | { readonly kind: 'hall-link'; readonly hash: string; readonly children: readonly MarkupNode[] };

/** How much of the markup to keep. */
export interface MarkupOptions {
  /** Also the RICH_MARKUP_TAGS, the site's own links and pictures, cards and hall links (see MarkupNode). */
  readonly rich?: boolean;
}

/** The patch notes' markup (PatchNoteRenderer, CultureIcons). */
export const RICH_MARKUP: MarkupOptions = { rich: true };

const ALLOWED = new Set<string>(MARKUP_TAGS);
const RICH_ALLOWED = new Set<string>(RICH_MARKUP_TAGS);
/** Elements dropped with everything in them. */
const DROPPED = new Set(['script', 'style', 'template', 'iframe', 'object', 'embed', 'noscript', 'title', 'head']);
const WEB_ADDRESS = /^https?:\/\//i;
/** An address with a scheme (http:, javascript:, data:, ...) or a protocol-relative one (//host). */
const OTHER_PLACE = /^(?:[a-z][a-z0-9+.-]*:|\/\/)/i;
/** Where the server's relative addresses point: next to the legacy pages (patchnotes/..., images/cultures/...). */
const LEGACY_PAGES_URL = '/gemp-lotr/';
/** A card the server names (LibraryCardLookup.BLUEPRINT_ID; PatchNotesUI.openCard checks the same). */
const BLUEPRINT_ID = /^\d{1,3}_\d{1,4}$/;
const POSITIVE = /^[1-9]\d{0,3}$/;

// Node types (Node.ELEMENT_NODE, Node.TEXT_NODE), without reaching for the browser's globals
const ELEMENT = 1;
const TEXT = 3;

/**
 * Reads server markup into MarkupNodes. The markup is parsed inert (DOMParser runs no scripts and loads nothing);
 * then only text, <br>, the MARKUP_TAGS and <a href="http(s)://..."> are kept. Any other element gives way to its
 * content (a <span> or <div> to its text, an <img> to nothing), and attributes are dropped (a link keeps its address
 * only; the view opens it in a new tab). With `RICH_MARKUP` (the patch notes) the further nodes of MarkupNode are kept
 * too; chat and the MOTD are read as before.
 */
export function parseServerMarkup(html: string, options: MarkupOptions = {}): MarkupNode[] {
  if (html === '') {
    return [];
  }
  const parsed = new DOMParser().parseFromString(`<!DOCTYPE html><body>${html}`, 'text/html');
  return readChildren(parsed.body, options.rich === true);
}

function readChildren(parent: Node, rich: boolean): MarkupNode[] {
  const nodes: MarkupNode[] = [];
  parent.childNodes.forEach((child) => nodes.push(...readNode(child, rich)));
  return nodes;
}

function readNode(node: Node, rich: boolean): MarkupNode[] {
  if (node.nodeType === TEXT) {
    const text = node.nodeValue ?? '';
    return text === '' ? [] : [{ kind: 'text', text }];
  }
  if (node.nodeType !== ELEMENT) {
    return [];
  }
  const element = node as Element;
  const tag = element.tagName.toLowerCase();
  if (tag === 'br') {
    return [{ kind: 'break' }];
  }
  if (DROPPED.has(tag)) {
    return [];
  }
  if (rich && tag === 'img') {
    return readImage(element);
  }
  const children = readChildren(element, rich);
  if (tag === 'a') {
    const href = element.getAttribute('href')?.trim() ?? '';
    if (WEB_ADDRESS.test(href)) {
      return [{ kind: 'link', href, children }];
    }
    if (rich && href.length > 1 && href.startsWith('#')) {
      return [{ kind: 'hall-link', hash: href, children }];
    }
    const local = rich ? sitePath(href) : null;
    return local === null ? children : [{ kind: 'link', href: local, children }];
  }
  if (rich && tag === 'span' && element.classList.contains('cardHint')) {
    const blueprintId = element.getAttribute('value')?.trim() ?? '';
    if (BLUEPRINT_ID.test(blueprintId)) {
      return [{ kind: 'card', blueprintId, title: element.getAttribute('title'), children }];
    }
  }
  if (ALLOWED.has(tag) || (rich && RICH_ALLOWED.has(tag))) {
    const attributes = rich ? readAttributes(element, tag) : null;
    return [
      attributes === null
        ? { kind: 'element', tag: tag as MarkupTag, children }
        : { kind: 'element', tag: tag as MarkupTag, children, attributes },
    ];
  }
  return children;
}

/** A picture at a web address or one of the site's own; anything else (data:, javascript:) is dropped. */
function readImage(element: Element): MarkupNode[] {
  const raw = element.getAttribute('src')?.trim() ?? '';
  const src = WEB_ADDRESS.test(raw) ? raw : sitePath(raw);
  if (src === null) {
    return [];
  }
  return [
    {
      kind: 'image',
      src,
      alt: element.getAttribute('alt') ?? '',
      title: element.getAttribute('title'),
      icon: element.classList.contains('patchnote-culture'),
    },
  ];
}

/**
 * A path on this site: an absolute one as it is, a relative one made the legacy pages' (the server writes them
 * relative to hall.html: `patchnotes/2026/img/a.png`, `images/cultures/gondor.png`). Null for anything else.
 */
function sitePath(address: string): string | null {
  if (address === '' || address.startsWith('#') || OTHER_PLACE.test(address)) {
    return null;
  }
  if (address.startsWith('/')) {
    return address;
  }
  return LEGACY_PAGES_URL + address.replace(/^(?:\.\/)+/, '');
}

/** The attributes the rich markup keeps (an ordered list's `start`, a cell's spans), or null when there are none. */
function readAttributes(element: Element, tag: string): Partial<Record<MarkupAttribute, string>> | null {
  const names: readonly MarkupAttribute[] =
    tag === 'td' || tag === 'th' ? ['colspan', 'rowspan'] : tag === 'ol' ? ['start'] : [];
  const kept: Partial<Record<MarkupAttribute, string>> = {};
  let any = false;
  for (const name of names) {
    const value = element.getAttribute(name)?.trim() ?? '';
    if (POSITIVE.test(value)) {
      kept[name] = value;
      any = true;
    }
  }
  return any ? kept : null;
}
