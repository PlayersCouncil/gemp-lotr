import { create } from 'zustand';
import { devtools } from 'zustand/middleware';
import { immer } from 'zustand/middleware/immer';
import { HeartbeatManager } from '../types/gamehall-heartbeat/heartbeat.manager';

interface CurrentUserState {
  userId: string | null;
  channelNumber: number | null;
}

interface CurrentUserActions {
  setChannelNumber(channelNumber: number): void;
  setUserId(userId: string): void;
}

type CurrentUserStore = CurrentUserState & CurrentUserActions;

let heartbeat: HeartbeatManager | null = null;

export const useCurrentUserStore = create<CurrentUserStore>()(
  devtools(
    immer<CurrentUserStore>((set, get) => ({
      userId: null,
      channelNumber: null,
      setUserId: (userId: string) => {
        set((draft) => {
          draft.userId = userId;
        });
      },
      setChannelNumber: (channelNumber: number) => {
        if (get().channelNumber === null && get().channelNumber !== channelNumber) {
            heartbeat?.stop();
            heartbeat = new HeartbeatManager(channelNumber);
            heartbeat.start();
        }
        set((draft) => {
          draft.channelNumber = channelNumber;
        });
      },
    }))
  )
);
