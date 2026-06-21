import { helper } from '@ember/component/helper';

export default helper(function firstChar([str]: [string | null | undefined]) {
  if (!str) return '';
  return str.charAt(0).toUpperCase();
});
