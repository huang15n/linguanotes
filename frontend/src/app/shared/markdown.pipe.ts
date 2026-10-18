import { Pipe, PipeTransform } from '@angular/core';
import { marked } from 'marked';

// Le HTML produit est assaini par Angular lors du [innerHTML] (scripts supprimés).
@Pipe({ name: 'markdown' })
export class MarkdownPipe implements PipeTransform {
  transform(value: string | null | undefined): string {
    return value ? (marked.parse(value, { gfm: true, breaks: true, async: false }) as string) : '';
  }
}
