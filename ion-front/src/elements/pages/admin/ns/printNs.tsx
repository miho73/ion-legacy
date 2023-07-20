import React, { useState } from 'react';
import jsPDF from "jspdf";
import "jspdf-autotable";
import { Alert, Button, Container, Form, FormCheck, FormGroup, FormSelect, InputGroup, Row, Table } from 'react-bootstrap';
import axios from 'axios';
import font from '../../../types/SpoqaHanSansNeo-normal';

function PrintNs() {
    const [data, setData] = useState<any[]>([]);
    const [grade, setGrade] = useState(1);
    const [date, setDate] = useState('d');
    const [includeDenied, setIncludeDenied ] = useState(false);
    const [workState, setWorkState] = useState(-1);

    function exportPdf() {
        var doc = new jsPDF('portrait', 'mm', 'a4');

        doc.addFileToVFS("SpoqaHanSansNeo.ttf", font);
        doc.addFont("SpoqaHanSansNeo.ttf", "SpoqaHanSansNeo", "normal");
        doc.setFont("SpoqaHanSansNeo");

        doc.setFontSize(20);
        doc.text('면학 지도 일지', 10, 20);
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

        doc.save(`면학 지도 일지 ${date}.pdf`);
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
            switch(err.response?.data['result']) {
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
                    { includeDenied &&
                        <>
                            { e.n8 !== null &&
                                <td className={e.n8.a ? 'table-success' : 'table-danger'}>{e.n8.c}</td>
                            }
                            { e.n8 === null &&
                                <td></td>
                            }
                            { e.n1 !== null &&
                                <td className={e.n1.a ? 'table-success' : 'table-danger'}>{e.n1.c}</td>
                            }
                            { e.n1 === null &&
                                <td></td>
                            }
                            { e.n2 !== null &&
                                <td className={e.n2.a ? 'table-success' : 'table-danger'}>{e.n2.c}</td>
                            }
                            { e.n2 === null &&
                                <td></td>
                            }
                        </>
                    }
                    { !includeDenied &&
                        <>
                            { (e.n8 !== null && e.n8.a) &&
                                <td>{e.n8.c}</td>
                            }
                            { (e.n8 === null || !e.n8.a) &&
                                <td></td>
                            }
                            { (e.n1 !== null && e.n1.a) &&
                                <td>{e.n1.c}</td>
                            }
                            { (e.n1 === null || !e.n1.a) &&
                                <td></td>
                            }
                            { (e.n2 !== null && e.n2.a) &&
                                <td>{e.n2.c}</td>
                            }
                            { (e.n2 === null || !e.n2.a) &&
                                <td></td>
                            }
                        </>
                    }
                </tr>
            )
        });
    }

    return (
        <Row className='my-3'>
            <h2>면학 불참 목록</h2>
            <FormGroup>
                <InputGroup className='w-25 mgw'>
                    <FormSelect value={grade} onChange={e => setGrade(Number.parseInt(e.target.value))}>
                        <option value={1}>1학년</option>
                        <option value={2}>2학년</option>
                        <option value={3}>3학년</option>
                    </FormSelect>
                    <Button onClick={createTable}>로드</Button>
                    <Button onClick={exportPdf} disabled={workState !== 0 || includeDenied}>인쇄</Button>
                </InputGroup>
                <Form.Check
                    label='승인되지 않은 요청 포함'
                    className='mt-1'
                    id='drq'
                    checked={includeDenied}
                    onChange={e => setIncludeDenied(e.target.checked)}
                />
            </FormGroup>
            <Container className='my-3'>
                { workState === 0 &&
                    <div className='table-cover'>
                        <Table id='prt'>
                            <thead>
                                <tr>
                                    <th>학번</th>
                                    <th>이름</th>
                                    <th>8면학</th>
                                    <th>1면학</th>
                                    <th>2면학</th>
                                </tr>
                            </thead>
                            <tbody>{rr}</tbody>
                        </Table>
                        <p>{date}</p>
                    </div>
                }
                { workState === 1 &&
                    <Alert variant='danger'>권한이 부족합니다.</Alert>
                }
                { workState === 2 &&
                    <Alert variant='danger'>문제가 발생했습니다.</Alert>
                }
            </Container>
        </Row>
    );
}

export default PrintNs;
