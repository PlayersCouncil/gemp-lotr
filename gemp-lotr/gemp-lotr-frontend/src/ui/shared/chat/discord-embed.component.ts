import { DOCUMENT } from '@angular/common';
import { Component, ElementRef, inject, input, OnInit, Renderer2 } from '@angular/core';
import { DISCORD_WIDGET } from '../../../core/chat/chat-messages';

/** Marks the WidgetBot script, so it is added to the page once. */
const SCRIPT_ATTRIBUTE = 'data-gd-widgetbot';

/**
 * The Discord chat shown instead of the built-in one (chat.js handleChatVisibility): WidgetBot's script, added to the
 * page once, and its `<widgetbot>` element with the server, the channel and the player's name. Created when first
 * shown (the owner renders it from then on), so players who never choose Discord never load it.
 *
 * DOM contract: `gd-discord-embed.gd-discord-embed` holding `widgetbot.gd-discord-embed__widget`.
 */
@Component({
  selector: 'gd-discord-embed',
  styleUrl: './discord-embed.component.css',
  host: { class: 'gd-discord-embed' },
  template: '',
})
export class DiscordEmbedComponent implements OnInit {
  /** The name WidgetBot shows for the player (empty until known). */
  readonly username = input('');

  private readonly host = inject<ElementRef<HTMLElement>>(ElementRef).nativeElement;
  private readonly renderer = inject(Renderer2);
  private readonly page = inject(DOCUMENT);

  ngOnInit(): void {
    if (!this.page.head.querySelector(`script[${SCRIPT_ATTRIBUTE}]`)) {
      const script = this.renderer.createElement('script') as HTMLScriptElement;
      this.renderer.setAttribute(script, SCRIPT_ATTRIBUTE, '');
      this.renderer.setAttribute(script, 'src', DISCORD_WIDGET.script);
      this.renderer.appendChild(this.page.head, script);
    }
    const widget = this.renderer.createElement('widgetbot') as HTMLElement;
    this.renderer.addClass(widget, 'gd-discord-embed__widget');
    this.renderer.setAttribute(widget, 'server', DISCORD_WIDGET.server);
    this.renderer.setAttribute(widget, 'channel', DISCORD_WIDGET.channel);
    this.renderer.setAttribute(widget, 'shard', DISCORD_WIDGET.shard);
    this.renderer.setAttribute(widget, 'username', this.username());
    this.renderer.appendChild(this.host, widget);
  }
}
