import axios from "axios";
import React, { useState } from "react";
import { Alert, Button, Form, InputGroup } from "react-bootstrap";

function IonIdActivation() {
    const [id, setId] = useState('');
    const [result, setResult] = useState<any[]>([]);

    function setActiveState(mode) {
        axios.patch('/manage/api/ionid/active/patch', {
            id: id,
            ac: mode
        })
        .then(res => {
            const r = res.data['result'];
            setResult([
                0,
                `"${r['sub']}"의 활성화 상태를 "${r['act']}"로 변경했습니다.`
            ]);
        })
        .catch(err => {
            let msg;
            switch(err.response?.data['result']) {
                case 1:
                    msg = '작업을 위한 권한이 부족합니다.'
                    break;
                case 2:
                    msg = '필수 파라미터가 없습니다.'
                    break;
                case 3:
                    msg = '올바르지 않은 상태입니다.'
                    break;
                case 4:
                    msg = 'IonID가 존재하지 않습니다.'
                    break;
                case 5:
                    msg = '자신은 수정할 수 없습니다.'
                    break;
                default:
                    msg = '작업을 처리하지 못했습니다.'
                    break;
            }
            setResult([1, msg])
        });
    }

    return (
        <Form className="w-50 mgw">
            <InputGroup className="mb-3">
                <Form.Control
                    type="text"
                    placeholder="IonID"
                    value={id}
                    onChange={e => setId(e.target.value)}
                />
                <Button variant="warning" onClick={() => setActiveState(0)}>Inactivate</Button>
                <Button variant="success" onClick={() => setActiveState(1)}>Activate</Button>
                <Button variant="danger" onClick={() => setActiveState(2)}>Ban</Button>
            </InputGroup>
            {result[0] === 0 &&
                <Alert variant="success">{result[1]}</Alert>
            }
            {result[0] === 1 &&
                <Alert variant="danger">{result[1]}</Alert>
            }
        </Form>
    );
}

export default IonIdActivation;