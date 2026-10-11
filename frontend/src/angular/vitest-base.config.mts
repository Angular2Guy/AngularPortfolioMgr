import { defineConfig } from "vitest/config";

export default defineConfig({
  test: {
    // 38 legacy *.spec.ts files contain only commented-out suites (no active
    // tests). Keep the run green until those suites are revived or removed.
    passWithNoTests: true,
  },
});
