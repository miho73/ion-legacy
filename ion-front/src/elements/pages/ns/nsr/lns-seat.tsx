import React, { useState } from 'react';

/**
 * @param props sn: Seat number(1-6) rev: Reserved(true/false) revName: Name(str) revScd: sCode(int)
 * @returns 
 */
function LnsSeatSelect(props) {
    let str = '', tstr = '';
    if(props.value) {
        str = ' bg-secondary border-secondary';
        tstr=' text-light';
    }

    return (
        <>
            {!props.rev &&
                <button type='button' className={'btn btn-outline-dark lns-seat '+str} onClick={props.onSelected}>
                    <span className={'fs-4 p-2'+tstr}>{props.sn}</span>
                </button>
            }
            {props.rev &&
                <div className='btn btn-outline-dark p-0 lns-seat'>
                    <div className='h-100 d-flex flex-column justify-content-center align-content-center'>
                        <span className='sel-seat fw-bold'>{props.revScd}</span>
                        <span className='sel-seat fw-bold'>{props.revName}</span>
                    </div>
                </div>
            }
        </>
    )
}

export default LnsSeatSelect;