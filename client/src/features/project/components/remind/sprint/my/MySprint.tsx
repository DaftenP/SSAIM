import React, { useState } from 'react';
import { useParams } from 'react-router-dom';
import styles from './MySprint.module.css';
import usePmIdStore from '@/features/project/stores/remind/usePmIdStore';
import { editSprintRemind } from '@/features/project/apis/remind/editSprintRemind';
import { SprintRemindRequestDTO } from '@/features/project/types/remind/SprintRemndDTO';
import { useSprintRemind } from '@/features/project/hooks/remind/useSprintRemind';
import { FaArrowRotateRight } from "react-icons/fa6";
import Loading from '@/components/loading/Loading';

interface Content {
  content: string;
  weeklyRemindId: number;
}

interface MySprintProps {
  contents: Content[];
  selectedDateInfo: { checkDate: string; startDate: string; endDate: string } | null;
}

const MySprint: React.FC<MySprintProps> = ({ contents, selectedDateInfo }) => {
  const { projectId: projectIdParam } = useParams<{ projectId: string }>();
  const { pmId } = usePmIdStore();
  const [keep, problem, trySection] = contents[0]?.content.split("\n\n") || [];
  const [isLoading, setIsLoading] = useState(false);

  const projectId = projectIdParam ? Number(projectIdParam) : undefined;

  const { refetch } = useSprintRemind({ projectId: projectId! });

  const handleEditClick = async () => {
    if (!projectId || !pmId || !selectedDateInfo || contents.length === 0) {
      console.error("필요한 데이터가 부족하여 요청을 보낼 수 없습니다.");
      return;
    }

    const sprintRemindRequestDTO: SprintRemindRequestDTO = {
      projectMemberId: pmId,
      startDate: selectedDateInfo.startDate,
      endDate: selectedDateInfo.endDate,
    };

    setIsLoading(true);

    try {
      const response = await editSprintRemind(
        Number(projectId),
        contents[0].weeklyRemindId,
        sprintRemindRequestDTO
      );
      console.log("Edit successful:", response);
      refetch();
    } catch (error) {
      console.error("Edit request failed:", error);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className={styles.myReviewContainer}>
      <div className={styles.myReview}>
        {/* Keep 섹션 */}
        <div className={styles.keepSection}>
          <div className={styles.sectionTitle}>
            <h3 className={styles.h3}>Keep</h3>
          </div>
          <div className={styles.reviewContainer}>
            <p className={styles.p}>{keep}</p>
          </div>
        </div>

        {/* Problem 섹션 */}
        <div className={styles.problemSection}>
          <div className={styles.sectionTitle}>
            <h3 className={styles.h3}>Problem</h3>
          </div>
          <div className={styles.reviewContainer}>
            <p className={styles.p}>{problem}</p>
          </div>
        </div>

        {/* Try 섹션 */}
        <div className={styles.trySection}>
          <div className={styles.sectionTitle}>
            <h3 className={styles.h3}>Try</h3>
          </div>
          <div className={styles.reviewContainer}>
            <p className={styles.p}>{trySection}</p>
          </div>
        </div>
      </div>
      {contents.length > 0 && (
        <div className={styles.editbox}>
          <div className={styles.editButton} onClick={handleEditClick}>
            <FaArrowRotateRight />
            <p className={styles.p}>다시 생성하기</p>
          </div>
        </div>
      )}
      {isLoading && <Loading />}
    </div>
  );
};

export default MySprint;