export interface SchoolClass {
  id: number;
  name: string;
  description: string | null;
  joinCode: string | null;
  teacherId: number;
  teacherName: string;
  memberCount: number;
  owner: boolean;
  createdAt: string;
}

export interface ClassMember {
  userId: number;
  name: string;
  email: string;
  role: string;
  joinedAt: string;
}
