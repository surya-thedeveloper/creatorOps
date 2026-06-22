import { helper } from '@ember/component/helper';

export default helper(function pick([path, callback]: [string, any]) {
  return function (event: Event) {
    const parts = path.split('.');
    let val: any = event;
    for (const part of parts) {
      if (val !== undefined && val !== null) {
        val = val[part];
      }
    }
    if (callback && typeof callback.update === 'function') {
      callback.update(val);
    } else if (typeof callback === 'function') {
      callback(val);
    }
  };
});
