import { helper } from '@ember/component/helper';

export default helper(function pick([path, callback]: [string, any]) {
  return function (event: Event) {
    const parts = path.split('.');
    let val: any = event;
    for (const part of parts) {
      if (val !== undefined && val !== null) {
        // @ts-ignore – dynamic property access
        val = (val as any)[part];
      }
    }
    if (typeof callback === 'function') {
      callback(val);
    } else if (callback && typeof (callback as any).update === 'function') {
      (callback as any).update(val);
    }
  };
});
