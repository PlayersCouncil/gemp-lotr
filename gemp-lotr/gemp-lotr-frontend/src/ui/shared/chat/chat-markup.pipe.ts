import { Pipe, PipeTransform } from '@angular/core';
import { MarkupNode, parseServerMarkup } from '../../../core/chat/chat-markup';

/** Server markup as MarkupNodes, for `gd-markup` (read once per text: a pure pipe). */
@Pipe({ name: 'serverMarkup' })
export class ServerMarkupPipe implements PipeTransform {
  transform(html: string | null): MarkupNode[] {
    return parseServerMarkup(html ?? '');
  }
}
