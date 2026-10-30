// Share the active operation immediately, before React can commit a disabled button.
// This prevents duplicate submissions from same-tick clicks or Enter key events.
export function createSingleFlight() {
  let active = null;
  return (operation) => {
    if (active) return active;
    const pending = Promise.resolve()
      .then(operation)
      .finally(() => {
        if (active === pending) active = null;
      });
    active = pending;
    return pending;
  };
}
