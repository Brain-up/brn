import * as fs from 'fs';
import * as path from 'path';
import * as os from 'os';
import { parseArgs, loadConfig, mergeConfigAndArgs, validateOptions } from '../src/cli';
import {
  type PhaseResult,
  printPhaseHeader,
  printPhaseSummary,
  printGrandSummary,
  formatResultsJson,
} from '../src/utils/reporter';

// ---------------------------------------------------------------------------
// parseArgs
// ---------------------------------------------------------------------------

describe('parseArgs', () => {
  it('should parse --target', () => {
    const result = parseArgs(['--target=frontend/app']);
    expect(result.target).toBe('frontend/app');
  });

  it('should parse --appName', () => {
    const result = parseArgs(['--appName=myapp']);
    expect(result.appName).toBe('myapp');
  });

  it('should parse --phases as comma-separated list', () => {
    const result = parseArgs(['--phases=0,1,3a']);
    expect(result.phases).toEqual(['0', '1', '3a']);
  });

  it('should parse --dry-run flag', () => {
    const result = parseArgs(['--dry-run']);
    expect(result.dryRun).toBe(true);
  });

  it('should parse --dryRun flag', () => {
    const result = parseArgs(['--dryRun']);
    expect(result.dryRun).toBe(true);
  });

  it('should parse --extensions', () => {
    const result = parseArgs(['--extensions=ts,js']);
    expect(result.extensions).toBe('ts,js');
  });

  it('should parse --modelsDir', () => {
    const result = parseArgs(['--modelsDir=app/models']);
    expect(result.modelsDir).toBe('app/models');
  });

  it('should parse --schemasDir', () => {
    const result = parseArgs(['--schemasDir=app/schemas']);
    expect(result.schemasDir).toBe('app/schemas');
  });

  it('should parse --baseOnlyClasses as comma-separated', () => {
    const result = parseArgs(['--baseOnlyClasses=Foo,Bar']);
    expect(result.baseOnlyClasses).toEqual(['Foo', 'Bar']);
  });

  it('should parse --verbose flag', () => {
    const result = parseArgs(['--verbose']);
    expect(result.verbose).toBe(true);
  });

  it('should parse --quiet flag', () => {
    const result = parseArgs(['--quiet']);
    expect(result.quiet).toBe(true);
  });

  it('should parse --strict flag', () => {
    const result = parseArgs(['--strict']);
    expect(result.strict).toBe(true);
  });

  it('should parse --json flag', () => {
    const result = parseArgs(['--json']);
    expect(result.json).toBe(true);
  });

  it('should parse multiple flags together', () => {
    const result = parseArgs([
      '--target=frontend/app',
      '--appName=myapp',
      '--phases=0,1',
      '--dry-run',
      '--verbose',
    ]);
    expect(result.target).toBe('frontend/app');
    expect(result.appName).toBe('myapp');
    expect(result.phases).toEqual(['0', '1']);
    expect(result.dryRun).toBe(true);
    expect(result.verbose).toBe(true);
  });

  it('should return empty object for no args', () => {
    const result = parseArgs([]);
    expect(result.target).toBeUndefined();
    expect(result.appName).toBeUndefined();
    expect(result.phases).toBeUndefined();
    expect(result.dryRun).toBeUndefined();
    expect(result.verbose).toBeUndefined();
    expect(result.quiet).toBeUndefined();
    expect(result.strict).toBeUndefined();
    expect(result.json).toBeUndefined();
  });

  it('should ignore unknown flags', () => {
    const result = parseArgs(['--unknown=value', '--target=app']);
    expect(result.target).toBe('app');
    expect((result as Record<string, unknown>)['unknown']).toBeUndefined();
  });
});

// ---------------------------------------------------------------------------
// loadConfig
// ---------------------------------------------------------------------------

describe('loadConfig', () => {
  let tempDir: string;

  beforeEach(() => {
    tempDir = fs.mkdtempSync(path.join(os.tmpdir(), 'cli-test-'));
  });

  afterEach(() => {
    fs.rmSync(tempDir, { recursive: true, force: true });
  });

  it('should load a valid config file', () => {
    const configPath = path.join(tempDir, '.codemodrc.json');
    fs.writeFileSync(
      configPath,
      JSON.stringify({ appName: 'myapp', target: 'frontend/app' }),
      'utf-8',
    );
    const config = loadConfig(configPath);
    expect(config.appName).toBe('myapp');
    expect(config.target).toBe('frontend/app');
  });

  it('should return empty object for missing file', () => {
    const config = loadConfig(path.join(tempDir, 'nonexistent.json'));
    expect(config).toEqual({});
  });

  it('should return empty object and warn for invalid JSON', () => {
    const configPath = path.join(tempDir, '.codemodrc.json');
    fs.writeFileSync(configPath, '{bad json', 'utf-8');
    const warnSpy = jest.spyOn(console, 'warn').mockImplementation();
    const config = loadConfig(configPath);
    expect(config).toEqual({});
    expect(warnSpy).toHaveBeenCalledWith(
      expect.stringContaining('could not parse'),
    );
    expect(warnSpy).toHaveBeenCalledWith(
      expect.stringContaining('as JSON'),
    );
    warnSpy.mockRestore();
  });

  it('should warn with actual error message for permission errors', () => {
    const configPath = path.join(tempDir, '.codemodrc.json');
    fs.writeFileSync(configPath, '{"appName":"test"}', 'utf-8');
    fs.chmodSync(configPath, 0o000);
    const warnSpy = jest.spyOn(console, 'warn').mockImplementation();
    const config = loadConfig(configPath);
    // If running as root, chmod 000 may not prevent reading
    if (process.getuid?.() === 0) {
      // Skip assertion when running as root
      expect(config).toBeDefined();
    } else {
      expect(config).toEqual({});
      expect(warnSpy).toHaveBeenCalledWith(
        expect.stringContaining('could not read'),
      );
    }
    warnSpy.mockRestore();
    // Restore permissions so cleanup works
    fs.chmodSync(configPath, 0o644);
  });

  it('should load all config fields', () => {
    const configPath = path.join(tempDir, '.codemodrc.json');
    fs.writeFileSync(
      configPath,
      JSON.stringify({
        appName: 'myapp',
        target: 'frontend/app',
        modelsDir: 'frontend/app/models',
        schemasDir: 'frontend/app/schemas',
        extensions: 'ts,gts',
        baseOnlyClasses: ['CompletionDependent'],
      }),
      'utf-8',
    );
    const config = loadConfig(configPath);
    expect(config.modelsDir).toBe('frontend/app/models');
    expect(config.schemasDir).toBe('frontend/app/schemas');
    expect(config.extensions).toBe('ts,gts');
    expect(config.baseOnlyClasses).toEqual(['CompletionDependent']);
  });
});

// ---------------------------------------------------------------------------
// mergeConfigAndArgs
// ---------------------------------------------------------------------------

describe('mergeConfigAndArgs', () => {
  it('should use CLI args over config', () => {
    const config = { appName: 'from-config', target: 'config-target' };
    const args = { appName: 'from-cli', target: 'cli-target' };
    const merged = mergeConfigAndArgs(config, args);
    expect(merged.appName).toBe('from-cli');
    expect(merged.target).toBe('cli-target');
  });

  it('should fall back to config when CLI arg is missing', () => {
    const config = { appName: 'from-config', target: 'config-target' };
    const args = {};
    const merged = mergeConfigAndArgs(config, args);
    expect(merged.appName).toBe('from-config');
    expect(merged.target).toBe('config-target');
  });

  it('should use defaults when both are missing', () => {
    const merged = mergeConfigAndArgs({}, {});
    expect(merged.target).toBe('');
    expect(merged.appName).toBe('');
    expect(merged.dryRun).toBe(false);
    expect(merged.extensions).toBe('ts,gts');
    expect(merged.baseOnlyClasses).toEqual([]);
    expect(merged.verbose).toBe(false);
    expect(merged.quiet).toBe(false);
    expect(merged.strict).toBe(false);
    expect(merged.json).toBe(false);
  });

  it('should default phases to 0,1,3a,2a,3b', () => {
    const merged = mergeConfigAndArgs({}, {});
    expect(merged.phases).toEqual(['0', '1', '3a', '2a', '3b']);
  });

  it('should derive modelsDir from target', () => {
    const merged = mergeConfigAndArgs({}, { target: 'frontend/app' });
    expect(merged.modelsDir).toBe(path.join('frontend/app', 'models'));
  });

  it('should derive schemasDir from target', () => {
    const merged = mergeConfigAndArgs({}, { target: 'frontend/app' });
    expect(merged.schemasDir).toBe(path.join('frontend/app', 'schemas'));
  });

  it('should prefer explicit modelsDir over derived', () => {
    const merged = mergeConfigAndArgs(
      {},
      { target: 'frontend/app', modelsDir: 'custom/models' },
    );
    expect(merged.modelsDir).toBe('custom/models');
  });

  it('should never set dryRun from config (CLI-only)', () => {
    const merged = mergeConfigAndArgs({}, {});
    expect(merged.dryRun).toBe(false);
  });

  it('should pass through output flags', () => {
    const merged = mergeConfigAndArgs({}, { verbose: true, json: true, strict: true });
    expect(merged.verbose).toBe(true);
    expect(merged.json).toBe(true);
    expect(merged.strict).toBe(true);
  });

  it('should use phases from config when CLI arg is missing', () => {
    const config = { phases: ['0', '1'] };
    const merged = mergeConfigAndArgs(config, {});
    expect(merged.phases).toEqual(['0', '1']);
  });

  it('should prefer CLI phases over config phases', () => {
    const config = { phases: ['0', '1'] };
    const args = { phases: ['3a', '2a'] };
    const merged = mergeConfigAndArgs(config, args);
    expect(merged.phases).toEqual(['3a', '2a']);
  });

  it('should fall back to DEFAULT_PHASES when neither CLI nor config has phases', () => {
    const merged = mergeConfigAndArgs({}, {});
    expect(merged.phases).toEqual(['0', '1', '3a', '2a', '3b']);
  });
});

// ---------------------------------------------------------------------------
// validateOptions
// ---------------------------------------------------------------------------

describe('validateOptions', () => {
  let tempDir: string;

  beforeEach(() => {
    tempDir = fs.mkdtempSync(path.join(os.tmpdir(), 'cli-validate-'));
  });

  afterEach(() => {
    fs.rmSync(tempDir, { recursive: true, force: true });
  });

  function makeOpts(overrides: Partial<ReturnType<typeof mergeConfigAndArgs>>): ReturnType<typeof mergeConfigAndArgs> {
    return {
      target: tempDir,
      appName: 'myapp',
      phases: ['0', '1', '3a', '2a', '3b'],
      dryRun: false,
      extensions: 'ts,gts',
      modelsDir: path.join(tempDir, 'models'),
      schemasDir: path.join(tempDir, 'schemas'),
      baseOnlyClasses: [],
      verbose: false,
      quiet: false,
      strict: false,
      json: false,
      ...overrides,
    };
  }

  it('should error when --target is missing', () => {
    const { errors } = validateOptions(makeOpts({ target: '' }));
    expect(errors).toContain('--target is required.');
  });

  it('should error when --target directory does not exist', () => {
    const { errors } = validateOptions(makeOpts({ target: '/nonexistent/path' }));
    expect(errors.some((e) => e.includes('does not exist'))).toBe(true);
  });

  it('should error when --appName is missing for phases needing it', () => {
    const { errors } = validateOptions(
      makeOpts({ appName: '', phases: ['1'] }),
    );
    expect(errors.some((e) => e.includes('--appName is required'))).toBe(true);
  });

  it('should not error when --appName is missing for phase 0 only', () => {
    const { errors } = validateOptions(
      makeOpts({ appName: '', phases: ['0'] }),
    );
    expect(errors).toHaveLength(0);
  });

  it('should warn when appName is "app"', () => {
    const { warnings } = validateOptions(makeOpts({ appName: 'app' }));
    expect(warnings.some((w) => w.includes('default appName'))).toBe(true);
  });

  it('should warn when 3a comes after 2a', () => {
    const { warnings } = validateOptions(
      makeOpts({ phases: ['0', '2a', '3a'] }),
    );
    expect(
      warnings.some((w) => w.includes('3a should run before 2a')),
    ).toBe(true);
  });

  it('should warn when 3b is selected without 3a', () => {
    const { warnings } = validateOptions(
      makeOpts({ phases: ['0', '1', '3b'] }),
    );
    expect(warnings.some((w) => w.includes('3b needs schemas from 3a'))).toBe(
      true,
    );
  });

  it('should warn when schemasDir does not exist for 3b', () => {
    const { warnings } = validateOptions(
      makeOpts({ phases: ['3a', '3b'], schemasDir: '/nonexistent/schemas' }),
    );
    expect(
      warnings.some((w) => w.includes('schemasDir does not exist')),
    ).toBe(true);
  });

  it('should pass with valid options', () => {
    const { errors } = validateOptions(makeOpts({}));
    expect(errors).toHaveLength(0);
  });

  it('should warn when --verbose and --quiet are both set', () => {
    const { warnings } = validateOptions(
      makeOpts({ verbose: true, quiet: true }),
    );
    expect(warnings.some((w) => w.includes('mutually exclusive'))).toBe(true);
  });

  it('should warn about unknown phase IDs', () => {
    const { warnings } = validateOptions(
      makeOpts({ phases: ['0', '1', '9', 'abc'] }),
    );
    expect(warnings.some((w) => w.includes('Unknown phase "9"'))).toBe(true);
    expect(warnings.some((w) => w.includes('Unknown phase "abc"'))).toBe(true);
    expect(warnings.some((w) => w.includes('Known phases:'))).toBe(true);
  });

  it('should not warn for valid phase IDs', () => {
    const { warnings } = validateOptions(
      makeOpts({ phases: ['0', '1', '3a', '2a', '3b', '4'] }),
    );
    expect(warnings.some((w) => w.includes('Unknown phase'))).toBe(false);
  });
});

// ---------------------------------------------------------------------------
// Phase filtering (--phases)
// ---------------------------------------------------------------------------

describe('phase filtering', () => {
  it('should select only specified phases via parseArgs', () => {
    const args = parseArgs(['--phases=0,1']);
    expect(args.phases).toEqual(['0', '1']);
  });

  it('should handle single phase', () => {
    const args = parseArgs(['--phases=3b']);
    expect(args.phases).toEqual(['3b']);
  });

  it('should handle all phases including opt-in phase 4', () => {
    const args = parseArgs(['--phases=0,1,3a,2a,3b,4']);
    expect(args.phases).toEqual(['0', '1', '3a', '2a', '3b', '4']);
  });
});

// ---------------------------------------------------------------------------
// Reporter
// ---------------------------------------------------------------------------

describe('reporter', () => {
  let logSpy: jest.SpyInstance;

  beforeEach(() => {
    logSpy = jest.spyOn(console, 'log').mockImplementation();
  });

  afterEach(() => {
    logSpy.mockRestore();
  });

  it('printPhaseHeader should print phase id and name', () => {
    printPhaseHeader('0', 'Deprecation Cleanup');
    expect(logSpy).toHaveBeenCalledWith('');
    expect(logSpy).toHaveBeenCalledWith('--- Phase 0: Deprecation Cleanup ---');
  });

  it('printPhaseSummary should print counts with time suffix', () => {
    const result: PhaseResult = {
      phaseId: '0',
      phaseName: 'Deprecation Cleanup',
      ok: 42,
      nochange: 18,
      skip: 0,
      error: 0,
      timeElapsed: '2.300',
    };
    printPhaseSummary(result);
    expect(logSpy).toHaveBeenCalledWith('  Changed:   42 files');
    expect(logSpy).toHaveBeenCalledWith('  Unchanged: 18 files');
    expect(logSpy).toHaveBeenCalledWith('  Errors:    0');
    expect(logSpy).toHaveBeenCalledWith('  Time:      2.300s');
  });

  it('printPhaseSummary should show skipped if > 0', () => {
    const result: PhaseResult = {
      phaseId: '0',
      phaseName: 'Test',
      ok: 1,
      nochange: 0,
      skip: 3,
      error: 0,
      timeElapsed: '1.000',
    };
    printPhaseSummary(result);
    expect(logSpy).toHaveBeenCalledWith('  Skipped:   3 files');
  });

  it('printGrandSummary should print table with totals', () => {
    const results: PhaseResult[] = [
      {
        phaseId: '0',
        phaseName: 'Deprecation Cleanup',
        ok: 42,
        nochange: 18,
        skip: 0,
        error: 0,
        timeElapsed: '2.300',
      },
      {
        phaseId: '1',
        phaseName: 'Import Migration',
        ok: 30,
        nochange: 8,
        skip: 0,
        error: 0,
        timeElapsed: '1.500',
      },
    ];
    printGrandSummary(results);

    expect(logSpy).toHaveBeenCalledWith('=== Migration Summary ===');

    const totalCall = logSpy.mock.calls.find(
      (args: string[]) => typeof args[0] === 'string' && args[0].includes('TOTAL'),
    );
    expect(totalCall).toBeDefined();
    expect(totalCall![0]).toContain('72');
    expect(totalCall![0]).toContain('26');
  });

  it('printGrandSummary should include Time column in header and rows', () => {
    const results: PhaseResult[] = [
      {
        phaseId: '0',
        phaseName: 'Deprecation Cleanup',
        ok: 10,
        nochange: 5,
        skip: 0,
        error: 0,
        timeElapsed: '2.300',
      },
      {
        phaseId: '1',
        phaseName: 'Import Migration',
        ok: 20,
        nochange: 3,
        skip: 0,
        error: 1,
        timeElapsed: '1.500',
      },
    ];
    printGrandSummary(results);

    // Header should contain Time
    const headerCall = logSpy.mock.calls.find(
      (args: string[]) => typeof args[0] === 'string' && args[0].includes('Phase') && args[0].includes('Time'),
    );
    expect(headerCall).toBeDefined();

    // Phase rows should contain time values
    const phase0Call = logSpy.mock.calls.find(
      (args: string[]) => typeof args[0] === 'string' && args[0].includes('Deprecation Cleanup') && args[0].includes('2.300s'),
    );
    expect(phase0Call).toBeDefined();

    const phase1Call = logSpy.mock.calls.find(
      (args: string[]) => typeof args[0] === 'string' && args[0].includes('Import Migration') && args[0].includes('1.500s'),
    );
    expect(phase1Call).toBeDefined();

    // Total row should contain summed time
    const totalCall = logSpy.mock.calls.find(
      (args: string[]) => typeof args[0] === 'string' && args[0].includes('TOTAL'),
    );
    expect(totalCall).toBeDefined();
    expect(totalCall![0]).toContain('3.800s');
  });

  it('formatResultsJson should produce valid JSON with phase data', () => {
    const results: PhaseResult[] = [
      {
        phaseId: '0',
        phaseName: 'Deprecation Cleanup',
        ok: 42,
        nochange: 18,
        skip: 0,
        error: 1,
        timeElapsed: '2.300',
      },
    ];
    const json = formatResultsJson(results);
    const parsed = JSON.parse(json);
    expect(parsed.phases).toHaveLength(1);
    expect(parsed.phases[0].id).toBe('0');
    expect(parsed.phases[0].changed).toBe(42);
    expect(parsed.phases[0].errors).toBe(1);
    expect(parsed.totals.changed).toBe(42);
    expect(parsed.totals.errors).toBe(1);
  });

  it('formatResultsJson should sum totalSkip across phases', () => {
    const results: PhaseResult[] = [
      {
        phaseId: '0',
        phaseName: 'Phase A',
        ok: 1,
        nochange: 2,
        skip: 3,
        error: 0,
        timeElapsed: '1.000',
      },
      {
        phaseId: '1',
        phaseName: 'Phase B',
        ok: 4,
        nochange: 5,
        skip: 6,
        error: 0,
        timeElapsed: '2.000',
      },
    ];
    const json = formatResultsJson(results);
    const parsed = JSON.parse(json);
    expect(parsed.totals.skipped).toBe(9);
    expect(parsed.totals.changed).toBe(5);
    expect(parsed.totals.unchanged).toBe(7);
  });
});

// ---------------------------------------------------------------------------
// CLI integration
// ---------------------------------------------------------------------------

describe('CLI integration', () => {
  let tempDir: string;

  beforeEach(() => {
    tempDir = fs.mkdtempSync(path.join(os.tmpdir(), 'cli-integration-'));
  });

  afterEach(() => {
    fs.rmSync(tempDir, { recursive: true, force: true });
  });

  it('should warn about unknown phase IDs', () => {
    const args = parseArgs(['--target=' + tempDir, '--appName=myapp', '--phases=0,1,9,abc']);
    const opts = mergeConfigAndArgs({}, args);
    const { warnings } = validateOptions(opts);
    expect(warnings.some((w) => w.includes('Unknown phase "9"'))).toBe(true);
    expect(warnings.some((w) => w.includes('Unknown phase "abc"'))).toBe(true);
  });

  it('should support phases in config file', () => {
    const configPath = path.join(tempDir, '.codemodrc.json');
    fs.writeFileSync(
      configPath,
      JSON.stringify({ appName: 'myapp', target: tempDir, phases: ['0', '3a'] }),
      'utf-8',
    );
    const config = loadConfig(configPath);
    const args = parseArgs(['--target=' + tempDir]);
    const opts = mergeConfigAndArgs(config, args);
    expect(opts.phases).toEqual(['0', '3a']);
    expect(opts.appName).toBe('myapp');
  });

  it('should let CLI phases override config phases', () => {
    const configPath = path.join(tempDir, '.codemodrc.json');
    fs.writeFileSync(
      configPath,
      JSON.stringify({ phases: ['0', '3a'] }),
      'utf-8',
    );
    const config = loadConfig(configPath);
    const args = parseArgs(['--target=' + tempDir, '--appName=myapp', '--phases=1,2a']);
    const opts = mergeConfigAndArgs(config, args);
    expect(opts.phases).toEqual(['1', '2a']);
  });

  it('should handle loadConfig with permission error (EACCES)', () => {
    const configPath = path.join(tempDir, '.codemodrc.json');
    fs.writeFileSync(configPath, '{"appName":"test"}', 'utf-8');
    fs.chmodSync(configPath, 0o000);
    const warnSpy = jest.spyOn(console, 'warn').mockImplementation();
    const config = loadConfig(configPath);
    // If running as root, chmod 000 may not prevent reading
    if (process.getuid?.() === 0) {
      expect(config).toBeDefined();
    } else {
      expect(config).toEqual({});
      expect(warnSpy).toHaveBeenCalledWith(
        expect.stringContaining('could not read'),
      );
    }
    warnSpy.mockRestore();
    // Restore permissions so cleanup works
    fs.chmodSync(configPath, 0o644);
  });
});
