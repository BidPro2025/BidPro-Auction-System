export function createCheckoutLoader({
  ready = () => globalThis.window?.Razorpay,
  document = () => globalThis.document,
  timeoutMs = 15000,
} = {}) {
  let active;
  return () => {
    if (ready()) return Promise.resolve();
    if (active) return active;
    const attempt = new Promise((resolve, reject) => {
      const page = document();
      const script = page.createElement('script');
      let settled = false;
      const timer = setTimeout(
        () => complete(new Error('Secure checkout took too long to load. Please try again.')),
        timeoutMs,
      );
      function complete(error) {
        if (settled) return;
        settled = true;
        clearTimeout(timer);
        script.onload = null;
        script.onerror = null;
        if (error) {
          script.remove();
          reject(error);
        } else resolve();
      }
      script.src = 'https://checkout.razorpay.com/v1/checkout.js';
      script.async = true;
      script.onload = () =>
        complete(ready() ? null : new Error('Secure checkout did not load. Please try again.'));
      script.onerror = () =>
        complete(new Error('Cannot load secure checkout. Check your connection and try again.'));
      try {
        page.head.appendChild(script);
      } catch (error) {
        complete(error);
      }
    });
    active = attempt.catch((error) => {
      active = null;
      throw error;
    });
    return active;
  };
}
