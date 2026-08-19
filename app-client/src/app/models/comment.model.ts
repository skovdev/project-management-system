export interface CommentDto {
  id: string;
  content: string;
  taskId: string;
  authorId: string;
  parentCommentId: string | null;
  createdAt: string;
  updatedAt: string;
  replies: CommentDto[];
}

export interface CommentRequestDto {
  content: string;
  parentCommentId?: string | null;
}