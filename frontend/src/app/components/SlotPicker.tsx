interface SlotPickerProps {
  slots: string[];
  selectedSlot: string | null;
  onSelect: (iso: string) => void;
}

export function SlotPicker({ slots, selectedSlot, onSelect }: SlotPickerProps) {
  const displayed = slots.slice(0, 20);
  const byDate: Record<string, string[]> = {};

  for (const iso of displayed) {
    const key = new Date(iso).toLocaleDateString("es-ES", {
      weekday: "long",
      year: "numeric",
      month: "long",
      day: "numeric",
    });
    if (!byDate[key]) byDate[key] = [];
    byDate[key].push(iso);
  }

  return (
    <div className="border border-gray-200 rounded-xl overflow-hidden">
      <div className="max-h-64 overflow-y-auto">
        {Object.entries(byDate).map(([dateLabel, dateSlots]) => (
          <div key={dateLabel}>
            <div className="bg-gray-50 px-4 py-2 text-xs font-bold text-gray-500 uppercase tracking-wider sticky top-0 capitalize">
              {dateLabel}
            </div>
            <div className="flex flex-wrap gap-2 px-4 py-3">
              {dateSlots.map((iso) => {
                const label = new Date(iso).toLocaleTimeString("es-ES", {
                  hour: "2-digit",
                  minute: "2-digit",
                });
                return (
                  <button
                    key={iso}
                    type="button"
                    onClick={() => onSelect(iso)}
                    className={`px-3 py-1.5 rounded-lg text-sm font-semibold border-2 transition-all ${
                      selectedSlot === iso
                        ? "bg-blue-700 text-white border-blue-700"
                        : "border-gray-200 text-gray-600 hover:border-blue-400 hover:text-blue-700"
                    }`}
                  >
                    {label}
                  </button>
                );
              })}
            </div>
          </div>
        ))}
      </div>
      {slots.length > 20 && (
        <div className="bg-gray-50 px-4 py-2 text-xs text-gray-400 text-center border-t border-gray-100">
          Mostrando los primeros 20 horarios disponibles
        </div>
      )}
    </div>
  );
}
