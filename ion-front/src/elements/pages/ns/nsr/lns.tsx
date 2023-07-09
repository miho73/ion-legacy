import React, { useEffect, useState } from 'react';
import LnsSeat from './lns-seat';
import axios from 'axios';

const sigMapping = ['A', 'B', 'C', 'D', 'E', 'F']

function LnsRoomSelect(props) {
    if(props.lnsState === 2) {
        return (
            <p>노트북 면불실 자리를 확인하지 못했습니다.</p>
        );
    }
    else if(props.lnsState === 0) {
        return <p>잠시만요</p>;
    }

    const pLst = props.seatLst;
    const selected = pLst[props.nst];

    let map: any[] = [];
    map.push(
        <p className='w-75 mx-auto my-2 text-center border p-3 rounded' key={0}>면학실</p>
    );
    for(let i=0; i<3; i++) {
        let row: any[] = [];
        for(let j=1; j<=2; j++) {
            let seat: any[] = [];
            for(let k=1; k<=6; k++) {
                // 32: C의 2번
                let key = i*20+j*10+k;
                let cp = sigMapping[i*2+j-1]+k;
                if(selected.hasOwnProperty(cp)) {
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
                }
                else {
                    seat.push(
                        <LnsSeat
                            sn={k}
                            rev={false}
                            key={key}
                            value={key===props.selected}
                            onSelected={() => props.setSelected(key)}
                        />
                    );
                }
            }
            row.push(
                <div className='hstack mx-4'>
                    <div className='vstack row btn-group-vertical'>{seat.slice(0, 3)}</div>
                    <div className='p-5 m-0 border border-dark rounded fs-4 h-100 d-flex align-items-center mx-4'>{sigMapping[i*2+j-1]}</div>
                    <div className='vstack row btn-group-vertical'>{seat.splice(3, 6)}</div>
                </div>
            )
        }
        map.push(
            <div className='d-flex justify-content-evenly my-4' key={i+1}>{row}</div>
        )
    }
    return (
        <>{map}</>
    )
}

export default LnsRoomSelect;