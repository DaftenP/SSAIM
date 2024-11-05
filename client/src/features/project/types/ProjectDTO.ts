// src/features/project/types/ProjectDTO.ts

export interface ProjectMemberDTO {
  id: number;
  role: number;
  userName: string;
  profileImage : string|null;
}

// 공통 프로젝트 속성 인터페이스 정의
interface BaseProjectDTO {
  title: string;
  name: string;
  profileImage: string;
  startDate: string | null | Date;
  endDate: string | null | Date;
  teamMembers: ProjectMemberDTO[] | null;
}

// ProjectCreateDTO에서 공통 속성 상속
export type ProjectCreateDTO = BaseProjectDTO;

// ProjectDTO에서 공통 속성 상속 및 고유 필드 추가
export interface ProjectDTO extends BaseProjectDTO {
  id : number;
  progress_front: number;
  progress_back: number;
  // teamMembers 배열이 항상 null이 아닌 경우 타입에서 | null 제거 가능
  teamMembers: ProjectMemberDTO[]|null;
}