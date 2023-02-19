self.deprecationWorkflow = self.deprecationWorkflow || {};
self.deprecationWorkflow.config = {
    workflow: [
        { handler: "silence", matchId: "ember.component.reopen" },
        { handler: "silence", matchId: "ember.built-in-components.import" },
        { handler: "silence", matchId: "ember-metal.get-with-default" },
        { handler: "silence", matchId: "manager-capabilities.modifiers-3-13" },
        { handler: "silence", matchId: "ember-cli-mirage-config-routes-only-export" },
        { handler: "silence", matchId: "deprecated-run-loop-and-computed-dot-access" },
        { handler: "silence", matchId: "implicit-injections" },
        { handler: "silence", matchId: "ember-global" },
        { handler: "silence", matchId: "routing.transition-methods" },
        { handler: "silence", matchId: "computed-property.override" },
        { handler: "silence", matchId: "argument-less-helper-paren-less-invocation" }
    ]
};