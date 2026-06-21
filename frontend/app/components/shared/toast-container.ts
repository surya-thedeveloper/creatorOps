import Component from '@glimmer/component';
import { service } from '@ember/service';
import type ToastService from '../../services/toast';
import { precompileTemplate } from '@ember/template-compilation';
import { setComponentTemplate } from '@ember/component';

class ToastContainerComponent extends Component {
  @service declare toast: ToastService;
}

export default setComponentTemplate(
  precompileTemplate(`
<div class="fixed bottom-4 right-4 z-50 flex flex-col gap-2 pointer-events-none max-w-sm w-full">
  {{#each this.toast.toasts as |toast|}}
    <div class="flex items-center p-4 rounded-lg shadow-lg border pointer-events-auto transition-all duration-300 transform translate-y-0
      {{if (eq toast.type 'success') 'bg-green-50 border-green-200 text-green-800'}}
      {{if (eq toast.type 'error') 'bg-red-50 border-red-200 text-red-800'}}
      {{if (eq toast.type 'warning') 'bg-yellow-50 border-yellow-200 text-yellow-800'}}
      {{if (eq toast.type 'info') 'bg-blue-50 border-blue-200 text-blue-800'}}"
      role="alert"
    >
      <div class="flex-grow text-sm font-medium mr-2">
        {{toast.message}}
      </div>
      <button
        type="button"
        class="ml-auto inline-flex items-center justify-center h-6 w-6 rounded-md p-1 focus:outline-none focus:ring-2 focus:ring-offset-2
          {{if (eq toast.type 'success') 'text-green-500 hover:bg-green-100 focus:ring-green-500'}}
          {{if (eq toast.type 'error') 'text-red-500 hover:bg-red-100 focus:ring-red-500'}}
          {{if (eq toast.type 'warning') 'text-yellow-500 hover:bg-yellow-100 focus:ring-yellow-500'}}
          {{if (eq toast.type 'info') 'text-blue-500 hover:bg-blue-100 focus:ring-blue-500'}}"
        {{on "click" (fn this.toast.remove toast.id)}}
      >
        <span class="sr-only">Close</span>
        <svg class="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/>
        </svg>
      </button>
    </div>
  {{/each}}
</div>
`),
  ToastContainerComponent
);
