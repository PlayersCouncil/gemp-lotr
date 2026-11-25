import { Component, inject, ViewEncapsulation } from '@angular/core';
import { CardImageService } from '../../../core/cards/card-image.service';
import { HallDeliveries } from '../../../core/hall/deliveries/hall-deliveries.service';
import {
  announcementTitle,
  DISMISS,
  newItemsTitle,
  REMIND_LATER,
  RTMD_TITLE,
} from '../../../core/hall/deliveries/hall-deliveries.service';
import { HallDialogs } from '../../../core/hall/hall-dialogs.service';
import { pageMarkup } from '../../../core/hall/hall-dialogs.service';
import {
  readyCheckText,
  READY_CHECK_OK,
  READY_CHECK_READY,
  READY_CHECK_TITLE,
} from '../../../core/hall/queues/hall-queue-messages';
import { HallQueuesService } from '../../../core/hall/queues/hall-queues.service';
import { HallReadyOffers } from '../../../core/hall/ready/hall-ready-offers.service';
import { MarkupComponent } from '../../shared/chat/markup.component';
import { DialogComponent } from '../../shared/dialog/dialog.component';

/**
 * The Game Hall's dialogs over whatever tab is shown (hallUi.js showDialog, showReadyCheckDialog, showReadyModal;
 * deliveryService.js, announcementDeliveryService.js, leagueDeliveryService.js): the hall's message dialogs (OK),
 * the ready check (Ready, OK, its seconds counting down), "Your game / draft is ready" (no close button: a link),
 * "New items - <collection>", the announcement (Dismiss, Remind Me Later; a link into the hall counts as Dismiss and
 * goes there) and the Race to Mount Doom move.
 *
 * DOM contract: `gd-game-hall-dialogs` (no box) holding `gd-dialog`s: `data-dialog=hall-message` (`gd-markup`,
 * `button.gd-button[data-action=ok]`), `hall-info` (the same without OK, not modal), `ready-check` (`div.gd-game-hall-dialogs__ready-check`,
 * `button.gd-button[data-action=ready|ok]`), `ready-offer` (`p`, `a.gd-button.gd-game-hall-dialogs__open`
 * `target=_blank`), `new-items` (`div.gd-game-hall-dialogs__items` of `span.gd-game-hall-dialogs__item` with an `img`
 * and, past 1, `span.gd-game-hall-dialogs__count`), `announcement` (`div.gd-game-hall-dialogs__announcement`,
 * `button.gd-button[data-action=dismiss|snooze]`) and `rtmd` (`div.gd-game-hall-dialogs__rtmd` with
 * `p.gd-game-hall-dialogs__rtmd-message` and `span.gd-game-hall-dialogs__site[data-site=old|new]`, an arrow between).
 */
@Component({
  selector: 'gd-game-hall-dialogs',
  styleUrl: './game-hall-dialogs.component.css',
  // Not scoped: it sizes the dialogs it opens (their rules all start at gd-game-hall-dialogs)
  encapsulation: ViewEncapsulation.None,
  imports: [DialogComponent, MarkupComponent],
  host: { style: 'display: contents' },
  template: `
    @for (dialog of dialogs.open(); track dialog.id) {
      <gd-dialog
        [name]="dialog.ok ? 'hall-message' : 'hall-info'"
        [heading]="dialog.title"
        [open]="true"
        [modal]="dialog.ok"
        (closed)="dialogs.close(dialog.id)"
      >
        <div class="gd-game-hall-dialogs__message"><gd-markup [nodes]="dialog.body" /></div>
        @if (dialog.ok) {
          <div class="gd-dialog__actions" gdDialogActions>
            <button type="button" class="gd-button" data-action="ok" (click)="dialogs.close(dialog.id)">OK</button>
          </div>
        }
      </gd-dialog>
    }
    @for (check of queues.readyCheckDialogs(); track check.queueId) {
      <gd-dialog
        name="ready-check"
        [heading]="readyCheckTitle"
        [open]="true"
        (closed)="queues.closeReadyCheckDialog(check.queueId)"
      >
        <div class="gd-game-hall-dialogs__ready-check">
          <gd-markup [nodes]="readyCheckMarkup(check.queueName, check.seconds)" />
        </div>
        <div class="gd-dialog__actions" gdDialogActions>
          <button type="button" class="gd-button" data-action="ready" (click)="queues.confirmReady(check.queueId)">
            {{ readyWord }}
          </button>
          <button
            type="button"
            class="gd-button"
            data-action="ok"
            (click)="queues.closeReadyCheckDialog(check.queueId)"
          >
            {{ okWord }}
          </button>
        </div>
      </gd-dialog>
    }
    @for (offer of offers.offers(); track offer.key) {
      <gd-dialog name="ready-offer" [heading]="offer.title" [open]="true" [closable]="false">
        <p class="gd-game-hall-dialogs__offer">{{ offer.text }}</p>
        <p class="gd-game-hall-dialogs__offer-actions">
          <a
            class="gd-button gd-game-hall-dialogs__open"
            [href]="offer.url"
            target="_blank"
            autofocus
            (click)="followed(offer.key)"
            >{{ offer.label }}</a
          >
        </p>
      </gd-dialog>
    }
    @for (items of deliveries.newItems(); track items.collectionName) {
      <gd-dialog
        name="new-items"
        [heading]="newItemsTitle(items.collectionName)"
        [open]="true"
        [modal]="false"
        (closed)="deliveries.closeNewItems(items.collectionName)"
      >
        <div class="gd-game-hall-dialogs__items">
          @for (item of items.packs.concat(items.cards); track $index) {
            <span class="gd-game-hall-dialogs__item">
              <img [src]="images.imageUrl(item.blueprintId)" alt="" />
              @if (item.count > 1) {
                <span class="gd-game-hall-dialogs__count">{{ item.count }}</span>
              }
            </span>
          }
        </div>
      </gd-dialog>
    }
    @if (deliveries.announcement(); as announcement) {
      <gd-dialog
        name="announcement"
        [heading]="announcementTitle(announcement.title)"
        [open]="true"
        [modal]="false"
        (closed)="deliveries.snoozeAnnouncement()"
      >
        <div class="gd-game-hall-dialogs__announcement">
          <gd-markup [nodes]="announcement.content" (hallLink)="deliveries.followAnnouncementLink()" />
        </div>
        <div class="gd-dialog__actions" gdDialogActions>
          <button type="button" class="gd-button" data-action="dismiss" (click)="deliveries.dismissAnnouncement()">
            {{ dismissWord }}
          </button>
          <button type="button" class="gd-button" data-action="snooze" (click)="deliveries.snoozeAnnouncement()">
            {{ remindWord }}
          </button>
        </div>
      </gd-dialog>
    }
    @if (deliveries.leagueNotifications()[0]; as move) {
      <gd-dialog name="rtmd" [heading]="rtmdTitle" [open]="true" (closed)="deliveries.closeLeagueNotification()">
        <div class="gd-game-hall-dialogs__rtmd">
          <p class="gd-game-hall-dialogs__rtmd-message">{{ move.message }}</p>
          <div class="gd-game-hall-dialogs__rtmd-sites">
            @if (move.old; as old) {
              <span class="gd-game-hall-dialogs__site" data-site="old">
                <img [src]="images.imageUrl(old.visual)" alt="" />
                @if (old.modifier; as modifier) {
                  <span class="gd-game-hall-dialogs__modifier"><img [src]="images.imageUrl(modifier)" alt="" /></span>
                }
              </span>
              <span class="gd-game-hall-dialogs__arrow" aria-hidden="true">&rarr;</span>
            }
            @if (move.next; as next) {
              <span class="gd-game-hall-dialogs__site" data-site="new">
                <img [src]="images.imageUrl(next.visual)" alt="" />
                @if (next.modifier; as modifier) {
                  <span class="gd-game-hall-dialogs__modifier"><img [src]="images.imageUrl(modifier)" alt="" /></span>
                }
              </span>
            }
          </div>
        </div>
      </gd-dialog>
    }
  `,
})
export class GameHallDialogsComponent {
  protected readonly dialogs = inject(HallDialogs);
  protected readonly queues = inject(HallQueuesService);
  protected readonly offers = inject(HallReadyOffers);
  protected readonly deliveries = inject(HallDeliveries);
  protected readonly images = inject(CardImageService);

  protected readonly readyCheckTitle = READY_CHECK_TITLE;
  protected readonly readyWord = READY_CHECK_READY;
  protected readonly okWord = READY_CHECK_OK;
  protected readonly dismissWord = DISMISS;
  protected readonly remindWord = REMIND_LATER;
  protected readonly rtmdTitle = RTMD_TITLE;
  protected readonly newItemsTitle = newItemsTitle;
  protected readonly announcementTitle = announcementTitle;

  protected readyCheckMarkup(queueName: string, seconds: number) {
    return pageMarkup(readyCheckText(queueName, seconds));
  }

  /** The prompt's link was followed: it goes once the browser has followed it. */
  protected followed(key: string): void {
    setTimeout(() => this.offers.dismiss(key), 0);
  }

  /** A link into the hall: its route. */
}
