export type OrganizationRole = 'OWNER' | 'ADMIN' | 'MEMBER';

export const ORG_ROLES: OrganizationRole[] = ['OWNER', 'ADMIN', 'MEMBER'];

export interface OrganizationDto {
  id?: string;
  name: string;
  description: string;
  ownerId?: string;
}

export interface OrganizationMemberDto {
  id?: string;
  organizationId: string;
  userId: string;
  role: OrganizationRole;
}
