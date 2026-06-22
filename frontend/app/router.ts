import EmberRouter from '@embroider/router';
import config from 'frontend/config/environment';

export default class Router extends EmberRouter {
  location = config.locationType;
  rootURL = config.rootURL;
}

Router.map(function () {
  // ROUTE-05: Removed redundant path options where path === route name
  this.route('login');
  this.route('register');

  this.route('setup', function () {
    this.route('organization');
    this.route('brand');
  });

  this.route('authenticated', { path: '/' }, function () {
    this.route('dashboard');
    this.route('org', { path: '/:org_id' }, function () {
      this.route('brand', { path: '/:brand_id' }, function () {
        this.route('content', function () {
          this.route('detail', { path: '/:content_id' });
        });
        this.route('calendar');
        this.route('team');
        this.route('analytics');
        this.route('settings');
      });
    });
  });

  // ROUTE-01: 404 catch-all route for unmatched URLs
  this.route('not-found', { path: '/*path' });
});
