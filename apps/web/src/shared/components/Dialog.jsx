import { useLayoutEffect, useRef } from 'react';
import { createPortal } from 'react-dom';

const focusableSelector =
  'button:not(:disabled), [href], input:not(:disabled), select:not(:disabled), textarea:not(:disabled), [tabindex]:not([tabindex="-1"])';

export default function Dialog({
  labelledBy,
  describedBy,
  busy = false,
  onClose,
  restoreFocusRef,
  children,
}) {
  const dialog = useRef(null);
  const current = useRef({ busy, onClose });
  useLayoutEffect(() => {
    current.current = { busy, onClose };
  }, [busy, onClose]);
  useLayoutEffect(() => {
    const previousFocus = restoreFocusRef?.current || document.activeElement;
    const previousOverflow = document.body.style.overflow;
    // A portal keeps fixed dialogs outside scrollable tables and filtered ancestors.
    const background = [...document.body.children].filter((element) => element !== dialog.current);
    const previousInert = background.map((element) => [element, element.inert]);
    previousInert.forEach(([element]) => {
      element.inert = true;
    });
    document.body.style.overflow = 'hidden';
    const controls = () =>
      [...dialog.current.querySelectorAll(focusableSelector)].filter(
        (element) => element.tabIndex >= 0 && element.getClientRects().length,
      );
    const focusInitial = () => (controls()[0] || dialog.current).focus();
    focusInitial();
    const keydown = (event) => {
      if (event.key === 'Escape') {
        event.preventDefault();
        event.stopPropagation();
        if (!current.current.busy) current.current.onClose();
      }
      if (event.key === 'Tab') {
        const available = controls();
        const first = available[0];
        const last = available[available.length - 1];
        if (!first) {
          event.preventDefault();
          dialog.current.focus();
        } else if (
          event.shiftKey &&
          (document.activeElement === first || document.activeElement === dialog.current)
        ) {
          event.preventDefault();
          last.focus();
        } else if (!event.shiftKey && document.activeElement === last) {
          event.preventDefault();
          first.focus();
        }
      }
    };
    const retainFocus = (event) => {
      if (!dialog.current.contains(event.target)) focusInitial();
    };
    document.addEventListener('keydown', keydown, true);
    document.addEventListener('focusin', retainFocus);
    return () => {
      document.removeEventListener('keydown', keydown, true);
      document.removeEventListener('focusin', retainFocus);
      previousInert.forEach(([element, value]) => {
        element.inert = value;
      });
      document.body.style.overflow = previousOverflow;
      // Wait until React has removed the portal and enabled/replaced its trigger.
      queueMicrotask(() => {
        if (previousFocus?.isConnected && !previousFocus.disabled) previousFocus.focus();
        else document.getElementById('main-content')?.focus({ preventScroll: true });
      });
    };
  }, [restoreFocusRef]);
  return createPortal(
    <div
      ref={dialog}
      className="modal d-block"
      style={{ background: '#0008' }}
      role="dialog"
      aria-modal="true"
      aria-labelledby={labelledBy}
      aria-describedby={describedBy}
      aria-busy={busy}
      tabIndex={-1}
    >
      <div className="modal-dialog modal-dialog-centered">
        <div className="modal-content">{children}</div>
      </div>
    </div>,
    document.body,
  );
}
