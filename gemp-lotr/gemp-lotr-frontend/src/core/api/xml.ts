/** Small helpers for reading the Gemp server's XML responses. */

export function parseXml(text: string): Document {
  const doc = new DOMParser().parseFromString(text, 'application/xml');
  if (doc.getElementsByTagName('parsererror').length > 0) {
    throw new Error('Response is not valid XML');
  }
  return doc;
}

/** The root element, checked against the expected tag name (the legacy client silently ignored other roots). */
export function rootElement(doc: Document, expectedTag: string): Element | null {
  const root = doc.documentElement;
  return root.tagName === expectedTag ? root : null;
}

/** Direct child elements with the given tag name, in document order. */
export function childElements(parent: Element, tag: string): Element[] {
  return Array.from(parent.children).filter((child) => child.tagName === tag);
}

export function firstChild(parent: Element, tag: string): Element | null {
  return childElements(parent, tag)[0] ?? null;
}

export function attr(element: Element, name: string): string | null {
  return element.getAttribute(name);
}

export function requiredAttr(element: Element, name: string): string {
  const value = element.getAttribute(name);
  if (value === null) {
    throw new Error(`<${element.tagName}> is missing the "${name}" attribute`);
  }
  return value;
}

export function intAttr(element: Element, name: string, fallback: number): number {
  const value = element.getAttribute(name);
  if (value === null || value === '') {
    return fallback;
  }
  const parsed = parseInt(value, 10);
  return Number.isNaN(parsed) ? fallback : parsed;
}

export function boolAttr(element: Element, name: string): boolean {
  return element.getAttribute(name) === 'true';
}
