import path from "node:path";
import { test, expect } from "@playwright/test";

test.describe("Watchlist core features", () => {
  test("Feature 1: add show and verify it appears in table", async ({ page }) => {
    const testTitle = `Series E2E ${Date.now()}`;

    await page.goto("/watchlist");
    await page.getByRole("button", { name: "Add to list" }).click();

    await page.getByLabel("Title").fill(testTitle);
    await page.getByLabel("Description").fill("Automated end-to-end test for adding a show.");
    await page.getByLabel("Release date").fill("2024");
    await page.getByLabel("Score (0-10)").fill("8");
    await page.getByLabel("Progress").fill("1/8");
    await page.getByLabel("Type").selectOption("Series");
    await page.getByLabel("Status").selectOption("Watching");

    const posterPath = path.resolve("public", "logo.png");
    await page.getByLabel("Poster image").setInputFiles(posterPath);

    const addDialog = page.getByRole("dialog", { name: "Add show" });
    await expect(addDialog).toBeVisible();

    const addEntryButton = addDialog.getByRole("button", { name: "Add entry" });
    await addEntryButton.scrollIntoViewIfNeeded();
    await addEntryButton.click();

    await expect(addDialog).toBeHidden();

    const rowByTitle = page.locator("tbody tr", { hasText: testTitle });
    const nextButton = page.getByRole("button", { name: "Next" });

    while ((await rowByTitle.count()) === 0 && await nextButton.isEnabled()) {
      await nextButton.click();
    }

    await expect(rowByTitle.first()).toBeVisible();
  });

  test("Feature 2: delete show from details page and verify it is gone", async ({ page }) => {
    const targetTitle = "American Psycho";

    await page.goto("/watchlist");

    const targetRow = page.locator("tbody tr", { hasText: targetTitle }).first();
    await expect(targetRow).toBeVisible();
    await targetRow.click();

    await expect(page).toHaveURL(/\/watchlist\/.+/);

    page.once("dialog", (dialog) => dialog.accept());
    await page.getByRole("button", { name: "Delete" }).click();

    await expect(page).toHaveURL("/watchlist");
    await expect(page.locator("tbody tr", { hasText: targetTitle })).toHaveCount(0);
  });

  test("Feature 3: filter by Series and hide non-series rows", async ({ page }) => {
    await page.goto("/watchlist");

    await page.locator(".filter-type button", { hasText: "Series" }).click();

    await expect(page.locator(".filter-type button.active")).toHaveText("Series");

    const rows = page.locator("tbody tr:not(.empty-row)");
    const rowCount = await rows.count();
    expect(rowCount).toBeGreaterThan(0);

    for (let index = 0; index < rowCount; index += 1) {
      await expect(rows.nth(index).locator("td").nth(3)).toHaveText("Series");
    }

    await expect(page.locator("tbody tr td", { hasText: "Film" })).toHaveCount(0);
    await expect(page.locator("tbody tr td", { hasText: "Anime" })).toHaveCount(0);
  });
});
