import axios from "axios";
import React, { useState } from "react";
import { Form, Button, InputGroup, Table, Alert } from "react-bootstrap";

function QueryIonId() {
    const [id, setId] = useState('');
    const [usr, setUsr] = useState<any[]>([]);
    const [gs, setGs] = useState(0);

    function query() {
        axios.get('/manage/api/ionid/get', {
            params: {id: id}
        })
        .then(res => {
            const r = res.data['result'];
            setUsr([
                r['ui'],
                r['na'],
                r['gr']*1000+r['cl']*100+r['sc'],
                r['sf'],
                r['id'],
                r['ll'],
                r['jd'],
                r['st'],
                r['pr']
            ]);
            setGs(1);
        })
        .catch(err => {
            switch(err.response?.data['result']) {
                case 1:
                    setGs(2);
                    break;
                case 2:
                    setGs(3);
                    break;
                default:
                    setGs(4);
                    break;
            }
        });
    }

    return (
        <>
        <Form className="w-25 mgw">
            <InputGroup className="mb-3">
                <InputGroup.Text>IonID</InputGroup.Text>
                <Form.Control
                    type="text"
                    placeholder="IonID"
                    value={id}
                    onChange={e => setId(e.target.value)}
                />
                <Button variant="outline-primary" onClick={query}>조회</Button>
            </InputGroup>
            {gs === 1 &&
                <Table>
                    <thead>
                        <tr>
                            <th>uid</th>
                            <th>name</th>
                            <th>scode</th>
                            <th>scode_cflag</th>
                            <th>id</th>
                            <th>last_login</th>
                            <th>join_date</th>
                            <th>status</th>
                            <th>privilege</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr>
                            <td>{usr[0]}</td>
                            <td>{usr[1]}</td>
                            <td>{usr[2]}</td>
                            <td>{usr[3] ? 'true' : 'false'}</td>
                            <td>{usr[4]}</td>
                            <td>{usr[5]}</td>
                            <td>{usr[6]}</td>
                            <td>{usr[7]}</td>
                            <td>{usr[8]}</td>
                        </tr>
                    </tbody>
                </Table>
            }
            { gs === 2 &&
                <Alert variant="danger">작업을 위한 권한이 부족합니다.</Alert>
            }
            { gs === 3 &&
                <Alert variant="danger">IonID가 존재하지 않습니다.</Alert>
            }
            { gs === 4 &&
                <Alert variant="danger">작업을 처리하지 못했습니다.</Alert>
            }
            { gs === 5 &&
                <Alert variant="danger">작업을 처리하지 못했습니다.</Alert>
            }
        </Form>
        </>
    );
}

export default QueryIonId;