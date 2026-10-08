#!/bin/bash
cd ../frontend/ && corepack enable && pnpm install && node ./node_modules/ember-cli/bin/ember build --environment=production