import Controller from '@ember/controller';
import { service } from '@ember/service';
import type SessionService from '../services/session';

export default class AuthenticatedController extends Controller {
  @service declare session: SessionService;

  get userInitial(): string {
    const email = this.session.userEmail || 'User';
    return email.charAt(0).toUpperCase();
  }
}
