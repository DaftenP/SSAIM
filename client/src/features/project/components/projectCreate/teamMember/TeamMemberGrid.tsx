import useTeamStore from '../../../stores/useTeamStore';
import SearchModal from '../searchModal/SearchModal';
import styles from './TeamMemberGrid.module.css';
import { CiCircleMinus } from 'react-icons/ci';
import leaderCrown from '@/assets/project/leaderCrown.png';

const TeamMemberGrid = () => {
  const { members, isModalOpen, openModal, closeModal, removeMember, leaderId, setLeaderId } = useTeamStore();
  const handleAddClick = () => {
    openModal();
    // 검색 모달을 여는 함수
  };
  const handleRemoveClick = (id: number) => () => {
    removeMember(id);
  };

  const handleSetLeader = (id: number) => () => {
    setLeaderId(id);
  };
  return (
    <>
      <div className={styles.gridContainer}>
        {members.map((member) => (
          <div key={member.userId} className={styles.memberBox}>
            <div className={styles.memberBoxLeft}>
              { (member.userId === leaderId) ? 
                <div className={styles.leaderRibbon}>
                  <img src={leaderCrown} alt="leaderCrown" className={styles.leaderCrown} />
                </div>
                : null
              }
              <img src={member.userProfileImage} alt={`${member.userName}의 프로필`} className={styles.profileImage} />
              <div className={styles.memberInfo}>
                <div className={styles.name}>{member.userName}</div>
                <div className={styles.email}>{member.userEmail}</div>
              </div>
            </div>
            <div className={styles.memberBoxRight}>
              <button
                className={styles.leaderButton}
                disabled={member.userId === leaderId}
                onClick={handleSetLeader(member.userId)}
              >
                팀장 선택
              </button>
              <div className={styles.removeButton} onClick={handleRemoveClick(member.userId)}>
                <CiCircleMinus />
              </div>
            </div>
          </div>
        ))}
        {members.length < 10 && (
          <div className={styles.addBox} onClick={handleAddClick}>
            +
          </div>
        )}
      </div>
      {isModalOpen && <SearchModal onClose={closeModal} />}
    </>
  );
};

export default TeamMemberGrid;