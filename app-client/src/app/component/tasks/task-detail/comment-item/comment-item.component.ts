import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSnackBar } from '@angular/material/snack-bar';
import { CommentService } from '../../../../services/comment.service';
import { CommentDto } from '../../../../models/comment.model';

@Component({
  selector: 'app-comment-item',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatButtonModule, MatIconModule, MatProgressSpinnerModule,
    MatFormFieldModule, MatInputModule,
    CommentItemComponent
  ],
  templateUrl: './comment-item.component.html',
  styleUrl: './comment-item.component.css'
})
export class CommentItemComponent {
  @Input({ required: true }) comment!: CommentDto;
  @Input({ required: true }) taskId!: string;
  @Input() currentUserId: string | null = null;
  @Input() isReply = false;

  @Output() changed = new EventEmitter<void>();

  editing = false;
  editingContent = '';

  replying = false;
  replyContent = '';
  submittingReply = false;

  suggestionsLoading = false;
  showSuggestions = false;
  suggestions: string[] = [];

  constructor(private commentService: CommentService, private snackBar: MatSnackBar) {}

  get isOwnComment(): boolean {
    return !!this.currentUserId && this.comment.authorId === this.currentUserId;
  }

  shortId(id: string): string {
    return id.slice(0, 8);
  }

  // ── Edit ──────────────────────────────────────────────────────────────

  startEdit(): void {
    this.editing = true;
    this.editingContent = this.comment.content;
    this.replying = false;
    this.dismissSuggestions();
  }

  cancelEdit(): void {
    this.editing = false;
    this.editingContent = '';
  }

  saveEdit(): void {
    const content = this.editingContent.trim();
    if (!content) return;
    this.commentService.updateComment(this.taskId, this.comment.id, { content }).subscribe({
      next: (res) => {
        this.comment.content = res.data.content;
        this.comment.updatedAt = res.data.updatedAt;
        this.cancelEdit();
        this.snackBar.open('Comment updated.', 'Close', { duration: 3000 });
      },
      error: () => {
        this.snackBar.open('Failed to update comment.', 'Close', { duration: 3000 });
      }
    });
  }

  // ── Delete ────────────────────────────────────────────────────────────

  deleteComment(): void {
    if (!confirm('Delete this comment?')) return;
    this.commentService.deleteComment(this.taskId, this.comment.id).subscribe({
      next: () => {
        this.snackBar.open('Comment deleted.', 'Close', { duration: 3000 });
        this.changed.emit();
      },
      error: (err) => {
        const msg = err.status === 403
          ? 'You can only delete your own comments.'
          : 'Failed to delete comment.';
        this.snackBar.open(msg, 'Close', { duration: 3000 });
      }
    });
  }

  // ── Reply ─────────────────────────────────────────────────────────────

  startReply(): void {
    this.replying = true;
    this.editing = false;
    this.dismissSuggestions();
  }

  cancelReply(): void {
    this.replying = false;
    this.replyContent = '';
  }

  submitReply(): void {
    const content = this.replyContent.trim();
    if (!content) return;
    this.submittingReply = true;
    this.commentService.createComment(this.taskId, { content, parentCommentId: this.comment.id }).subscribe({
      next: () => {
        this.submittingReply = false;
        this.cancelReply();
        this.snackBar.open('Reply posted.', 'Close', { duration: 3000 });
        this.changed.emit();
      },
      error: () => {
        this.submittingReply = false;
        this.snackBar.open('Failed to post reply.', 'Close', { duration: 3000 });
      }
    });
  }

  onReplyChanged(): void {
    this.changed.emit();
  }

  // ── AI reply suggestions ─────────────────────────────────────────────

  generateSuggestions(): void {
    this.suggestionsLoading = true;
    this.dismissSuggestions();
    this.commentService.generateReplySuggestions(this.taskId, this.comment.id).subscribe({
      next: (res) => {
        this.suggestions = res.data ?? [];
        this.showSuggestions = true;
        this.suggestionsLoading = false;
      },
      error: () => {
        this.suggestionsLoading = false;
        this.snackBar.open('Failed to generate reply suggestions.', 'Close', { duration: 4000 });
      }
    });
  }

  useSuggestion(text: string): void {
    this.dismissSuggestions();
    this.startReply();
    this.replyContent = text;
  }

  dismissSuggestions(): void {
    this.showSuggestions = false;
    this.suggestions = [];
  }
}
