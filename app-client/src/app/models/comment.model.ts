export interface CommentDto {
  id: string;
  content: string;
  taskId: string;
  authorId: string;
  createdAt: string;
  updatedAt: string;
}

export interface CommentRequestDto {
  content: string;
}