export function startRouting(container) {
  const router = container.lookup('router:main');
  router.startRouting(true);
}
