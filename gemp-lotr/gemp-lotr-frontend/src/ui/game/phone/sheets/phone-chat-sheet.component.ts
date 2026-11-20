import { Component, computed, inject } from '@angular/core';
import { ChatRoom } from '../../../../core/chat/chat-room.service';
import { GameChatComponent } from '../../tabs/game-chat.component';

/**
 * The chat with the game's messages in the phone layout's sheet, under who is in the room.
 *
 * DOM contract: `.gd-phone-note` (who is in the room) and `gd-game-chat.gd-phone-chat`.
 */
@Component({
  selector: 'gd-phone-chat-sheet',
  styleUrl: './phone-chat-sheet.component.css',
  imports: [GameChatComponent],
  template: `
    <div class="gd-phone-note">In the room: {{ users() }}</div>
    <gd-game-chat class="gd-phone-chat" />
  `,
})
export class PhoneChatSheetComponent {
  private readonly chat = inject(ChatRoom);

  protected readonly users = computed(
    () =>
      this.chat
        .users()
        .map((user) => user.name)
        .join(', ') || '–',
  );
}
