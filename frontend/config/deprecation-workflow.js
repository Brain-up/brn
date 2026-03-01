self.deprecationWorkflow = self.deprecationWorkflow || {};
self.deprecationWorkflow.config = {
    throwOnUnhandled: false,
    workflow: [
        { handler: "silence", matchId: "ember-polyfills.deprecate-assign" },
    ]
};
