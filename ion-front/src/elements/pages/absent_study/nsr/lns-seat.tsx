import React, { useState } from 'react';

/**
 * @param props sn: Seat number(1-6) rev: Reserved(true/false) revName: Name(str) revScd: sCode(int)
 * @returns 
 */
function LnsSeatSelect(props) {
    let str = '', tstr = '', bstr = '';
    if(props.value) {
        str = ' bg-secondary border-secondary';
        tstr=' text-light';
    }

    return (
        <>
            {!props.rev &&
                <button type='button' className={'btn btn-outline-dark'+str} style={{width: '60px', height: '60px'}} onClick={props.onSelected}>
                    <span className={'fs-4 p-2'+tstr}>{props.sn}</span>
                </button>
            }
            {props.rev &&
                <button type='button' className='btn btn-outline-dark' style={{width: '60px', height: '60px'}} disabled>            
                    <div className='vstack'>
                        <span>{props.revScd}</span>
                        <span>{props.revName}</span>
                    </div>
                </button>
            }
        </>
    )
}

export default LnsSeatSelect;