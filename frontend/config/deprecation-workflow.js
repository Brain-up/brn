self.deprecationWorkflow = self.deprecationWorkflow || {};
self.deprecationWorkflow.config = {
    throwOnUnhandled: true,
    workflow: [
        { handler: "silence", matchId: "ember-polyfills.deprecate-assign" },
    ]
};
