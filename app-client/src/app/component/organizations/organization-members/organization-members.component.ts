import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSelectModule } from '@angular/material/select';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatCardModule } from '@angular/material/card';
import { OrganizationService } from '../../../services/organization.service';
import { UserService } from '../../../services/user.service';
import { OrganizationMemberDto, OrganizationRole, ORG_ROLES } from '../../../models/organization.model';
import { UserDto } from '../../../models/user.model';

@Component({
  selector: 'app-organization-members',
  standalone: true,
  imports: [
    CommonModule, RouterLink, ReactiveFormsModule,
    MatTableModule, MatPaginatorModule, MatButtonModule, MatIconModule,
    MatSelectModule, MatFormFieldModule, MatProgressSpinnerModule, MatCardModule
  ],
  templateUrl: './organization-members.component.html',
  styleUrl: './organization-members.component.css'
})
export class OrganizationMembersComponent implements OnInit {
  displayedColumns = ['user', 'role', 'actions'];
  organizationId = '';
  members: OrganizationMemberDto[] = [];
  usersById = new Map<string, UserDto>();
  allUsers: UserDto[] = [];
  totalElements = 0;
  pageSize = 10;
  pageIndex = 0;
  loading = true;
  roles = ORG_ROLES;

  showAddForm = false;
  addForm!: FormGroup;
  adding = false;

  constructor(
    private route: ActivatedRoute,
    private organizationService: OrganizationService,
    private userService: UserService,
    private fb: FormBuilder,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.organizationId = this.route.snapshot.paramMap.get('organizationId')!;
    this.addForm = this.fb.group({
      userId: [null, Validators.required],
      role: ['MEMBER', Validators.required]
    });
    this.userService.getUsers(0, 1000).subscribe({
      next: (page) => {
        this.allUsers = page.content;
        this.usersById = new Map(page.content.map(u => [u.id, u]));
        this.loadMembers();
      },
      error: () => this.loadMembers()
    });
  }

  loadMembers(): void {
    this.loading = true;
    this.organizationService.getMembers(this.organizationId, this.pageIndex, this.pageSize).subscribe({
      next: (page) => {
        this.members = page.content;
        this.totalElements = page.page.totalElements;
        this.loading = false;
      },
      error: () => { this.loading = false; }
    });
  }

  onPageChange(event: PageEvent): void {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadMembers();
  }

  displayName(userId: string): string {
    const u = this.usersById.get(userId);
    return u ? `${u.firstName} ${u.lastName} (${u.email})` : userId;
  }

  get availableUsers(): UserDto[] {
    const memberIds = new Set(this.members.map(m => m.userId));
    return this.allUsers.filter(u => !memberIds.has(u.id));
  }

  toggleAddForm(): void {
    this.showAddForm = !this.showAddForm;
    if (this.showAddForm) {
      this.addForm.reset({ userId: null, role: 'MEMBER' });
    }
  }

  addMember(): void {
    if (this.addForm.invalid) return;
    const { userId, role } = this.addForm.value;
    this.adding = true;
    this.organizationService.addMember(this.organizationId, userId, role).subscribe({
      next: () => {
        this.adding = false;
        this.showAddForm = false;
        this.snackBar.open('Member added.', 'Close', { duration: 3000 });
        this.loadMembers();
      },
      error: (err) => {
        this.adding = false;
        this.snackBar.open(err.error?.message ?? 'Failed to add member.', 'Close', { duration: 3000 });
      }
    });
  }

  changeRole(member: OrganizationMemberDto, role: OrganizationRole): void {
    if (role === member.role) return;
    this.organizationService.updateMemberRole(this.organizationId, member.id!, role).subscribe({
      next: () => {
        this.snackBar.open('Role updated.', 'Close', { duration: 3000 });
        this.loadMembers();
      },
      error: (err) => {
        this.snackBar.open(err.error?.message ?? 'Failed to update role.', 'Close', { duration: 3000 });
        this.loadMembers();
      }
    });
  }

  removeMember(member: OrganizationMemberDto): void {
    if (!confirm(`Remove ${this.displayName(member.userId)} from this organization?`)) return;
    this.organizationService.removeMember(this.organizationId, member.id!).subscribe({
      next: () => {
        this.snackBar.open('Member removed.', 'Close', { duration: 3000 });
        this.loadMembers();
      },
      error: (err) => {
        this.snackBar.open(err.error?.message ?? 'Failed to remove member.', 'Close', { duration: 3000 });
      }
    });
  }
}
