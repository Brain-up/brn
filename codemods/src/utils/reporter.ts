/**
 * Summary reporting utility for the CLI wrapper.
 *
 * Provides formatted output for per-phase results and a grand summary table.
 * Supports human-readable (default) and JSON output modes.
 */

export interface PhaseResult {
  phaseId: string;
  phaseName: string;
  ok: number;
  nochange: number;
  skip: number;
  error: number;
  timeElapsed: string;
}

export function printPhaseHeader(phaseId: string, phaseName: string): void {
  console.log('');
  console.log(`--- Phase ${phaseId}: ${phaseName} ---`);
}

export function printPhaseSummary(result: PhaseResult): void {
  console.log(`  Changed:   ${result.ok} files`);
  console.log(`  Unchanged: ${result.nochange} files`);
  if (result.skip > 0) {
    console.log(`  Skipped:   ${result.skip} files`);
  }
  console.log(`  Errors:    ${result.error}`);
  console.log(`  Time:      ${result.timeElapsed}s`);
}

export function printGrandSummary(results: PhaseResult[]): void {
  console.log('');
  console.log('=== Migration Summary ===');

  const header = 'Phase                          Changed  Unchanged  Errors     Time';
  const separator = '\u2500'.repeat(header.length);

  console.log(header);
  console.log(separator);

  let totalOk = 0;
  let totalNochange = 0;
  let totalError = 0;

  for (const r of results) {
    const label = `${r.phaseId}: ${r.phaseName}`;
    const padded = label.padEnd(31);
    const ok = String(r.ok).padStart(7);
    const nochange = String(r.nochange).padStart(10);
    const error = String(r.error).padStart(8);
    const time = r.timeElapsed.padStart(7) + 's';
    console.log(`${padded}${ok}${nochange}${error}  ${time}`);

    totalOk += r.ok;
    totalNochange += r.nochange;
    totalError += r.error;
  }

  console.log(separator);

  const totalLabel = 'TOTAL'.padEnd(31);
  const totalOkStr = String(totalOk).padStart(7);
  const totalNochangeStr = String(totalNochange).padStart(10);
  const totalErrorStr = String(totalError).padStart(8);
  const totalTime = results.reduce((sum, r) => sum + parseFloat(r.timeElapsed), 0).toFixed(3);
  const totalTimeStr = totalTime.padStart(7) + 's';
  console.log(`${totalLabel}${totalOkStr}${totalNochangeStr}${totalErrorStr}  ${totalTimeStr}`);
}

export function formatResultsJson(results: PhaseResult[]): string {
  let totalOk = 0;
  let totalNochange = 0;
  let totalSkip = 0;
  let totalError = 0;
  for (const r of results) {
    totalOk += r.ok;
    totalNochange += r.nochange;
    totalSkip += r.skip;
    totalError += r.error;
  }

  return JSON.stringify(
    {
      phases: results.map((r) => ({
        id: r.phaseId,
        name: r.phaseName,
        changed: r.ok,
        unchanged: r.nochange,
        skipped: r.skip,
        errors: r.error,
        timeElapsed: r.timeElapsed,
      })),
      totals: {
        changed: totalOk,
        unchanged: totalNochange,
        skipped: totalSkip,
        errors: totalError,
      },
    },
    null,
    2,
  );
}
