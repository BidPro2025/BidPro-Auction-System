import { useCallback, useEffect, useState } from 'react';
import { request } from '../api/client';

export default function useResource(path) {
  const [revision, setRevision] = useState(0);
  const [state, setState] = useState({ path, data: null, loading: Boolean(path), error: '' });
  const reload = useCallback(() => setRevision((value) => value + 1), []);
  useEffect(() => {
    if (!path) {
      setState({ path, data: null, loading: false, error: '' });
      return;
    }
    const controller = new AbortController();
    setState((previous) => ({
      path,
      data: previous.path === path ? previous.data : null,
      loading: true,
      error: '',
    }));
    request(path, { signal: controller.signal })
      .then((data) => {
        if (!controller.signal.aborted) setState({ path, data, loading: false, error: '' });
      })
      .catch((error) => {
        if (!controller.signal.aborted)
          setState((previous) => ({ ...previous, loading: false, error: error.message }));
      });
    return () => controller.abort();
  }, [path, revision]);
  return {
    ...(state.path === path ? state : { data: null, loading: Boolean(path), error: '' }),
    reload,
  };
}
