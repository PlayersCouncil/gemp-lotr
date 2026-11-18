/** A clock's seconds as the game page writes them: "m:ss", or "h:mm:ss" from an hour (legacy parseTime). */
export function clockText(seconds: number): string {
  const sign = seconds < 0 ? '-' : '';
  const value = Math.abs(seconds);
  const two = (n: number) => String(n).padStart(2, '0');
  const hours = Math.floor(value / 3600);
  const minutes = Math.floor(value / 60) % 60;
  return hours > 0 ? `${sign}${hours}:${two(minutes)}:${two(value % 60)}` : `${sign}${minutes}:${two(value % 60)}`;
}
