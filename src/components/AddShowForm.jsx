import { useEffect, useState } from "react";
import "./AddShowForm.css";
import {
  normalizeShowForm,
  SHOW_STATUSES,
  SHOW_TYPES,
  validateShowForm,
} from "../utils/showValidation";

const initialForm = {
  title: "",
  description: "",
  releaseDate: "",
  score: "",
  progress: "",
  type: "Film",
  status: "Watching",
};

function fileToDataUrl(file) {
  return new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.onload = () => resolve(String(reader.result));
    reader.onerror = () => reject(new Error("Unable to read image file."));
    reader.readAsDataURL(file);
  });
}

export function AddShowForm({ onClose, onSubmit }) {
  const [form, setForm] = useState(initialForm);
  const [imageFile, setImageFile] = useState(null);
  const [imagePreview, setImagePreview] = useState("");
  const [errors, setErrors] = useState({});
  const [isSaving, setIsSaving] = useState(false);

  useEffect(() => {
    return () => {
      if (imagePreview.startsWith("blob:")) {
        URL.revokeObjectURL(imagePreview);
      }
    };
  }, [imagePreview]);

  const handleChange = (field, value) => {
    setForm((prev) => ({ ...prev, [field]: value }));
  };

  const handleImageChange = (event) => {
    const file = event.target.files?.[0] ?? null;
    setImageFile(file);
    setErrors((prev) => ({ ...prev, image: undefined }));

    if (!file || !file.type.startsWith("image/")) {
      setImagePreview("");
      return;
    }

    const previewUrl = URL.createObjectURL(file);
    setImagePreview(previewUrl);
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    const foundErrors = validateShowForm(form, {
      requireImage: true,
      imageFile,
    });
    setErrors(foundErrors);

    if (Object.keys(foundErrors).length > 0 || !imageFile) {
      return;
    }

    setIsSaving(true);
    try {
      const image = await fileToDataUrl(imageFile);
      try {
        await onSubmit(normalizeShowForm(form, image));
      } catch (error) {
        setErrors((prev) => ({
          ...prev,
          image: error instanceof Error ? error.message : "Could not save this show.",
        }));
      }
    } catch {
      setErrors((prev) => ({ ...prev, image: "Could not process this image file." }));
    } finally {
      setIsSaving(false);
    }
  };

  return (
    <div className="add-show-overlay" role="dialog" aria-modal="true" aria-label="Add show">
      <form className="add-show-form" onSubmit={handleSubmit}>
        <h3>Add to list</h3>

        <label>
          Title
          <input
            type="text"
            value={form.title}
            onChange={(e) => handleChange("title", e.target.value)}
            placeholder="Show or movie title"
          />
          {errors.title ? <span className="field-error">{errors.title}</span> : null}
        </label>

        <label>
          Description
          <textarea
            value={form.description}
            onChange={(e) => handleChange("description", e.target.value)}
            placeholder="Brief plot or personal note"
            rows={4}
          />
          {errors.description ? <span className="field-error">{errors.description}</span> : null}
        </label>

        <label>
          Release date
          <input
            type="text"
            value={form.releaseDate}
            onChange={(e) => handleChange("releaseDate", e.target.value)}
            placeholder="YYYY or YYYY-MM-DD"
          />
          {errors.releaseDate ? <span className="field-error">{errors.releaseDate}</span> : null}
        </label>

        <div className="form-grid">
          <label>
            Score (0-10)
            <input
              type="number"
              min="0"
              max="10"
              step="1"
              value={form.score}
              onChange={(e) => handleChange("score", e.target.value)}
            />
            {errors.score ? <span className="field-error">{errors.score}</span> : null}
          </label>

          <label>
            Progress
            <input
              type="text"
              value={form.progress}
              onChange={(e) => handleChange("progress", e.target.value)}
              placeholder="Example: 3/10"
            />
            {errors.progress ? <span className="field-error">{errors.progress}</span> : null}
          </label>
        </div>

        <div className="form-grid">
          <label>
            Type
            <select value={form.type} onChange={(e) => handleChange("type", e.target.value)}>
              {SHOW_TYPES.map((type) => (
                <option key={type} value={type}>
                  {type}
                </option>
              ))}
            </select>
            {errors.type ? <span className="field-error">{errors.type}</span> : null}
          </label>

          <label>
            Status
            <select value={form.status} onChange={(e) => handleChange("status", e.target.value)}>
              {SHOW_STATUSES.map((status) => (
                <option key={status} value={status}>
                  {status}
                </option>
              ))}
            </select>
            {errors.status ? <span className="field-error">{errors.status}</span> : null}
          </label>
        </div>

        <label>
          Poster image
          <input type="file" accept="image/*" onChange={handleImageChange} />
          {errors.image ? <span className="field-error">{errors.image}</span> : null}
        </label>

        {imagePreview ? (
          <div className="preview-box">
            <img src={imagePreview} alt="Poster preview" />
          </div>
        ) : null}

        <div className="form-actions">
          <button type="button" className="secondary" onClick={onClose}>
            Cancel
          </button>
          <button type="submit" className="primary" disabled={isSaving}>
            {isSaving ? "Saving..." : "Add entry"}
          </button>
        </div>
      </form>
    </div>
  );
}
