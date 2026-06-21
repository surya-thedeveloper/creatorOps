import EmberRouter from '@embroider/router';
import config from 'frontend/config/environment';

export default class Router extends EmberRouter {
  location = config.locationType;
  rootURL = config.rootURL;
}

Router.map(function () {
  this.route('login', { path: '/login' });
  this.route('register', { path: '/register' });

  this.route('setup', { path: '/setup' }, function () {
    this.route('organization', { path: '/organization' });
    this.route('brand', { path: '/brand' });
  });

  this.route('authenticated', { path: '/' }, function () {
    this.route('dashboard', { path: '/dashboard' });
    this.route('org', { path: '/:org_id' }, function () {
      this.route('brand', { path: '/:brand_id' }, function () {
        this.route('content', { path: '/content' }, function () {
          this.route('detail', { path: '/:content_id' });
        });
        this.route('calendar', { path: '/calendar' });
        this.route('team', { path: '/team' });
        this.route('analytics', { path: '/analytics' });
        this.route('settings', { path: '/settings' });
      });
    });
  });
});
