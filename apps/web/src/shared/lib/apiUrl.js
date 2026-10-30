export function assetUrl(path, apiBase, origin) {
  if (!path || !apiBase || /^(?:https?:)?\/\//i.test(path)) return path;
  try {
    return new URL(path, new URL(apiBase, origin)).href;
  } catch {
    return path;
  }
}
