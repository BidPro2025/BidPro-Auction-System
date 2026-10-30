import { useCallback, useEffect, useRef, useState } from 'react';
import { createSingleFlight } from '../lib/singleFlight';

export default function useMutation() {
  const [state, setState] = useState({ pending: false, error: '', success: '' });
  const flight = useRef(createSingleFlight());
  const mounted = useRef(true);
  useEffect(() => {
    mounted.current = true;
    return () => {
      mounted.current = false;
    };
  }, []);
  const run = useCallback(
    (operation, success = 'Changes saved.') =>
      flight.current(async () => {
        if (!mounted.current) return { ok: false, cancelled: true };
        if (mounted.current) setState({ pending: true, error: '', success: '' });
        try {
          const result = await operation();
          if (!mounted.current) return { ok: false, cancelled: true };
          if (mounted.current) setState({ pending: false, error: '', success });
          return { ok: true, data: result };
        } catch (error) {
          if (mounted.current)
            setState({
              pending: false,
              error: error.message || 'The change could not be saved. Please try again.',
              success: '',
            });
          return { ok: false };
        }
      }),
    [],
  );
  return { ...state, run };
}
