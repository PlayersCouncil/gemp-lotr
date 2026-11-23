// Finds every place in the Angular app's TypeScript where a value, a parameter or a declaration has the type `any`:
// written (`: any`, `as any`, `<any>`) or implicit (JSON.parse, an untyped library, a spy's arguments, a `catch`
// without a type). The app must have none, specs and test helpers included. Template expressions are checked by
// strictTemplates and the lint's ban on $any().
//
//   node scripts/find-any.mjs            prints each place, exits 1 when there is one
//   node scripts/find-any.mjs --summary  only the count per file
import ts from 'typescript';
import { dirname, relative, resolve } from 'node:path';
import { fileURLToPath } from 'node:url';

const ROOT = resolve(dirname(fileURLToPath(import.meta.url)), '..');
const PROJECTS = ['tsconfig.app.json', 'tsconfig.spec.json'];
const summary = process.argv.includes('--summary');

/**
 * The type `any` itself, or an array or a promise of it (JSON.parse's result, `any[]`, `Promise<any>`). A library's own
 * generic with `any` inside (HttpRequest<any>, Provider) is not counted: its `any` shows where our code reads it.
 */
function anyIn(checker, type) {
  if (type.flags & ts.TypeFlags.Any) return type.intrinsicName !== 'error';
  if (type.flags & ts.TypeFlags.Object && type.objectFlags & ts.ObjectFlags.Reference) {
    const name = type.target?.symbol?.name;
    if (name === 'Array' || name === 'ReadonlyArray' || name === 'Promise') {
      return checker.getTypeArguments(type).some((arg) => (arg.flags & ts.TypeFlags.Any) !== 0);
    }
  }
  return false;
}

/** The nodes whose type counts: names (variables, parameters, properties, calls' results) and written types. */
function interesting(node) {
  if (node.kind === ts.SyntaxKind.AnyKeyword) return true;
  if (ts.isIdentifier(node)) {
    // A name in an import or a type position names a type, not a value
    const parent = node.parent;
    if (ts.isImportSpecifier(parent) || ts.isImportClause(parent) || ts.isNamespaceImport(parent)) return false;
    if (ts.isTypeReferenceNode(parent) || ts.isQualifiedName(parent) || ts.isExpressionWithTypeArguments(parent)) {
      return false;
    }
    if (ts.isPropertyAccessExpression(parent) && parent.expression === node && ts.isCallExpression(parent.parent)) {
      return true;
    }
    return true;
  }
  if (!(ts.isCallExpression(node) || ts.isAwaitExpression(node) || ts.isElementAccessExpression(node))) return false;
  // A result thrown away (`await fixture.whenStable();`) is never used: only a library's signature says any
  let outer = node;
  while (ts.isAwaitExpression(outer.parent) || ts.isParenthesizedExpression(outer.parent)) outer = outer.parent;
  return !ts.isExpressionStatement(outer.parent) && !ts.isVoidExpression(outer.parent);
}

/** A library's `any` narrowed where it comes in (`JSON.parse(text) as unknown`, `nativeElement as HTMLElement`). */
function narrowedAtOnce(node) {
  // The name in `a.b` stands for the whole `a.b`
  let outer = ts.isPropertyAccessExpression(node.parent) && node.parent.name === node ? node.parent : node;
  while (ts.isParenthesizedExpression(outer.parent) || ts.isNonNullExpression(outer.parent)) outer = outer.parent;
  return ts.isAsExpression(outer.parent) && outer.parent.expression === outer;
}

const found = new Map();
const seen = new Set();
for (const project of PROJECTS) {
  const configPath = resolve(ROOT, project);
  const config = ts.getParsedCommandLineOfConfigFile(
    configPath,
    {},
    { ...ts.sys, onUnRecoverableConfigFileDiagnostic() {} },
  );
  const program = ts.createProgram(config.fileNames, config.options);
  const checker = program.getTypeChecker();
  for (const file of program.getSourceFiles()) {
    if (file.isDeclarationFile || !file.fileName.startsWith(resolve(ROOT, 'src'))) continue;
    if (seen.has(file.fileName)) continue;
    seen.add(file.fileName);
    const name = relative(ROOT, file.fileName);
    const visit = (node) => {
      if (interesting(node)) {
        const isKeyword = node.kind === ts.SyntaxKind.AnyKeyword;
        let type;
        try {
          type = isKeyword ? null : checker.getTypeAtLocation(node);
        } catch {
          type = null;
        }
        if (isKeyword || (type && anyIn(checker, type) && !narrowedAtOnce(node))) {
          const { line } = file.getLineAndCharacterOfPosition(node.getStart(file));
          const places = found.get(name) ?? new Map();
          const what = isKeyword ? 'written any' : `${node.getText(file).slice(0, 60)}: ${checker.typeToString(type)}`;
          // One report per line and text: a name used twice on a line is one place
          places.set(`${line + 1}|${what}`, { line: line + 1, what, code: file.text.split('\n')[line].trim() });
          found.set(name, places);
          // Inside an any, everything is any: report the outermost only
          if (!isKeyword && !ts.isIdentifier(node)) return;
        }
      }
      ts.forEachChild(node, visit);
    };
    visit(file);
  }
}

let total = 0;
for (const [name, places] of [...found].sort(([a], [b]) => a.localeCompare(b))) {
  total += places.size;
  if (summary) {
    console.log(`${String(places.size).padStart(4)}  ${name}`);
    continue;
  }
  for (const { line, what, code } of [...places.values()].sort((a, b) => a.line - b.line)) {
    console.log(`${name}:${line}  ${what}\n      ${code}`);
  }
}
console.log(total === 0 ? 'No any.' : `${total} places with the type any in ${found.size} files.`);
process.exit(total === 0 ? 0 : 1);
