import { Injectable } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class CurrentOrganizationService {
  private readonly ORG_ID_KEY = 'project_management_system_current_org_id';
  private readonly ORG_NAME_KEY = 'project_management_system_current_org_name';

  setCurrentOrganization(id: string, name: string): void {
    localStorage.setItem(this.ORG_ID_KEY, id);
    localStorage.setItem(this.ORG_NAME_KEY, name);
  }

  getCurrentOrganizationId(): string | null {
    return localStorage.getItem(this.ORG_ID_KEY);
  }

  getCurrentOrganizationName(): string | null {
    return localStorage.getItem(this.ORG_NAME_KEY);
  }

  clear(): void {
    localStorage.removeItem(this.ORG_ID_KEY);
    localStorage.removeItem(this.ORG_NAME_KEY);
  }
}
