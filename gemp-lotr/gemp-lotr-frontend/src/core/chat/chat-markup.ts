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
export type MarkupTag = (typeof MARKUP_TAGS)[number];

/**
 * Server markup (a chat message, the MOTD) as data the view draws element by element: text, line breaks, the allowed
 * formatting elements and links to http(s) addresses. Nothing of it is ever inserted as HTML.
 */
export type MarkupNode =
  | { readonly kind: 'text'; readonly text: string }
  | { readonly kind: 'break' }
  | { readonly kind: 'element'; readonly tag: MarkupTag; readonly children: readonly MarkupNode[] }
  | { readonly kind: 'link'; readonly href: string; readonly children: readonly MarkupNode[] };

const ALLOWED = new Set<string>(MARKUP_TAGS);
/** Elements dropped with everything in them. */
const DROPPED = new Set(['script', 'style', 'template', 'iframe', 'object', 'embed', 'noscript', 'title', 'head']);
const WEB_ADDRESS = /^https?:\/\//i;

// Node types (Node.ELEMENT_NODE, Node.TEXT_NODE), without reaching for the browser's globals
const ELEMENT = 1;
const TEXT = 3;

/**
 * Reads server markup into MarkupNodes. The markup is parsed inert (DOMParser runs no scripts and loads nothing);
 * then only text, <br>, the MARKUP_TAGS and <a href="http(s)://..."> are kept. Any other element gives way to its
 * content (a <span> or <div> to its text, an <img> to nothing), and attributes are dropped (a link keeps its address
 * only; the view opens it in a new tab).
 */
export function parseServerMarkup(html: string): MarkupNode[] {
  if (html === '') {
    return [];
  }
  const parsed = new DOMParser().parseFromString(`<!DOCTYPE html><body>${html}`, 'text/html');
  return readChildren(parsed.body);
}

function readChildren(parent: Node): MarkupNode[] {
  const nodes: MarkupNode[] = [];
  parent.childNodes.forEach((child) => nodes.push(...readNode(child)));
  return nodes;
}

function readNode(node: Node): MarkupNode[] {
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
  const children = readChildren(element);
  if (tag === 'a') {
    const href = element.getAttribute('href')?.trim() ?? '';
    return WEB_ADDRESS.test(href) ? [{ kind: 'link', href, children }] : children;
  }
  if (ALLOWED.has(tag)) {
    return [{ kind: 'element', tag: tag as MarkupTag, children }];
  }
  return children;
}
