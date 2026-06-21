declare module '@ember-data/store' {
  import Store from '@ember-data/store';
  export default Store;
}

declare module '@ember-data/model' {
  import Model from '@ember-data/model';
  export default Model;
  export function attr<T = any>(options?: any): any;
  export function belongsTo<T = any>(options?: any): any;
}

declare module '@ember-data/serializer/rest' {
  import RESTSerializer from '@ember-data/serializer/rest';
  export default RESTSerializer;
}
