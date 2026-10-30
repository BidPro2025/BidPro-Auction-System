import { useEffect } from 'react';
import { useLocation } from 'react-router-dom';

export default function RouteAccessibility() {
  const { pathname } = useLocation();
  useEffect(() => {
    document.title = 'BidPro';
    let observer;
    const update = () => {
      const main = document.getElementById('main-content');
      const heading = main?.querySelector('h1');
      if (!heading) return false;
      document.title = `${heading.textContent.trim()} · BidPro`;
      main.focus({ preventScroll: true });
      window.scrollTo(0, 0);
      observer?.disconnect();
      return true;
    };
    // Lazy routes and authenticated session checks may render their heading later.
    if (update()) return;
    observer = new MutationObserver(update);
    observer.observe(document.body, { childList: true, subtree: true });
    return () => observer.disconnect();
  }, [pathname]);
  return null;
}
