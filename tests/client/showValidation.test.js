import assert from "node:assert/strict";
import {
  normalizeShowForm,
  validateImageFile,
  validateShowForm,
} from "../../src/utils/showValidation.js";

const validForm = {
  title: "Arcane",
  description: "Animated fantasy drama with strong characters.",
  releaseDate: "2021",
  score: "9",
  progress: "3/9",
  type: "Series",
  status: "Watching",
  image: "data:image/png;base64,abc",
};

async function runTest(name, callback) {
  try {
    await callback();
    console.log(`PASS ${name}`);
  } catch (error) {
    console.error(`FAIL ${name}`);
    throw error;
  }
}

await runTest("validateShowForm accepts a valid form", async () => {
  assert.deepEqual(validateShowForm(validForm), {});
});

await runTest("validateShowForm reports every important invalid field", async () => {
  const errors = validateShowForm({
    ...validForm,
    title: "A",
    description: "short",
    releaseDate: "21",
    score: "12",
    progress: "soon",
    type: "Book",
    status: "Unknown",
  });

  assert.equal(errors.title, "Title must be at least 2 characters.");
  assert.equal(errors.description, "Description must be at least 10 characters.");
  assert.equal(errors.releaseDate, "Use YYYY or YYYY-MM-DD.");
  assert.equal(errors.score, "Score must be between 0 and 10.");
  assert.equal(errors.progress, "Progress must look like 3/10 or 3/?.");
  assert.equal(errors.type, "Please choose a valid type.");
  assert.equal(errors.status, "Please choose a valid status.");
});

await runTest("validateImageFile enforces required poster uploads", async () => {
  assert.equal(
    validateImageFile(null, { required: true }),
    "Please upload a poster image.",
  );
});

await runTest("validateImageFile rejects wrong file types and oversize files", async () => {
  assert.equal(
    validateImageFile({ type: "text/plain", size: 10 }, { required: true }),
    "File must be an image.",
  );
  assert.equal(
    validateImageFile({ type: "image/png", size: 4 * 1024 * 1024 }, { required: true }),
    "Image must be 3 MB or smaller.",
  );
});

await runTest("normalizeShowForm trims and converts values", async () => {
  const normalized = normalizeShowForm({
    ...validForm,
    title: "  Arcane  ",
    description: "  Animated fantasy drama with strong characters.  ",
    releaseDate: " 2021-11-06 ",
    score: "8",
    progress: " 4/9 ",
  });

  assert.deepEqual(normalized, {
    title: "Arcane",
    description: "Animated fantasy drama with strong characters.",
    releaseDate: "2021-11-06",
    score: 8,
    progress: "4/9",
    type: "Series",
    status: "Watching",
    image: "data:image/png;base64,abc",
  });
});
