import { useSyncExternalStore } from 'react';

function getSnapshot() {
  return window.innerWidth;
}

function subscribe(callback: () => void) {
  window.addEventListener('resize', callback);
  return () => {
    window.removeEventListener('resize', callback);
  };
}

export function useWindowDimensions() {
  const isOnline = useSyncExternalStore(subscribe, getSnapshot);
  return isOnline;
}
