import Controller from '@ember/controller';
import { service } from '@ember/service';
import type SessionService from '../../services/session';

export default class DashboardController extends Controller {
  @service declare session: SessionService;
}
