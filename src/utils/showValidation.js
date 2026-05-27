export const SHOW_TYPES = ["Film", "Series", "Anime"];
export const SHOW_STATUSES = ["Watching", "Completed", "On Hold", "Dropped", "Plan to watch"];

const PROGRESS_PATTERN = /^\d+\/(\d+|\?)$/;
const RELEASE_DATE_PATTERN = /^(\d{4}|\d{4}-\d{2}-\d{2})$/;

export function validateImageFile(imageFile, { required }) {
  if (!imageFile) {
    return required ? "Please upload a poster image." : undefined;
  }

  if (!imageFile.type.startsWith("image/")) {
    return "File must be an image.";
  }

  if (imageFile.size > 3 * 1024 * 1024) {
    return "Image must be 3 MB or smaller.";
  }

  return undefined;
}

export function validateShowForm(form, { requireImage = false, imageFile = null } = {}) {
  const errors = {};
  const scoreValue = Number(form.score);

  if (form.title.trim().length < 2) {
    errors.title = "Title must be at least 2 characters.";
  }

  if (form.description.trim().length < 10) {
    errors.description = "Description must be at least 10 characters.";
  }

  if (!RELEASE_DATE_PATTERN.test(form.releaseDate.trim())) {
    errors.releaseDate = "Use YYYY or YYYY-MM-DD.";
  }

  if (Number.isNaN(scoreValue) || scoreValue < 0 || scoreValue > 10) {
    errors.score = "Score must be between 0 and 10.";
  }

  if (!PROGRESS_PATTERN.test(form.progress.trim())) {
    errors.progress = "Progress must look like 3/10 or 3/?.";
  }

  if (!SHOW_TYPES.includes(form.type)) {
    errors.type = "Please choose a valid type.";
  }

  if (!SHOW_STATUSES.includes(form.status)) {
    errors.status = "Please choose a valid status.";
  }

  const imageError = validateImageFile(imageFile, { required: requireImage });

  if (imageError) {
    errors.image = imageError;
  }

  return errors;
}

export function normalizeShowForm(form, image = form.image ?? "") {
  return {
    title: form.title.trim(),
    description: form.description.trim(),
    releaseDate: form.releaseDate.trim(),
    score: Number(form.score),
    progress: form.progress.trim(),
    type: form.type,
    status: form.status,
    image,
  };
}
