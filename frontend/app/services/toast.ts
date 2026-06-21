import Service from '@ember/service';
import { tracked } from '@glimmer/tracking';

export interface Toast {
  id: string;
  type: 'success' | 'error' | 'warning' | 'info';
  message: string;
  duration?: number;
}

export default class ToastService extends Service {
  @tracked toasts: Toast[] = [];

  show(type: Toast['type'], message: string, duration = 3000) {
    const id = crypto.randomUUID();
    const newToast: Toast = { id, type, message, duration };
    this.toasts = [...this.toasts, newToast];

    if (duration > 0) {
      setTimeout(() => {
        this.remove(id);
      }, duration);
    }
  }

  success(message: string, duration?: number) {
    this.show('success', message, duration);
  }

  error(message: string, duration?: number) {
    this.show('error', message, duration);
  }

  warning(message: string, duration?: number) {
    this.show('warning', message, duration);
  }

  info(message: string, duration?: number) {
    this.show('info', message, duration);
  }

  remove(id: string) {
    this.toasts = this.toasts.filter((t) => t.id !== id);
  }
}

declare module '@ember/service' {
  interface Registry {
    toast: ToastService;
  }
}
