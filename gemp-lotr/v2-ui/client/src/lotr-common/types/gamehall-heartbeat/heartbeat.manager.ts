import axios from 'axios';

const HEARTBEAT_DEBOUNCE = 1000;

export class HeartbeatManager {
  private isStopped = false;

  constructor(private readonly channelNumber: number) {}

  start() {
    this.tick();
  }

  stop(): void {
    this.isStopped = true;
  }

  private tick(): void {
    setTimeout(async () => {
      await this.executeHeartbeat();
      if (!this.isStopped) {
        this.tick();
      }
    }, HEARTBEAT_DEBOUNCE);
  }

  private async executeHeartbeat(): Promise<void> {
    await axios.post(
      '/gemp-lotr-server/hall/update',
      {
        channelNumber: this.channelNumber,
        participantId: null,
      },
      {
        headers: {
          'Content-Type': 'application/x-www-form-urlencoded',
        },
        data: {
          channelNumber: this.channelNumber,
          participantId: null,
        },
      }
    );
  }
}
