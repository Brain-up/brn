// Shim for AMD require() used by v1 addons in Embroider/Vite.
// loader.js defines `require` as a global; this re-exports it as an ES module.
export default (typeof globalThis.require === 'function' ? globalThis.require : function() {});
export const has = (typeof globalThis.require === 'function' && globalThis.require.has) ? globalThis.require.has : function() { return false; };
