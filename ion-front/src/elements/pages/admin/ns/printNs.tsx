import React, {useEffect, useState} from 'react';
import jsPDF from "jspdf";
import "jspdf-autotable";
import {
    Alert,
    Button,
    Container,
    Form,
    FormCheck,
    FormGroup,
    FormSelect,
    InputGroup,
    Row,
    Stack,
    Table
} from 'react-bootstrap';
import axios from 'axios';
import {changeBit, getBit} from "../../../service/bitmask";
import {API_PREFIX} from "../../../service/apiUrl";

function PrintNs() {
    const [data, setData] = useState<any[]>([]);
    const [grade, setGrade] = useState<number>(1);
    const [date, setDate] = useState<string>('');
    const [includeDenied, setIncludeDenied] = useState<boolean>(false);
    const [workState, setWorkState] = useState<number>(-1);
    const [filterByClass, setFilterByClass] = useState<number>(15);

    function exportPdf() {
        axios.get(API_PREFIX+'/static/font/SpoqaHanSansNeo-normal.b64')
            .then(res => {
                exportPdfAfter(res);
            })
            .catch(err => {
                console.error(err);
                setWorkState(3);
            });
    }

    function exportPdfAfter(font: string) {
        var doc = new jsPDF('portrait', 'mm', 'a4');

        doc.addFileToVFS("SpoqaHanSansNeo.ttf", font);
        doc.addFont("SpoqaHanSansNeo.ttf", "SpoqaHanSansNeo", "normal");
        doc.setFont("SpoqaHanSansNeo");

        doc.setFontSize(18);
        doc.text('면학 지도 일지', 10, 13);
        doc.setFontSize(10);
        doc.text(date, 10, 18);

        doc.setFontSize(10);
        doc.autoTable({
            html: '#prt',
            headStyles: {halign: "center", valign: "middle", fillColor: [59, 59, 59]},
            tableLineColor: [59, 59, 59],
            tableLineWidth: 0.1,
            startX: 10,
            startY: 20,
            margin: {left: 10, top: 10, right: 10, bottom: 10},
            styles: {font: "SpoqaHanSansNeo", fontStyle: "normal"},
        });

        doc.save(`면학 지도 일지 ${date}.pdf`);
    }

    function createTable() {
        axios.get(API_PREFIX+'/manage/api/ns/print', {
            params: {grade: grade}
        })
            .then(res => {
                setData(res.data['result']['ns']);
                setDate(res.data['result']['qtime']);
                setWorkState(0);
            })
            .catch(err => {
                switch (err.response?.data['result']) {
                    case 1:
                        setWorkState(1);
                        break;
                    default:
                        setWorkState(2);
                }
            });
    }

    function copy(str: string) {
        navigator.clipboard.writeText(str);
    }

    let rr: any[] = [];
    if (workState === 0) {
        data.forEach(e => {
            let clas = (e.code-e.code%100)/100-10*(grade);
            if(getBit(filterByClass, clas-1) === 0) return;
            rr.push(
                <tr>
                    <td>{e.code}</td>
                    <td>{e.name}</td>
                    {includeDenied &&
                        <>
                            {e.n8 !== null &&
                                <td className={e.n8.a ? 'table-success' : 'table-danger'}>
                                    <a onClick={()=>copy(e.n8.c)}>{e.n8.c}</a>
                                </td>
                            }
                            {e.n8 === null &&
                                <td></td>
                            }
                            {e.n1 !== null &&
                                <td className={e.n1.a ? 'table-success' : 'table-danger'}>
                                    <a onClick={()=>copy(e.n1.c)}>{e.n1.c}</a>
                                </td>
                            }
                            {e.n1 === null &&
                                <td></td>
                            }
                            {e.n2 !== null &&
                                <td className={e.n2.a ? 'table-success' : 'table-danger'}>
                                    <a onClick={()=>copy(e.n8.c)}>{e.n2.c}</a>
                                </td>
                            }
                            {e.n2 === null &&
                                <td></td>
                            }
                        </>
                    }
                    {!includeDenied &&
                        <>
                            {(e.n8 !== null && e.n8.a) &&
                                <td>
                                    <a title={'Copy'} className={'ns-lst-cpy'} onClick={()=>copy(e.n8.c)}>{e.n8.c}</a>
                                </td>
                            }
                            {(e.n8 === null || !e.n8.a) &&
                                <td></td>
                            }
                            {(e.n1 !== null && e.n1.a) &&
                                <td>
                                    <a title={'Copy'} className={'ns-lst-cpy'} onClick={()=>copy(e.n1.c)}>{e.n1.c}</a>
                                </td>
                            }
                            {(e.n1 === null || !e.n1.a) &&
                                <td></td>
                            }
                            {(e.n2 !== null && e.n2.a) &&
                                <td>
                                    <a title={'Copy'} className={'ns-lst-cpy'} onClick={()=>copy(e.n2.c)}>{e.n2.c}</a>
                                </td>
                            }
                            {(e.n2 === null || !e.n2.a) &&
                                <td></td>
                            }
                        </>
                    }
                </tr>
            );
        });
    }

    useEffect(() => {
        let c = localStorage.getItem('nlfc');
        if(c === null) {
            localStorage.setItem('nlfc', 15);
            setFilterByClass(15);
        } else {
            setFilterByClass(Number.parseInt(c));
        }

        let g = localStorage.getItem('nlg');
        if(g === null) {
            localStorage.setItem('nlg', '1');
            setGrade(1);
        }
        else {
            setGrade(Number.parseInt(g));
        }
    }, []);
    function updateFilterByClass(clas: number) {
        let c = 0;
        c = changeBit(filterByClass, clas);
        setFilterByClass(c);
        localStorage.setItem('nlfc', c);
    }
    function updateGrade(grade: number) {
        setGrade(Number.parseInt(grade));
        localStorage.setItem("nlg", grade);
    }

    return (
        <Row className='my-3'>
            <h2>면학 불참 목록</h2>
            <FormGroup>
                <InputGroup className='w-25 mgw'>
                    <FormSelect value={grade} onChange={e => updateGrade(e.target.value)}>
                        <option value={1}>1학년</option>
                        <option value={2}>2학년</option>
                        <option value={3}>3학년</option>
                    </FormSelect>
                    <Button onClick={createTable}>로드</Button>
                </InputGroup>
                <Form.Check
                    label='승인되지 않은 요청 포함'
                    className='mt-1'
                    id='drq'
                    checked={includeDenied}
                    onChange={e => setIncludeDenied(e.target.checked)}
                />
                <Stack direction={'horizontal'} gap={3}>
                    <FormCheck
                        label={'1반'}
                        id={'clas-1'}
                        checked={getBit(filterByClass, 0) === 1}
                        onChange={() => updateFilterByClass(0)}
                    />
                    <FormCheck
                        label={'2반'}
                        id={'clas-2'}
                        checked={getBit(filterByClass, 1) === 1}
                        onChange={() => updateFilterByClass(1)}
                    />
                    <FormCheck
                        label={'3반'}
                        id={'clas-3'}
                        checked={getBit(filterByClass, 2) === 1}
                        onChange={() => updateFilterByClass(2)}
                    />
                    <FormCheck
                        label={'4반'}
                        id={'clas-4'}
                        checked={getBit(filterByClass, 3) === 1}
                        onChange={() => updateFilterByClass(3)}
                    />
                </Stack>
            </FormGroup>
            <Container className='my-3'>
                {workState === 0 &&
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
                {workState === 1 &&
                    <Alert variant='danger'>권한이 부족합니다.</Alert>
                }
                {workState === 2 &&
                    <Alert variant='danger'>문제가 발생했습니다.</Alert>
                }
                {workState === 3 &&
                    <Alert variant='danger'>PDF를 만들지 못했습니다.</Alert>
                }
            </Container>
        </Row>
    );
}

export default PrintNs;
