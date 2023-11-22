import React, {useState} from 'react';
import LnsSeat from './lns-seat';
import {changeBit, getBit} from '../../../service/bitmask';
import {Button, Form, Stack} from 'react-bootstrap';

const sigMapping = ['A', 'B', 'C', 'D', 'E', 'F']

function LnsRoomSelect(props) {
    const [findCommon, setFindCommon] = useState<number>(0);

    function changeFCF(place) {
        setFindCommon(changeBit(findCommon, place));
    }

    if (props.lnsState === 2) {
        return (
            <Stack>
                <p className='mb-2'>노트북 면불실 자리를 확인하지 못했습니다.</p>
                <Button variant='outline-primary' className='w-fit' onClick={props.reloadFunc}>다시 시도</Button>
            </Stack>
        );
    } else if (props.lnsState === 0) {
        return <p>잠시만요</p>;
    }

    const pLst = props.seatLst;
    const selected = pLst[props.nst];

    let commonOk: string[] = [];
    // find common pre-process
    if (findCommon !== 0) {
        let n8 = getBit(findCommon, 0), n1 = getBit(findCommon, 1), n2 = getBit(findCommon, 2);

        for (let a = 0; a < 6; a++) {
            for (let s = 1; s <= 6; s++) {
                let code = sigMapping[a] + s;
                let ok =
                    (!(n8 && pLst[0].hasOwnProperty(code))) &&
                    (!(n1 && pLst[1].hasOwnProperty(code))) &&
                    (!(n2 && pLst[2].hasOwnProperty(code)));

                if (ok) commonOk.push(code);
            }
        }
    }

    let map: any[] = [];
    map.push(
        <p className='w-75 mx-auto my-2 text-center border p-3 rounded' key={0}>면학실</p>
    );
    for (let i = 0; i < 3; i++) { // ROW 1, 2, 3
        let row: any[] = [];
        for (let j = 1; j <= 2; j++) { // COL 1, 2
            let seat: any[] = [];
            for (let k = 1; k <= 6; k++) { // SEAT 1 ~ 6
                // 32: C의 2번
                let key = i * 20 + j * 10 + k;
                let cp = sigMapping[i * 2 + j - 1] + k;
                if (selected.hasOwnProperty(cp)) {
                    const rev = selected[cp];
                    seat.push(
                        <LnsSeat
                            sn={k}
                            rev={true}
                            revName={rev.name}
                            revScd={rev.scode}
                            key={key}
                            value={false}
                        />
                    )
                } else {
                    seat.push(
                        <LnsSeat
                            sn={k}
                            rev={false}
                            key={key}
                            value={key === props.selected}
                            onSelected={() => props.setSelected(key)}
                            common={commonOk.includes(cp)}
                        />
                    );
                }
            }
            row.push(
                <Stack direction='horizontal' gap={4}>
                    <div className='vstack row btn-group-vertical'>{seat.slice(0, 3)}</div>
                    <div className='p-5 border border-dark rounded fs-4 h-100 d-flex align-items-center'>{sigMapping[i * 2 + j - 1]}</div>
                    <div className='vstack row btn-group-vertical'>{seat.slice(3, 6)}</div>
                </Stack>
            )
        }
        map.push(
            <div className='d-flex justify-content-evenly my-4' key={i + 1}>{row}</div>
        )
    }
    return (
        <>
            {map}
            <Stack className='mt-3'>
                <div className='border p-2'>
                    <p className='mb-1 fw-bold'>공통자리 찾기</p>
                    <Stack direction='horizontal' gap={3}>
                        <Form.Check id='csf8' checked={getBit(findCommon, 0) === 1} onChange={() => changeFCF(0)}
                                    label='8면'/>
                        <Form.Check id='csf1' checked={getBit(findCommon, 1) === 1} onChange={() => changeFCF(1)}
                                    label='1면'/>
                        <Form.Check id='csf2' checked={getBit(findCommon, 2) === 1} onChange={() => changeFCF(2)}
                                    label='2면'/>
                    </Stack>
                </div>
            </Stack>
        </>
    )
}

export default LnsRoomSelect;