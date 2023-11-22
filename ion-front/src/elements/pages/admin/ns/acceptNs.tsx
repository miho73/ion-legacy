import axios from "axios";
import React, {useEffect, useState} from "react";
import {Alert, Button, ButtonGroup, Row, Table} from "react-bootstrap";
import {API_PREFIX} from "../../../service/apiUrl";

function NsReq(props) {
    const id = props.id;

    const accept = props.accept;
    const deny = props.deny;

    let cc, sh;
    if (props.status === 'APPROVED') {
        cc = 'table-success text-success text-center';
        sh = 'APPROVED';
    }
    if (props.status === 'DENIED') {
        cc = 'table-danger text-danger text-center';
        sh = 'DENIED';
    }
    if (props.status === 'REQUESTED') {
        cc = 'text-center';
        sh = 'REQUESTED';
    }
    if (props.status === 'NO_SUPERVISOR') {
        cc = 'table-warning text-danger text-center';
        sh = 'NO SUPERVISOR';
    }

    return (
        <tr>
            <td>{props.name}</td>
            <td>{props.scode}</td>
            <td>{props.time}</td>
            <td>{props.place}</td>
            <td>{props.super}</td>
            <td>{props.reason}</td>
            <td className={cc}>{sh}</td>
            <td>
                <ButtonGroup>
                    <Button variant='outline-success' onClick={() => accept(id)}>승인</Button>
                    <Button variant='outline-danger' onClick={() => deny(id)}>거절</Button>
                </ButtonGroup>
            </td>
        </tr>
    );
}

function AcceptNs() {
    const [ws, setWs] = useState(0);
    const [nsLst, setNsLst] = useState([]);
    const [date, setDate] = useState('');

    useEffect(() => {
        loadNs();
    }, []);

    function loadNs() {
        axios.get(API_PREFIX+'/manage/api/ns/get')
            .then(res => {
                const data = res.data['result'];
                setDate(data['date']);
                setNsLst(data['nss']);
            })
            .catch(err => {
                switch (err.response?.data['result']) {
                    case 1:
                        setWs(1);
                        break;
                    default:
                        setWs(-1);
                        break;
                }
            })
    }

    const rLst: any[] = [];
    if (ws === -1) {
        rLst.push(
            <tr className='table-danger'>
                <td colSpan={8}>문제가 발생했습니다.</td>
            </tr>
        );
    } else if (ws === 1) {
        rLst.push(
            <tr className='table-danger'>
                <td colSpan={8}>권한이 부족합니다.</td>
            </tr>
        );
    } else if (nsLst.length === 0) {
        rLst.push(
            <tr>
                <td colSpan={8}>신청된 면학 불참이 없습니다.</td>
            </tr>
        );
    } else {
        nsLst.forEach(e => {
            if (e['v']) {
                rLst.push(
                    <NsReq
                        id={e['id']}
                        name={e['name']}
                        time={e['time']}
                        scode={e['rscode']}
                        place={e['place']}
                        super={e['super']}
                        reason={e['reason']}
                        status={e['status']}
                        accept={(id) => ctrl(id, true)}
                        deny={(id) => ctrl(id, false)}
                    />
                );
            } else {
                rLst.push(
                    <tr className='table-warning'>
                        <td colSpan={8}>유효하지 않은 신청입니다.</td>
                    </tr>
                )
            }
        });
    }

    function ctrl(id, accept) {
        axios.patch(API_PREFIX+'/manage/api/ns/accept', {
            id: id,
            ac: accept
        })
            .then(res => {
                loadNs();
            })
            .catch(err => {
                switch (err.response?.data['result']) {
                    case 1:
                        setWs(1);
                        break;
                    case 2:
                        setWs(2);
                        break;
                    case 3:
                        setWs(3);
                        break;
                    default:
                        setWs(-1);
                        break;
                }
            });
    }

    return (
        <Row className="my-3">
            <h2 className="mb-3">면학 불참 승인</h2>
            <p>나에게 요청된 신청만 승인할 수 있습니다.</p>
            <div className='table-cover'>
                <Table>
                    <thead>
                    <tr>
                        <th>이름</th>
                        <th>학번</th>
                        <th>면학</th>
                        <th>장소</th>
                        <th>담당교사</th>
                        <th>사유</th>
                        <th>상태</th>
                        <th>승인</th>
                    </tr>
                    </thead>
                    <tbody>{rLst}</tbody>
                </Table>
            </div>
            <Button variant='outline-primary w-fit' onClick={loadNs}>새로고침</Button>
            <p className='my-2'>{date}</p>
            {ws === 2 &&
                <Alert variant='danger w-fit'>문제가 발생했습니다.</Alert>
            }
            {ws === 3 &&
                <Alert variant='danger w-fit'>해당 신청이 존재하지 않습니다.</Alert>
            }
        </Row>
    )
}

export default AcceptNs;