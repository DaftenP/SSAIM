import React, { useState, useEffect, useRef } from 'react';
import styles from './ApiSpec.module.css';
import { MdDelete } from "react-icons/md";
import { Stomp } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { getApiSpec, getAutoApiSpec } from '@features/project/apis/webSocket/apiSpec';
import ApiDetailModal from './apiDetailModal/ApiDetailModal';
import CommonModal from '@components/modal/Modal';
import Button from '@components/button/Button';
import Tag from '@/features/teamBuilding/components/tag/Tag';
import { getApiStatusLabel } from '../../../../utils/labelUtils'
import { MdOpenInNew } from "react-icons/md";
import Loading from '@/components/loading/Loading';
import { showToast } from '@/utils/toastUtils';
import { getProposal } from '../../apis/webSocket/proposal';
import { getFeatureSpec } from '../../apis/webSocket/featureSpec';
import { useProjectInfo } from '../../hooks/useProjectInfo';

interface ApiSpecData {
  category: string[];
  functionName: string[];
  uri: string[];
  method: string[];
  frontOwner: string[];
  backOwner: string[];
  frontState: string[];
  backState: string[];
  priority: string[];
  description: string[];
  requestHeader: string[];
  responseHeader: string[];
  requestBody: string[];
  responseBody: string[];
}

interface ApiSpecTableProps {
  projectId: string;
  isWebSocketConnected: boolean;
}

const ApiSpecTable: React.FC<ApiSpecTableProps> = ({ projectId, isWebSocketConnected }) => {
  const [data, setData] = useState<ApiSpecData>({
    category: [],
    functionName: [],
    uri: [],
    method: [],
    frontOwner: [],
    backOwner: [],
    frontState: [],
    backState: [],
    priority: [],
    description: [],
    requestHeader: [],
    responseHeader: [],
    requestBody: [],
    responseBody: [],
  });
  const tableRef = useRef<HTMLDivElement | null>(null);
  const stompClientRef = useRef<any>(null);
  const [selectedRowIndex, setSelectedRowIndex] = useState<number | null>(null);
  const [isModalOpen, setIsModalOpen] = useState<boolean>(false);
  const [isAiModalOpen, setIsAiModalOpen] = useState<boolean>(false);
  const [modalTextareaValue, setModalTextareaValue] = useState<string>('');
  const [isEditing, setIsEditing] = useState<{ [rowIndex: number]: { [column: string]: boolean } }>({});
  const [isGenerating, setIsGenerating] = useState<boolean>(false);
  const { data: projectInfo } = useProjectInfo(parseInt(projectId));

  const openModal = (rowIndex: number) => {
    setSelectedRowIndex(rowIndex);
    setIsModalOpen(true);
  };

  const closeModal = () => {
    setSelectedRowIndex(null);
    setIsModalOpen(false);
  };

  useEffect(() => {
    const fetchApiSpec = async () => {
      try {
        const fetchedData = await getApiSpec(projectId);
        setData(fetchedData);
      } catch (error) {
        console.error('Error fetching api spec:', error);
      }
    };

    fetchApiSpec();
  }, [projectId]);

  useEffect(() => {
    if (isWebSocketConnected) {
      const socket = new SockJS('https://k11e203.p.ssafy.io:8080/ws');
      const stompClient = Stomp.over(socket);
      stompClientRef.current = stompClient;

      stompClient.connect({}, () => {
        const subscriptionPath = `/topic/api/v1/projects/${projectId}/api-docs`;

        stompClient.subscribe(subscriptionPath, (message) => {
          try {
            const updatedData = JSON.parse(message.body);
            setData(updatedData);
          } catch (error) {
            console.error('Error parsing WebSocket message:', error);
          }
        });
      });

      return () => {
        if (stompClient.connected) {
          stompClient.disconnect(() => {
            console.log('WebSocket disconnected for ApiSpecTable');
          });
        }
      };
    }
  }, [isWebSocketConnected, projectId]);

  const addNewRow = () => {
    const updatedData = {
      ...data,
      category: [...data.category, ''],
      functionName: [...data.functionName, ''],
      uri: [...data.uri, ''],
      method: [...data.method, 'GET'],
      frontOwner: [...data.frontOwner, ''],
      backOwner: [...data.backOwner, ''],
      frontState: [...data.frontState, '0'],
      backState: [...data.backState, '0'],
      priority: [...data.priority, ''],
      description: [...data.description, ''],
      requestHeader: [...data.requestHeader, ''],
      responseHeader: [...data.responseHeader, ''],
      requestBody: [...data.requestBody, ''],
      responseBody: [...data.responseBody, ''],
    };

    setData(updatedData);

    if (stompClientRef.current?.connected) {
      stompClientRef.current.send(
        `/app/edit/api/v1/projects/${projectId}/api-docs`,
        {},
        JSON.stringify(updatedData)
      );
    }
  };

  const handleDeleteRow = (index: number) => {
    const updatedData = {
      category: data.category.filter((_, i) => i !== index),
      functionName: data.functionName.filter((_, i) => i !== index),
      uri: data.uri.filter((_, i) => i !== index),
      method: data.method.filter((_, i) => i !== index),
      frontOwner: data.frontOwner.filter((_, i) => i !== index),
      backOwner: data.backOwner.filter((_, i) => i !== index),
      frontState: data.frontState.filter((_, i) => i !== index),
      backState: data.backState.filter((_, i) => i !== index),
      priority: data.priority.filter((_, i) => i !== index),
      description: data.description.filter((_, i) => i !== index),
      requestHeader: data.requestHeader.filter((_, i) => i !== index),
      responseHeader: data.responseHeader.filter((_, i) => i !== index),
      requestBody: data.requestBody.filter((_, i) => i !== index),
      responseBody: data.responseBody.filter((_, i) => i !== index),
    };

    setData(updatedData);

    if (stompClientRef.current?.connected) {
      stompClientRef.current.send(
        `/app/edit/api/v1/projects/${projectId}/api-docs`,
        {},
        JSON.stringify(updatedData)
      );
    }
  };
  
  const maxCharCount = 200;

  const handleModalTextareaChange = (e: React.ChangeEvent<HTMLTextAreaElement>) => {
    if (e.target.value.length <= maxCharCount) {
      setModalTextareaValue(e.target.value);
    }
  };


  const parseBacktickJson = (data: string) => {
    // 백틱과 "```json" 태그 제거
    const cleanedData = data.replace(/```json|```/g, "").trim();
    
    try {
      // JSON 문자열을 객체로 변환
      const parsedData = JSON.parse(cleanedData);
      return parsedData;
    } catch (error) {
      console.error("Error parsing JSON:", error);
      return data; // 파싱 실패 시 null 반환
    }
  };

  const handleModalSubmit = async () => {
    try {
      setIsGenerating(true);
      let response;
      try {
        response = await getAutoApiSpec(projectId, modalTextareaValue);
      } catch (fetchError) {
        showToast.error('자동 생성에 실패했습니다. 다시 시도해 주세요.');
        return;
      }
  
      let parsedData;
  
      // response가 문자열인지 확인
      if (typeof response === 'string') {
        console.log('Response is a string. Attempting to parse with parseBacktickJson.');
        parsedData = parseBacktickJson(response); // 백틱 JSON 처리
      } else if (typeof response === 'object') {
        console.log('Response is an object. Using it directly.');
        parsedData = response; // 객체 그대로 사용
      } else {
        console.error('Unexpected response type:', typeof response);
        parsedData = null;
      }
  
      // 파싱된 데이터를 상태에 반영
      if (parsedData) {
        const maxLength = Math.max(
          ...Object.values(parsedData).map((value) => Array.isArray(value) ? value.length : 0)
        );

        Object.keys(parsedData).forEach((key) => {
          if (Array.isArray(parsedData[key])) {
            while (parsedData[key].length < maxLength) {
              parsedData[key].push('');
            }
          }
        });

        parsedData.frontOwner = Array(maxLength).fill(''),
        parsedData.backOwner = Array(maxLength).fill(''),
        parsedData.frontState = Array(maxLength).fill('0'),
        parsedData.backState = Array(maxLength).fill('0'),
        parsedData.requestHeader = Array(maxLength).fill(''),
        parsedData.responseHeader = Array(maxLength).fill(''),
        parsedData.requestBody = Array(maxLength).fill(''),
        parsedData.responseBody = Array(maxLength).fill(''),
        setData(parsedData);
  
        // WebSocket을 통해 실시간 반영
        if (stompClientRef.current && stompClientRef.current.connected) {
          stompClientRef.current.send(
            `/app/edit/api/v1/projects/${projectId}/api-docs`, // WebSocket 경로
            {}, // 헤더
            JSON.stringify(parsedData) // WebSocket으로 데이터 전송
          );
          console.log('WebSocket message sent:', parsedData);
        } else {
          console.warn('WebSocket is not connected. Cannot send message.');
        }
      } else {
        console.error('Parsed data is null or undefined.');
      }
  
      setIsAiModalOpen(false); // 모달 닫기
    } catch (error) {
      showToast.error('AI가 요청을 이해하지 못했습니다. 요청을 확인해주세요.')
    } finally {
      setIsGenerating(false); // 로딩 종료
    }
  };

  const openAiModal = async() => {
    try {
      const response = await getProposal(projectId);

      const requiredFields = ['title', 'description', 'background', 'feature', 'effect'];

      const allFieldsFilled = requiredFields.every(field =>
        response[field] &&
        response[field].toString().trim() !== '' &&
        !(typeof response[field] === 'object' && Object.keys(response[field]).length === 0) // 비어있는 객체가 아님
      );

      if (!allFieldsFilled) {
        showToast.error('기획서를 먼저 완성해주세요.');
        return;
      }
    } catch (error) {
      console.error('API 호출 실패:', error);
      showToast.error('데이터를 불러오는 데 실패했습니다. 다시 시도해 주세요.');
      return;
    }

    try {
      const response = await getFeatureSpec(projectId);

      const requiredFields = ['category', 'functionName', 'description'];

      const invalidFields = requiredFields.filter(field =>
        !Array.isArray(response[field]) ||
        !response[field].some((value: any) => typeof value === 'string' && value.trim() !== '')
      );
    
      if (invalidFields.length > 0) {
        showToast.error('기능 명세서를 먼저 완성해주세요.');
        return;
      }
    } catch (error) {
      console.error('API 호출 실패:', error);
      showToast.error('데이터를 불러오는 데 실패했습니다. 다시 시도해 주세요.');
      return;
    }
    
    setModalTextareaValue('')
    setIsAiModalOpen(true);
  };

  const closeAiModal = () => {
    setIsAiModalOpen(false);
  };

  const handleEditClick = (rowIndex: number, column: keyof ApiSpecData) => {
    setIsEditing({ [rowIndex]: { [column]: true } });
  };

  const autoResize = (element: HTMLTextAreaElement) => {
    element.style.height = 'auto';
    element.style.height = `${element.scrollHeight}px`;
  };

  const handleInputChange = (column: keyof ApiSpecData, rowIndex: number, value: string) => {
    const updatedColumn = [...data[column]];
    updatedColumn[rowIndex] = value;
  
    const updatedData = { ...data, [column]: updatedColumn };
    setData(updatedData);
  
    if (stompClientRef.current?.connected) {
      stompClientRef.current.send(
        `/app/edit/api/v1/projects/${projectId}/api-docs`,
        {},
        JSON.stringify(updatedData)
      );
    }
  };

  const handleBlur = () => {
    setIsEditing({});
  };

  const handleKeyPress = (
    e: React.KeyboardEvent<HTMLTextAreaElement>,
  ) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      handleBlur();
    }
  };

  return (
    <div className={styles.tableContainer} ref={tableRef}>
      <h2 className={styles.sectionTitle}>
        API 명세서
        <div className={styles.aiButton}>
          <Button size='custom' colorType='purple' onClick={openAiModal}>AI 자동생성</Button>
        </div>
      </h2>
      <CommonModal 
        isOpen={isAiModalOpen}
        onClose={closeAiModal}
        title= ' API 명세서 자동 생성'
        content={
          <>
            {isGenerating && <Loading />}
            <textarea
              className={styles.modalTextarea}
              value={modalTextareaValue}
              onChange={handleModalTextareaChange}
            ></textarea>
            <div className={styles.charCounter}>
              {modalTextareaValue.length} / {maxCharCount}
            </div>
          </>
        }
        footer={
          <Button size='custom' colorType='blue' onClick={handleModalSubmit}>
            AI 자동생성
          </Button>
        }
        width={800}
        height={400}
        isOutsideClick={false}
      />
      <table className={styles.table}>
        <thead>
          <tr>
            <th className={styles.actionColumn}></th>
            <th>분류</th>
            <th>API 이름</th>
            <th>URI</th>
            <th>메소드</th>
            <th>FE 담당</th>
            <th>BE 담당</th>
            <th>FE 상태</th>
            <th>BE 상태</th>
            <th>우선순위</th>
            <th className={styles.actionColumn}></th>
          </tr>
        </thead>
        <tbody>
          {data.category.map((_, index) => (
            <React.Fragment key={index}>
              <tr>
                <td className={styles.actionCell}>
                  <MdOpenInNew
                    onClick={(e) => {
                      e.stopPropagation();
                      openModal(index);
                    }}
                    className={styles.actionButton}
                  />
                </td>
                {(['category', 'functionName', 'uri', 'method', 'frontOwner', 'backOwner', 'frontState', 'backState', 'priority'] as const).map((column) => (
                  <td
                    key={column}
                    onClick={() => handleEditClick(index, column)}
                    className={styles.tableCell}
                  >
                    {column === 'frontState' || column === 'backState' ? (
                      <>
                        <div className={styles.tagWrapper}>
                          <Tag text={getApiStatusLabel(parseInt(data[column][index]))} />
                        </div>

                        {isEditing[index]?.[column] && (
                          <div
                            className={styles.tagOptions}
                            onClick={(e) => e.stopPropagation()}
                          >
                            {[0, 1, 2].map((value) => (
                              <div
                                key={value}
                                onClick={() => {
                                  handleInputChange(column, index, value.toString());
                                  handleBlur();
                                }}
                                className={styles.tagOptionWrapper}
                              >
                                <Tag text={getApiStatusLabel(value)} />
                              </div>
                            ))}
                          </div>
                        )}
                      </>
                    ) : column === 'priority' ? ( 
                      <>
                        <div className={styles.tagWrapper}>
                          <Tag text={data[column][index]} />
                        </div>
                        {isEditing[index]?.[column] && (
                          <div
                            className={styles.tagOptions}
                            onClick={(e) => e.stopPropagation()}
                          >
                            {['상', '중', '하'].map((value) => (
                              <div
                                key={value}
                                onClick={() => {
                                  handleInputChange(column, index, value);
                                  handleBlur();
                                }}
                                className={styles.tagOptionWrapper}
                              >
                                <Tag text={value} />
                              </div>
                            ))}
                          </div>
                        )}
                      </>
                    ) : column === 'method' ? ( 
                      <>
                        <div className={styles.tagWrapper}>
                          <Tag text={data[column][index]} />
                        </div>
                        {isEditing[index]?.[column] && (
                          <div
                            className={styles.tagOptions}
                            onClick={(e) => e.stopPropagation()}
                          >
                            {['GET', 'POST', 'PUT', 'PATCH', 'DELETE'].map((value) => (
                              <div
                                key={value}
                                onClick={() => {
                                  handleInputChange(column, index, value);
                                  handleBlur(); // 선택 후 편집 종료
                                }}
                                className={styles.tagOptionWrapper}
                              >
                                <Tag text={value} />
                              </div>
                            ))}
                          </div>
                        )}
                      </>
                    ) : column === 'frontOwner' || column === 'backOwner' ? ( 
                      <>
                        <div className={styles.tagWrapper}>
                          {data[column][index]}
                        </div>
                        {isEditing[index]?.[column] && (
                          <div
                            className={styles.tagOptions}
                            onClick={(e) => e.stopPropagation()}
                          >
                            {projectInfo?.projectMembers.map((value) => (
                              <div
                                key={value.pmId}
                                onClick={() => {
                                  handleInputChange(column, index, value.name);
                                  handleBlur();
                                }}
                                className={styles.tagOptionWrapper}
                              >
                                {value.name}
                              </div>
                            ))}
                          </div>
                        )}
                      </>
                    ) : isEditing[index]?.[column] ? (
                      <textarea
                        value={data[column][index]}
                        onChange={(e) => handleInputChange(column, index, e.target.value)}
                        onKeyDown={(e) => handleKeyPress(e)}
                        onBlur={() => handleBlur()}
                        autoFocus
                        ref={(el) => el && autoResize(el)}
                        className={styles.editTextarea}
                      />
                    ) : (
                      data[column][index]?.length > (column === "category" ? 50 : 250) ? (
                        <span title={data[column][index]}>
                          {data[column][index].slice(0, column === "category" ? 50 : 250)}...
                        </span>
                      ) : (
                        data[column][index]
                      )
                    )}
                  </td>
                ))}
                <td>
                  <MdDelete
                    onClick={(e) => {
                      e.stopPropagation(); // 이벤트 버블링 방지
                      handleDeleteRow(index);
                    }}
                  />
                </td>
              </tr>
            </React.Fragment>
          ))}
          <tr className={styles.addRow} onClick={addNewRow}>
            <td colSpan={11} className={styles.addRowTd}>+ Add New Row</td> {/* colspan을 11로 조정 */}
          </tr>
        </tbody>
      </table>
      {isModalOpen && selectedRowIndex !== null && (
        <ApiDetailModal
          isOpen={isModalOpen}
          onClose={closeModal}
          data={data}
          rowIndex={selectedRowIndex}
          setData={setData}
          projectId={projectId}
          stompClient={stompClientRef.current}
        />
      )}
    </div>
  );
};

export default ApiSpecTable;