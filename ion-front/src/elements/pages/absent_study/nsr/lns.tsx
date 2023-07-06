import React, { useState } from 'react';
import LnsSeat from './lns-seat';

const sigMapping = ['A', 'B', 'C', 'D', 'E', 'F']

function LnsRoomSelect(props) {
    var map: any[] = [];
    map.push(
        <p className='w-75 mx-auto my-2 text-center border p-3 rounded' key={0}>면학실</p>
    );
    for(let i=0; i<3; i++) {
        var row: any[] = [];
        for(let j=0; j<2; j++) {
            var seat: any[] = [];
            for(let k=1; k<=6; k++) {
                // 32: C의 2번
                let key = i*20+j*10+k;
                seat.push(<LnsSeat sn={k} rev={false} revName='a' revScd={1333} key={key} value={key===props.selected} onSelected={() => props.setSelected(i*20+j*10+k)}/>);
            }
            row.push(
                <div className='hstack mx-4'>
                    <div className='vstack row btn-group-vertical'>{seat.slice(0, 3)}</div>
                    <div className='p-5 m-0 border border-dark rounded fs-4 h-100 d-flex align-items-center mx-4'>{sigMapping[i*2+j]}</div>
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