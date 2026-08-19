import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { TaskService } from '../../../services/task.service';
import { CommentService } from '../../../services/comment.service';
import { AuthTokenService } from '../../../services/auth-token.service';
import { TaskDto } from '../../../models/task.model';
import { CommentDto } from '../../../models/comment.model';
import { TaskFormComponent } from '../task-form/task-form.component';
import { CommentItemComponent } from './comment-item/comment-item.component';

@Component({
  selector: 'app-task-detail',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatCardModule, MatButtonModule, MatIconModule, MatProgressSpinnerModule,
    MatFormFieldModule, MatInputModule, MatPaginatorModule,
    CommentItemComponent
  ],
  templateUrl: './task-detail.component.html',
  styleUrl: './task-detail.component.css'
})
export class TaskDetailComponent implements OnInit {
  task: TaskDto | null = null;
  loading = true;
  error = false;
  taskId = '';

  comments: CommentDto[] = [];
  commentsTotalElements = 0;
  commentsPageSize = 5;
  commentsPageIndex = 0;
  commentsLoading = false;

  newCommentContent = '';
  submittingComment = false;

  currentUserId: string | null;

  // ── Acceptance Criteria ──────────────────────────────────────────────────
  acText = '';
  savedAcText = '';
  acGenerating = false;
  acSaving = false;

  get hasAc(): boolean {
    return this.acText.trim().length > 0;
  }

  get acDirty(): boolean {
    return this.acText !== this.savedAcText;
  }

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private taskService: TaskService,
    private commentService: CommentService,
    private authTokenService: AuthTokenService,
    private dialog: MatDialog,
    private snackBar: MatSnackBar
  ) {
    this.currentUserId = this.authTokenService.getAuthUserId();
  }

  ngOnInit(): void {
    this.taskId = this.route.snapshot.paramMap.get('id')!;
    this.taskService.getTask(this.taskId).subscribe({
      next: (res) => {
        this.task = res.data;
        this.savedAcText = res.data.acceptanceCriteria ?? '';
        this.acText = this.savedAcText;
        this.loading = false;
        this.loadComments();
      },
      error: () => {
        this.error = true;
        this.loading = false;
      }
    });
  }

  loadComments(): void {
    this.commentsLoading = true;
    this.commentService.getComments(this.taskId, this.commentsPageIndex, this.commentsPageSize).subscribe({
      next: (page) => {
        this.comments = page.content;
        this.commentsTotalElements = page.page.totalElements;
        this.commentsLoading = false;
      },
      error: () => { this.commentsLoading = false; }
    });
  }

  onCommentsPageChange(event: PageEvent): void {
    this.commentsPageIndex = event.pageIndex;
    this.commentsPageSize = event.pageSize;
    this.loadComments();
  }

  submitComment(): void {
    const content = this.newCommentContent.trim();
    if (!content) return;
    this.submittingComment = true;
    this.commentService.createComment(this.taskId, { content }).subscribe({
      next: () => {
        this.newCommentContent = '';
        this.submittingComment = false;
        this.commentsPageIndex = 0;
        this.loadComments();
        this.snackBar.open('Comment posted.', 'Close', { duration: 3000 });
      },
      error: () => {
        this.submittingComment = false;
        this.snackBar.open('Failed to post comment.', 'Close', { duration: 3000 });
      }
    });
  }

  openEditDialog(): void {
    const ref = this.dialog.open(TaskFormComponent, {
      width: '560px',
      data: { task: this.task }
    });
    ref.afterClosed().subscribe((updated: TaskDto | null) => {
      if (updated) {
        this.task = updated;
        this.snackBar.open('Task updated.', 'Close', { duration: 3000 });
      }
    });
  }

  goBack(): void {
    this.router.navigate(['/tasks']);
  }

  // ── Acceptance Criteria methods ──────────────────────────────────────────

  generateAc(): void {
    this.acGenerating = true;
    this.taskService.generateAcceptanceCriteria(this.taskId).subscribe({
      next: (res) => {
        this.acText = res.data ?? '';
        this.acGenerating = false;
      },
      error: () => {
        this.acGenerating = false;
        this.snackBar.open('Failed to generate acceptance criteria.', 'Close', { duration: 4000 });
      }
    });
  }

  cancelAc(): void {
    this.acText = this.savedAcText;
  }

  saveAc(): void {
    if (!this.task) return;
    this.acSaving = true;
    const updated: TaskDto = { ...this.task, acceptanceCriteria: this.acText };
    this.taskService.updateTask(this.taskId, updated).subscribe({
      next: (res) => {
        this.task = res.data;
        this.savedAcText = this.acText;
        this.acSaving = false;
        this.snackBar.open('Acceptance criteria saved.', 'Close', { duration: 3000 });
      },
      error: () => {
        this.acSaving = false;
        this.snackBar.open('Failed to save acceptance criteria.', 'Close', { duration: 3000 });
      }
    });
  }
}
