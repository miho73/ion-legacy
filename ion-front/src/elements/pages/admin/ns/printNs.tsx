import React, { useState } from 'react';
import jsPDF from "jspdf";
import "jspdf-autotable";
import { Alert, Button, Container, FormSelect, InputGroup, Row, Table } from 'react-bootstrap';
import axios from 'axios';
import font from '../../../types/SpoqaHanSansNeo-normal';

function PrintNs() {
    const [data, setData] = useState<any[]>([]);
    const [grade, setGrade] = useState(1);
    const [date, setDate] = useState('d');
    const [workState, setWorkState] = useState(-1);

    function exportPdf() {
        var doc = new jsPDF('portrait', 'mm', 'a4');

        doc.addFileToVFS("SpoqaHanSansNeo.ttf", font);
        doc.addFont("SpoqaHanSansNeo.ttf", "SpoqaHanSansNeo", "normal");
        doc.setFont("SpoqaHanSansNeo");

        doc.setFontSize(20);
        doc.text('면불 일지', 10, 20);
        doc.setFontSize(8);
        doc.text(date, 15, 26);

        doc.setFontSize(10);
        doc.autoTable({
            html: '#prt',
            headStyles: { halign: "center", valign: "middle" },
            startX: 15,
            startY: 30,
            margin: { left: 15, top: 0, right: 15 },
            styles: { font: "SpoqaHanSansNeo", fontStyle: "normal" }
        });

        doc.save(`면불일지 ${date}.pdf`);
    }

    function createTable() {
        axios.get('/manage/api/ns/print', {
            params: {grade: grade}
        })
        .then(res => {
            setData(res.data['result']['ns']);
            setDate(res.data['result']['qtime']);
            setWorkState(0);
        })
        .catch(err => {
            switch(err.response.data['result']) {
                case 1:
                    setWorkState(1);
                    break;
                default:
                    setWorkState(2);
            }
        });
    }
    
    let rr: any[] = [];
    if(workState === 0) {
        data.forEach(e => {
            rr.push(
                <tr>
                    <td>{e.code}</td>
                    <td>{e.name}</td>
                    <td>{e.n8}</td>
                    <td>{e.n1}</td>
                    <td>{e.n2}</td>
                </tr>
            )
        });
    }

    return (
        <Row className='my-3'>
            <h2>면불 목록 출력</h2>
            <InputGroup className='w-25'>
                <FormSelect value={grade} onChange={e => setGrade(Number.parseInt(e.target.value))}>
                    <option value={1}>1학년</option>
                    <option value={2}>2학년</option>
                    <option value={3}>3학년</option>
                </FormSelect>
                <Button onClick={createTable}>로드</Button>
                <Button onClick={exportPdf} disabled={workState !== 0}>인쇄</Button>
            </InputGroup>
            <Container className='my-3'>
                { workState === 0 &&
                    <>
                        <Table id='prt'>
                            <thead>
                                <tr>
                                    <th>학번</th>
                                    <th>이름</th>
                                    <th>8면</th>
                                    <th>1면</th>
                                    <th>2면</th>
                                </tr>
                            </thead>
                            <tbody>{rr}</tbody>
                        </Table>
                        <p>{date}</p>
                    </>
                }
                { workState === 1 &&
                    <Alert variant='danger'>작업에 필요한 권한이 없습니다.</Alert>
                }
                { workState === 2 &&
                    <Alert variant='danger'>작업을 처리하지 못했습니다.</Alert>
                }
            </Container>
        </Row>
    );
}

export default PrintNs;

// TODO: 장소별로 면불 보기 : 필터의 확장