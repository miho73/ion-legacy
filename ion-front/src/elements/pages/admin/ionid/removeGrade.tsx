import axios from 'axios';
import React, { useState } from 'react'
import { Alert, Button, FormControl, InputGroup } from 'react-bootstrap';

function RemoveGrade() {
    const [ionId, setIonId] = useState('');
    const [workState, setWorkState] = useState(-1);

    function exe() {
        axios.patch('/manage/api/ionid/eliminate', {
            id: ionId
        })
        .then(res => {
            setWorkState(0);
        })
        .catch(err => {
            switch(err.response?.data['result']) {
                case 1:
                    setWorkState(1);
                    break;
                case 2:
                    setWorkState(2);
                    break;
                default:
                    setWorkState(3);
            }
        });
    }

    return (
        <>
            <InputGroup className='w-25'>
                <InputGroup.Text>IonID</InputGroup.Text>
                <FormControl type='text' placeholder='IonID' value={ionId} onChange={e => setIonId(e.target.value)}/>
                <Button onClick={exe}>확인</Button>
            </InputGroup>
            <p className='my-1'>이 작업은 해당 IonID의 학년, 반, 번호 정보를 제거하여 학생으로 인식되지 않도록 합니다.</p>
            <p className='my-1 fst-italic fw-bold'>이 작업은 비가역적입니다.</p>
            {workState === 0 &&
                <Alert variant='success'>작업에 성공했습니다.</Alert>
            }
            {workState === 1 &&
                <Alert variant='danger'>작업을 위한 권한이 부족합니다.</Alert>
            }
            {workState === 2 &&
                <Alert variant='danger'>IonID가 존재하지 않습니다.</Alert>
            }
            {workState === 3 &&
                <Alert variant='danger'>작업을 처리하지 못했습니다.</Alert>
            }
        </>
    );
}

export default RemoveGrade;