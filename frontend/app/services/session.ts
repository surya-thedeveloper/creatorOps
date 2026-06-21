import Service from '@ember/service';
import { tracked } from '@glimmer/tracking';
import { service } from '@ember/service';
import type RouterService from '@ember/routing/router-service';

export default class SessionService extends Service {
  @service declare router: RouterService;

  @tracked token: string | null = localStorage.getItem('creatorops_jwt_token');
  @tracked refreshToken: string | null = localStorage.getItem('creatorops_refresh_token');
  @tracked userEmail: string | null = localStorage.getItem('creatorops_user_email');
  @tracked userRole: string | null = localStorage.getItem('creatorops_user_role');
  @tracked userId: string | null = localStorage.getItem('creatorops_user_id');
  @tracked orgId: string | null = localStorage.getItem('creatorops_org_id');
  @tracked brandId: string | null = localStorage.getItem('creatorops_brand_id');
  @tracked orgName: string | null = localStorage.getItem('creatorops_org_name');

  get isAuthenticated(): boolean {
    return !!this.token;
  }

  get isAdmin(): boolean {
    return this.userRole === 'ADMIN';
  }

  get isManager(): boolean {
    return this.userRole === 'MANAGER' || this.isAdmin;
  }

  get isContributor(): boolean {
    return this.userRole === 'CONTRIBUTOR';
  }

  saveSession(data: {
    token: string;
    refreshToken: string;
    email: string;
    role: string;
    id: number;
    organizationId?: number;
    organizationName?: string;
  }) {
    this.token = data.token;
    this.refreshToken = data.refreshToken;
    this.userEmail = data.email;
    this.userRole = data.role;
    this.userId = String(data.id);
    
    localStorage.setItem('creatorops_jwt_token', data.token);
    localStorage.setItem('creatorops_refresh_token', data.refreshToken);
    localStorage.setItem('creatorops_user_email', data.email);
    localStorage.setItem('creatorops_user_role', data.role);
    localStorage.setItem('creatorops_user_id', String(data.id));

    if (data.organizationId) {
      this.orgId = String(data.organizationId);
      localStorage.setItem('creatorops_org_id', String(data.organizationId));
    }
    if (data.organizationName) {
      this.orgName = data.organizationName;
      localStorage.setItem('creatorops_org_name', data.organizationName);
    }
  }

  selectBrand(brandId: string) {
    this.brandId = brandId;
    localStorage.setItem('creatorops_brand_id', brandId);
  }

  selectOrg(orgId: string, orgName?: string) {
    this.orgId = orgId;
    localStorage.setItem('creatorops_org_id', orgId);
    if (orgName) {
      this.orgName = orgName;
      localStorage.setItem('creatorops_org_name', orgName);
    }
  }

  clearSession() {
    this.token = null;
    this.refreshToken = null;
    this.userEmail = null;
    this.userRole = null;
    this.userId = null;
    this.orgId = null;
    this.brandId = null;
    this.orgName = null;

    localStorage.removeItem('creatorops_jwt_token');
    localStorage.removeItem('creatorops_refresh_token');
    localStorage.removeItem('creatorops_user_email');
    localStorage.removeItem('creatorops_user_role');
    localStorage.removeItem('creatorops_user_id');
    localStorage.removeItem('creatorops_org_id');
    localStorage.removeItem('creatorops_brand_id');
    localStorage.removeItem('creatorops_org_name');
  }

  logout() {
    this.clearSession();
    this.router.transitionTo('login');
  }
}

declare module '@ember/service' {
  interface Registry {
    session: SessionService;
  }
}
