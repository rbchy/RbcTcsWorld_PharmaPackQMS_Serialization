// Phase 3B — reusable, field-level validation used by every form in main.tsx.
// Kept deliberately dependency-free (no external form library) so the frontend
// stays a single self-contained bundle, matching the rest of this project.

export type FieldType = 'code' | 'name' | 'text' | 'decimal' | 'integer' | 'date' | 'select';
export type CapitalizeMode = 'words' | 'first' | 'upper';

export interface FieldRule {
  label: string;
  type: FieldType;
  required?: boolean;
  min?: number;
  max?: number;
  maxLength?: number;
  capitalize?: CapitalizeMode;
  /** Widen/override the built-in per-type character pattern (rarely needed). */
  pattern?: RegExp;
  patternMsg?: string;
}

export function isBlank(v: unknown): boolean {
  return v === null || v === undefined || String(v).trim() === '';
}

export function capitalizeWords(v: string): string {
  return v.replace(/(^|\s)\p{L}/gu, (ch) => ch.toUpperCase());
}
export function capitalizeFirst(v: string): string {
  if (!v) return v;
  return v.charAt(0).toUpperCase() + v.slice(1);
}
export function applyCapitalize(rule: FieldRule | undefined, v: string): string {
  if (!rule?.capitalize || !v) return v;
  if (rule.capitalize === 'words') return capitalizeWords(v);
  if (rule.capitalize === 'upper') return v.toUpperCase();
  return capitalizeFirst(v);
}

// CODE: identifiers such as product code / batch number / deviation number — letters,
// digits, dash, underscore, slash — no spaces, no other special characters.
const CODE_RE = /^[A-Za-z0-9][A-Za-z0-9_\-/]*$/;
// NAME: product/title-style free text — letters, digits, spaces and a small set of
// punctuation that legitimately appears in pharma names ( . , & ( ) ' - % / ).
const NAME_RE = /^[A-Za-z0-9 .,&()'\-%/]+$/;
// TEXT: longer free text (description / remarks / comments) — same safe set, plus : ;
const TEXT_RE = /^[A-Za-z0-9 .,&()'\-%/:;]+$/;

export function validateValue(rule: FieldRule, raw: unknown): string | null {
  const v = (raw ?? '').toString();
  if (rule.required && isBlank(v)) return `${rule.label} is required — this field cannot be blank.`;
  if (isBlank(v)) return null; // optional & empty is fine

  const pattern = rule.pattern;
  switch (rule.type) {
    case 'code':
      if (!(pattern ?? CODE_RE).test(v.trim())) {
        return rule.patternMsg ?? `${rule.label} may only contain letters, numbers, - _ / (no spaces or special characters).`;
      }
      break;
    case 'name':
      if (!(pattern ?? NAME_RE).test(v)) {
        return rule.patternMsg ?? `${rule.label} contains an invalid special character.`;
      }
      break;
    case 'text':
      if (!(pattern ?? TEXT_RE).test(v)) {
        return rule.patternMsg ?? `${rule.label} contains an invalid special character.`;
      }
      break;
    case 'decimal': {
      if (!/^-?\d+(\.\d+)?$/.test(v.trim())) return `${rule.label} must be a valid number (digits, with at most one decimal point).`;
      const n = Number(v);
      if (rule.min !== undefined && n < rule.min) return `${rule.label} must be ${rule.min} or greater.`;
      if (rule.max !== undefined && n > rule.max) return `${rule.label} must be ${rule.max} or less.`;
      break;
    }
    case 'integer': {
      if (!/^-?\d+$/.test(v.trim())) return `${rule.label} must be a whole number — no decimal point, no letters.`;
      const n = Number(v);
      if (rule.min !== undefined && n < rule.min) return `${rule.label} must be ${rule.min} or greater.`;
      if (rule.max !== undefined && n > rule.max) return `${rule.label} must be ${rule.max} or less.`;
      break;
    }
    case 'date':
      if (!/^\d{4}-\d{2}-\d{2}$/.test(v.trim()) || Number.isNaN(new Date(v).getTime())) {
        return `${rule.label} must be a valid date.`;
      }
      break;
    case 'select':
      break;
  }
  if (rule.maxLength && v.length > rule.maxLength) return `${rule.label} must be ${rule.maxLength} characters or fewer.`;
  return null;
}

export function validateAll(rules: Record<string, FieldRule>, values: Record<string, unknown>): Record<string, string> {
  const errors: Record<string, string> = {};
  for (const key of Object.keys(rules)) {
    const err = validateValue(rules[key], values[key]);
    if (err) errors[key] = err;
  }
  return errors;
}

/** Cross-field check: `end` (e.g. expiry) must not be before `start` (e.g. manufacturing). */
export function validateDateOrder(startLabel: string, endLabel: string, start?: string, end?: string): string | null {
  if (!start || !end) return null;
  if (new Date(end) < new Date(start)) return `${endLabel} cannot be before ${startLabel}.`;
  return null;
}

/** Creates a per-field onChange handler that auto-capitalizes, updates form state and re-validates live. */
export function fieldHandler<T extends Record<string, any>>(
  values: T,
  setValues: (v: T) => void,
  rules: Record<string, FieldRule>,
  errors: Record<string, string>,
  setErrors: (e: Record<string, string>) => void,
) {
  return (key: keyof T & string) => (raw: string) => {
    const rule = rules[key];
    const v = applyCapitalize(rule, raw);
    setValues({ ...values, [key]: v });
    const err = rule ? validateValue(rule, v) : null;
    const next = { ...errors };
    if (err) next[key] = err; else delete next[key];
    setErrors(next);
  };
}
