#!/usr/bin/env node

/**
 * CLI Wrapper — single entry point for the full migration pipeline.
 *
 * Usage:
 *   npx tsx codemods/src/cli.ts --appName=myapp --target=frontend/app
 *   npx tsx codemods/src/cli.ts --appName=myapp --target=frontend/app --phases=0,1
 *   npx tsx codemods/src/cli.ts --appName=myapp --target=frontend/app --dry-run
 */

import * as fs from 'fs';
import * as path from 'path';
import { execFileSync } from 'child_process';
import {
  type PhaseResult,
  printPhaseHeader,
  printPhaseSummary,
  printGrandSummary,
  formatResultsJson,
} from './utils/reporter';

// ---------------------------------------------------------------------------
// Types
// ---------------------------------------------------------------------------

export interface CliOptions {
  target: string;
  appName: string;
  phases: string[];
  dryRun: boolean;
  extensions: string;
  modelsDir: string;
  schemasDir: string;
  baseOnlyClasses: string[];
  verbose: boolean;
  quiet: boolean;
  strict: boolean;
  json: boolean;
}

interface ConfigFile {
  appName?: string;
  target?: string;
  modelsDir?: string;
  schemasDir?: string;
  extensions?: string;
  baseOnlyClasses?: string[];
  phases?: string[];
}

interface PhaseDefinition {
  id: string;
  name: string;
  type: 'jscodeshift' | 'standalone';
  transformFile?: string;
  getTarget: (opts: CliOptions) => string;
  getRunnerOptions?: (opts: CliOptions) => Record<string, unknown>;
}

// ---------------------------------------------------------------------------
// Phase definitions (hardcoded order)
// ---------------------------------------------------------------------------

const PHASE_DEFINITIONS: PhaseDefinition[] = [
  {
    id: '0',
    name: 'Deprecation Cleanup',
    type: 'jscodeshift',
    transformFile: 'src/phase-0-deprecation-cleanup.ts',
    getTarget: (opts) => opts.target,
  },
  {
    id: '1',
    name: 'Import Migration',
    type: 'jscodeshift',
    transformFile: 'src/phase-1-import-migration.ts',
    getTarget: (opts) => opts.target,
    getRunnerOptions: (opts) => ({ appName: opts.appName }),
  },
  {
    id: '3a',
    name: 'Model to Schema',
    type: 'jscodeshift',
    transformFile: 'src/phase-3a-model-to-schema.ts',
    getTarget: (opts) => opts.modelsDir,
    getRunnerOptions: (opts) => ({
      appName: opts.appName,
      schemasDir: opts.schemasDir,
      dryRun: opts.dryRun,
      baseOnlyClasses: opts.baseOnlyClasses.join(','),
    }),
  },
  {
    id: '2a',
    name: 'Consumer Migration',
    type: 'jscodeshift',
    transformFile: 'src/phase-2a-consumer-migration.ts',
    getTarget: (opts) => opts.target,
    getRunnerOptions: (opts) => ({
      appName: opts.appName,
      ignorePattern: '**/models/**',
    }),
  },
  {
    id: '3b',
    name: 'Schema Index',
    type: 'standalone',
    getTarget: (opts) => opts.schemasDir,
  },
  {
    id: '4',
    name: 'Mirror to Official',
    type: 'jscodeshift',
    transformFile: 'src/phase-4-mirror-to-official.ts',
    getTarget: (opts) => opts.target,
  },
];

// Phase 4 (mirror→official) is opt-in — only needed if mirror packages were used
const DEFAULT_PHASES = ['0', '1', '3a', '2a', '3b'];

// ---------------------------------------------------------------------------
// Argument parsing
// ---------------------------------------------------------------------------

export function parseArgs(argv: string[]): Partial<CliOptions> {
  const result: Partial<CliOptions> = {};

  for (const arg of argv) {
    if (arg.startsWith('--target=')) {
      result.target = arg.slice('--target='.length);
    } else if (arg.startsWith('--appName=')) {
      result.appName = arg.slice('--appName='.length);
    } else if (arg.startsWith('--phases=')) {
      result.phases = arg.slice('--phases='.length).split(',');
    } else if (arg === '--dry-run' || arg === '--dryRun') {
      result.dryRun = true;
    } else if (arg.startsWith('--extensions=')) {
      result.extensions = arg.slice('--extensions='.length);
    } else if (arg.startsWith('--modelsDir=')) {
      result.modelsDir = arg.slice('--modelsDir='.length);
    } else if (arg.startsWith('--schemasDir=')) {
      result.schemasDir = arg.slice('--schemasDir='.length);
    } else if (arg.startsWith('--baseOnlyClasses=')) {
      result.baseOnlyClasses = arg.slice('--baseOnlyClasses='.length).split(',');
    } else if (arg === '--verbose') {
      result.verbose = true;
    } else if (arg === '--quiet') {
      result.quiet = true;
    } else if (arg === '--strict') {
      result.strict = true;
    } else if (arg === '--json') {
      result.json = true;
    }
  }

  return result;
}

// ---------------------------------------------------------------------------
// Config file loading
// ---------------------------------------------------------------------------

export function loadConfig(configPath: string): ConfigFile {
  try {
    const raw = fs.readFileSync(configPath, 'utf-8');
    try {
      return JSON.parse(raw) as ConfigFile;
    } catch {
      console.warn(`Warning: could not parse ${configPath} as JSON`);
      return {};
    }
  } catch (err: any) {
    if (err.code === 'ENOENT') return {};
    console.warn(`Warning: could not read ${configPath}: ${err.message}`);
    return {};
  }
}

// ---------------------------------------------------------------------------
// Merge config + CLI args (CLI wins)
// ---------------------------------------------------------------------------

export function mergeConfigAndArgs(
  config: ConfigFile,
  args: Partial<CliOptions>,
): CliOptions {
  const target = args.target ?? config.target ?? '';

  return {
    target,
    appName: args.appName ?? config.appName ?? '',
    phases: args.phases ?? config.phases ?? DEFAULT_PHASES,
    dryRun: args.dryRun ?? false,
    extensions: args.extensions ?? config.extensions ?? 'ts,gts',
    modelsDir:
      args.modelsDir ?? config.modelsDir ?? (target ? path.join(target, 'models') : ''),
    schemasDir:
      args.schemasDir ?? config.schemasDir ?? (target ? path.join(target, 'schemas') : ''),
    baseOnlyClasses:
      args.baseOnlyClasses ?? config.baseOnlyClasses ?? [],
    verbose: args.verbose ?? false,
    quiet: args.quiet ?? false,
    strict: args.strict ?? false,
    json: args.json ?? false,
  };
}

// ---------------------------------------------------------------------------
// Validation
// ---------------------------------------------------------------------------

export interface ValidationResult {
  errors: string[];
  warnings: string[];
}

export function validateOptions(opts: CliOptions): ValidationResult {
  const errors: string[] = [];
  const warnings: string[] = [];

  if (!opts.target) {
    errors.push('--target is required.');
  } else if (!fs.existsSync(opts.target)) {
    errors.push(`--target directory does not exist: ${opts.target}`);
  }

  const needsAppName = opts.phases.some((p) =>
    ['1', '2a', '3a'].includes(p),
  );
  if (needsAppName && !opts.appName) {
    errors.push(
      '--appName is required when running phases 1, 2a, or 3a.',
    );
  }

  if (opts.appName === 'app') {
    warnings.push(
      'Using default appName "app" — did you forget --appName?',
    );
  }

  // Order checks
  const phaseOrder = opts.phases;
  const idx3a = phaseOrder.indexOf('3a');
  const idx2a = phaseOrder.indexOf('2a');
  if (idx3a !== -1 && idx2a !== -1 && idx3a > idx2a) {
    warnings.push(
      'Phase 3a should run before 2a (models before consumers).',
    );
  }

  const idx3b = phaseOrder.indexOf('3b');
  if (idx3b !== -1 && idx3a === -1) {
    warnings.push('Phase 3b needs schemas from 3a — consider adding phase 3a.');
  }

  if (
    idx3b !== -1 &&
    opts.schemasDir &&
    !fs.existsSync(opts.schemasDir)
  ) {
    warnings.push(
      `schemasDir does not exist: ${opts.schemasDir} (phase 3b may fail).`,
    );
  }

  const knownPhaseIds = PHASE_DEFINITIONS.map(p => p.id);
  for (const phase of opts.phases) {
    if (!knownPhaseIds.includes(phase)) {
      warnings.push(`Unknown phase "${phase}" — will be skipped. Known phases: ${knownPhaseIds.join(', ')}`);
    }
  }

  if (opts.verbose && opts.quiet) {
    warnings.push('--verbose and --quiet are mutually exclusive; --quiet takes precedence.');
  }

  return { errors, warnings };
}

// ---------------------------------------------------------------------------
// Phase execution
// ---------------------------------------------------------------------------

async function runJscodeshiftPhase(
  phase: PhaseDefinition,
  opts: CliOptions,
  codemodRoot: string,
): Promise<PhaseResult> {
  // Dynamic require so we don't bundle Runner at parse-time
  // eslint-disable-next-line @typescript-eslint/no-var-requires
  const Runner = require('jscodeshift/src/Runner');

  const transformPath = path.resolve(codemodRoot, phase.transformFile!);
  const targetPath = path.resolve(phase.getTarget(opts));

  const runnerOpts: Record<string, unknown> = {
    parser: 'ts',
    extensions: opts.extensions,
    dry: opts.dryRun,
    runInBand: true,
    silent: opts.quiet || opts.json,
    verbose: opts.verbose ? 2 : 0,
    ...(phase.getRunnerOptions?.(opts) ?? {}),
  };

  const result = await Runner.run(transformPath, [targetPath], runnerOpts);

  return {
    phaseId: phase.id,
    phaseName: phase.name,
    ok: result?.ok ?? 0,
    nochange: result?.nochange ?? 0,
    skip: result?.skip ?? 0,
    error: result?.error ?? 0,
    timeElapsed: result?.timeElapsed ?? '0',
  };
}

function runStandalonePhase3b(
  opts: CliOptions,
  codemodRoot: string,
): PhaseResult {
  const startTime = process.hrtime();
  const scriptPath = path.resolve(
    codemodRoot,
    'src/phase-3b-schema-index.ts',
  );
  const schemasDir = path.resolve(opts.schemasDir);

  const args = ['tsx', scriptPath, `--schemasDir=${schemasDir}`];
  if (opts.dryRun) {
    args.push('--dryRun');
  }

  const stdio = opts.quiet || opts.json ? 'pipe' : 'inherit';
  let ok = 0;
  let error = 0;
  try {
    execFileSync('npx', args, { encoding: 'utf-8', stdio });
    ok = 1;
  } catch {
    error = 1;
  }

  const endTime = process.hrtime(startTime);
  const timeElapsed = (endTime[0] + endTime[1] / 1e9).toFixed(3);

  return {
    phaseId: '3b',
    phaseName: 'Schema Index',
    ok,
    nochange: 0,
    skip: 0,
    error,
    timeElapsed,
  };
}

// ---------------------------------------------------------------------------
// Main
// ---------------------------------------------------------------------------

async function main(): Promise<void> {
  const codemodRoot = path.resolve(__dirname, '..');
  const args = parseArgs(process.argv.slice(2));
  const configPath = path.resolve(process.cwd(), '.codemodrc.json');
  const config = loadConfig(configPath);
  const opts = mergeConfigAndArgs(config, args);

  const { errors, warnings } = validateOptions(opts);

  if (!opts.json) {
    for (const w of warnings) {
      console.warn(`Warning: ${w}`);
    }
  }

  if (errors.length > 0) {
    if (!opts.json) {
      for (const e of errors) {
        console.error(`Error: ${e}`);
      }
    } else {
      console.log(JSON.stringify({ errors }, null, 2));
    }
    process.exit(1);
  }

  // Filter to requested phases, preserving definition order
  const selectedPhases = PHASE_DEFINITIONS.filter((p) =>
    opts.phases.includes(p.id),
  );

  if (selectedPhases.length === 0) {
    console.error('No valid phases selected.');
    process.exit(1);
  }

  if (!opts.quiet && !opts.json) {
    console.log(
      `Running ${selectedPhases.length} phase(s): ${selectedPhases.map((p) => p.id).join(', ')}`,
    );
    if (opts.dryRun) {
      console.log('Dry-run mode: no files will be written.');
    }
  }

  const results: PhaseResult[] = [];

  for (const phase of selectedPhases) {
    if (!opts.quiet && !opts.json) {
      printPhaseHeader(phase.id, phase.name);
    }

    let result: PhaseResult;
    if (phase.type === 'standalone') {
      result = runStandalonePhase3b(opts, codemodRoot);
    } else {
      result = await runJscodeshiftPhase(phase, opts, codemodRoot);
    }

    if (!opts.quiet && !opts.json) {
      printPhaseSummary(result);
    }
    results.push(result);

    if (result.error > 0 && !opts.quiet && !opts.json) {
      console.warn(
        `Phase ${phase.id} had ${result.error} error(s). Continuing...`,
      );
    }
  }

  if (opts.json) {
    console.log(formatResultsJson(results));
  } else {
    printGrandSummary(results);
  }

  const totalErrors = results.reduce((n, r) => n + r.error, 0);
  if (opts.strict && totalErrors > 0) {
    process.exit(1);
  }
}

if (require.main === module) {
  main().catch((err) => {
    console.error('Fatal error:', err);
    process.exit(1);
  });
}
