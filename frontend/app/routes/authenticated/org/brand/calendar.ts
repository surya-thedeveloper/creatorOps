import Route from '@ember/routing/route';
import { service } from '@ember/service';
import type ApiService from '../../../../services/api';
import RSVP from 'rsvp';

export default class CalendarRoute extends Route {
  @service declare api: ApiService;

  model() {
    const now = new Date();
    const startOfMonth = new Date(
      now.getFullYear(),
      now.getMonth(),
      1,
    ).toISOString();
    const endOfMonth = new Date(
      now.getFullYear(),
      now.getMonth() + 1,
      0,
      23,
      59,
      59,
    ).toISOString();

    return RSVP.hash({
      // Load content with due dates in current month for the calendar grid
      scheduled: this.api
        .get<any>(`calendar?startDate=${startOfMonth}&endDate=${endOfMonth}`)
        .then((res: any) => (Array.isArray(res) ? res : (res?.content ?? [])))
        .catch(() => []),
      // Sidebar: upcoming content with due dates
      upcoming: this.api
        .get<any>('calendar/upcoming')
        .then((res: any) => (Array.isArray(res) ? res : (res?.content ?? [])))
        .catch(() => []),
      // Overdue items for highlighting
      overdue: this.api
        .get<any>('calendar/overdue')
        .then((res: any) => (Array.isArray(res) ? res : (res?.content ?? [])))
        .catch(() => []),
    });
  }
}
