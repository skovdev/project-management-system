import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatCardModule } from '@angular/material/card';
import { OrganizationService } from '../../../services/organization.service';
import { CurrentOrganizationService } from '../../../services/current-organization.service';
import { OrganizationDto } from '../../../models/organization.model';
import { OrganizationFormComponent } from '../organization-form/organization-form.component';

@Component({
  selector: 'app-organization-list',
  standalone: true,
  imports: [
    CommonModule, RouterLink,
    MatTableModule, MatPaginatorModule, MatButtonModule,
    MatIconModule, MatProgressSpinnerModule, MatCardModule
  ],
  templateUrl: './organization-list.component.html',
  styleUrl: './organization-list.component.css'
})
export class OrganizationListComponent implements OnInit {
  displayedColumns = ['name', 'description', 'actions'];
  organizations: OrganizationDto[] = [];
  totalElements = 0;
  pageSize = 10;
  pageIndex = 0;
  loading = true;

  constructor(
    private organizationService: OrganizationService,
    private currentOrganizationService: CurrentOrganizationService,
    private dialog: MatDialog,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.loadOrganizations();
  }

  loadOrganizations(): void {
    this.loading = true;
    this.organizationService.getOrganizations(this.pageIndex, this.pageSize).subscribe({
      next: (page) => {
        this.organizations = page.content;
        this.totalElements = page.page.totalElements;
        this.loading = false;
      },
      error: () => { this.loading = false; }
    });
  }

  onPageChange(event: PageEvent): void {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadOrganizations();
  }

  isSelected(org: OrganizationDto): boolean {
    return this.currentOrganizationService.getCurrentOrganizationId() === org.id;
  }

  selectOrganization(org: OrganizationDto): void {
    this.currentOrganizationService.setCurrentOrganization(org.id!, org.name);
    this.snackBar.open(`"${org.name}" is now your active organization.`, 'Close', { duration: 3000 });
  }

  openCreateDialog(): void {
    const ref = this.dialog.open(OrganizationFormComponent, {
      width: '480px',
      data: { organization: null }
    });
    ref.afterClosed().subscribe(result => {
      if (result) this.loadOrganizations();
    });
  }

  openEditDialog(org: OrganizationDto): void {
    const ref = this.dialog.open(OrganizationFormComponent, {
      width: '480px',
      data: { organization: org }
    });
    ref.afterClosed().subscribe(result => {
      if (result) this.loadOrganizations();
    });
  }

  deleteOrganization(org: OrganizationDto): void {
    if (!confirm(`Delete organization "${org.name}"?`)) return;
    this.organizationService.deleteOrganization(org.id!).subscribe({
      next: () => {
        this.snackBar.open('Organization deleted.', 'Close', { duration: 3000 });
        if (this.isSelected(org)) this.currentOrganizationService.clear();
        this.loadOrganizations();
      },
      error: (err) => {
        const msg = err.status === 403
          ? 'You do not have permission to delete this organization.'
          : 'Failed to delete organization.';
        this.snackBar.open(msg, 'Close', { duration: 3000 });
      }
    });
  }
}
