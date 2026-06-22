import { helper } from '@ember/component/helper';

/**
 * UX-10: format-date helper
 * Formats a Date object or ISO string into a human-readable date string.
 * Usage in templates: {{format-date someDate}} or {{format-date someDate format="short"}}
 */
export default helper(function formatDate(
  [value]: [Date | string | null | undefined],
  { format = 'medium' }: { format?: string },
) {
  if (!value) return '—';

  const date = value instanceof Date ? value : new Date(value);
  if (isNaN(date.getTime())) return String(value);

  const options: Intl.DateTimeFormatOptions =
    format === 'short'
      ? { month: 'short', day: 'numeric' }
      : format === 'long'
        ? { weekday: 'short', year: 'numeric', month: 'long', day: 'numeric' }
        : { year: 'numeric', month: 'short', day: 'numeric' };

  return new Intl.DateTimeFormat('en-US', options).format(date);
});
