import './App.css'
import Modal from "./components/Modal/Modal";

function App() {
  const [isModalOpen, setIsModalOpen] = useState(false);

  const openModal = () => setIsModalOpen(true);
  const closeModal = () => setIsModalOpen(false);

  return (
    <>
      <div>
        <Button size="small" colorType="purple" onClick={() => alert('Button clicked')}>
          나의 회고
        </Button>
      </div>
      <h1>Vite + React</h1>
      <div className="card">
        <button onClick={openModal} style={{ padding: '10px 20px', backgroundColor: '#007BFF', color: 'white', border: 'none', borderRadius: '5px', cursor: 'pointer' }}>
          Open Modal
        </button>
        <p>
          Edit <code>src/App.tsx</code> and save to test HMR
        </p>
      </div>
      <p className="read-the-docs">
        Click on the Vite and React logos to learn more
      </p>

      <Modal
        isOpen={isModalOpen}
        onClose={closeModal}
        // title="유저 삭제"
        title={
          <>
            제목도 두줄 가능합니까<br />
            일단은요
          </>
        }
        content={
          // "정말로 삭제하시겠습니까?"
          <>
            정말로 삭제하시겠습니까?<br />
            삭제하면 되돌릴 수 없습니다.
          </>
        }
        footer={
          <>
            <button style={{ padding: '10px 20px', backgroundColor: 'blue', color: 'white', border: 'none', borderRadius: '5px', cursor: 'pointer' }}>
              파랑
            </button>
            <button style={{ padding: '10px 20px', backgroundColor: 'red', color: 'white', border: 'none', borderRadius: '5px', cursor: 'pointer' }}>
              빨강
            </button>
          </>
        }
        // width={400}
        height={250}
      />
    </>
  )
}

export default App