import { helper } from '@ember/component/helper';

export default helper(function eq([a, b]: [any, any]) {
  return a === b;
});
