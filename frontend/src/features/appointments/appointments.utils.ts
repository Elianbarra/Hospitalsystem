import type { Appointment } from "./appointments.types";

const MORNING_BLOCKS: [number, number][] = [
  [9, 0],
  [9, 30],
  [10, 0],
  [10, 30],
];
const AFTERNOON_BLOCKS: [number, number][] = [
  [14, 0],
  [14, 30],
  [15, 0],
  [15, 30],
];
const ALL_BLOCKS = [...MORNING_BLOCKS, ...AFTERNOON_BLOCKS];

export function findAvailableSlots(appts: Appointment[]): string[] {
  const now = new Date();
  const sixMonthsLater = new Date(now);
  sixMonthsLater.setMonth(sixMonthsLater.getMonth() + 6);

  const occupiedSet = new Set(
    appts
      .filter((a) => a.status !== "CANCELLED")
      .map((a) => {
        const d = new Date(a.scheduledAt);
        d.setSeconds(0);
        d.setMilliseconds(0);
        return d.getTime();
      })
  );

  const slots: string[] = [];
  const cursor = new Date(now);
  cursor.setSeconds(0);
  cursor.setMilliseconds(0);

  while (cursor <= sixMonthsLater) {
    const day = cursor.getDay();
    if (day !== 0 && day !== 6) {
      for (const [h, m] of ALL_BLOCKS) {
        const slot = new Date(cursor);
        slot.setHours(h, m, 0, 0);
        if (slot > now && slot <= sixMonthsLater && !occupiedSet.has(slot.getTime())) {
          slots.push(slot.toISOString());
        }
      }
    }
    cursor.setDate(cursor.getDate() + 1);
  }
  return slots;
}
