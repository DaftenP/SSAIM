import React from 'react';
import styles from './Tag.module.css';

interface TagProps {
    text: string;
}

const Tag: React.FC<TagProps> = ({ text }) => {
    // 기본 색상 정의
    const defaultBackgroundColor = '#d9d9d9';
    const defaultFontColor = '#000000';

    // 색상 맵 정의
    const colorMap: { [key: string]: { backgroundColor: string; fontColor: string } } = {
        FE: { backgroundColor: '#B6EDD7', fontColor: '#0EA475' },
        BE: { backgroundColor: '#D9E5F8', fontColor: '#427DDC' },
        Infra: { backgroundColor: '#FFF0D8', fontColor: '#FFA308' },
        자유주제: { backgroundColor: '#17C585', fontColor: '#FFFFFF' },
        기업연계: { backgroundColor: '#913BF6', fontColor: '#FFFFFF' },
        AI영상: { backgroundColor: '#433878', fontColor: '#FFFFFF' },
        AI음성: { backgroundColor: '#891CB4', fontColor: '#FFFFFF' },
        분산: { backgroundColor: '#1DA10B', fontColor: '#FFFFFF' },
        추천: { backgroundColor: '#216E4E', fontColor: '#FFFFFF' },
        P2P: { backgroundColor: '#124992', fontColor: '#FFFFFF' },
        디지털거래: { backgroundColor: '#001EB6', fontColor: '#FFFFFF' },
        자율주행: { backgroundColor: '#E78835', fontColor: '#FFFFFF' },
        스마트홈: { backgroundColor: '#DB5B00', fontColor: '#FFFFFF' },
        메타버스: { backgroundColor: '#962020', fontColor: '#FFFFFF' },
        핀테크: { backgroundColor: '#009CB8', fontColor: '#FFFFFF' },
        웹기술: { backgroundColor: '#d9d9d9', fontColor: '#000000' },
        웹디자인: { backgroundColor: '#d9d9d9', fontColor: '#000000' },
        모바일: { backgroundColor: '#d9d9d9', fontColor: '#000000' },
        AIoT: { backgroundColor: '#d9d9d9', fontColor: '#000000' },
    };

    // 해당 텍스트에 맞는 색상 가져오기
    const { backgroundColor, fontColor } = colorMap[text] || { backgroundColor: defaultBackgroundColor, fontColor: defaultFontColor };

    return (
        <span
            className={styles.tag}
            style={{ backgroundColor, color: fontColor }}
        >
            {text}
        </span>
    );
};

export default Tag;
