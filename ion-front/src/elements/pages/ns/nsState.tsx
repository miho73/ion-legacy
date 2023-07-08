import React from "react";

const NsState = function(props) {
    let cla = '';
    switch(props.status) {
        case 'APPROVED':
            cla = 'table-success';
            break;
        case 'DENIED':
            cla = 'table-danger';
            break;
    }

    let time;
    switch(props.name) {
        case 'N8':
            time = '8면'
            break;
        case 'N1':
            time = '1면'
            break;
        case 'N2':
            time = '2면'
            break;
    }

    function deleteNs() {
        props.setTargetNs([props.name, time]);
        props.showDeleteConfirm();
    }

    return (
        <tr className={cla}>
            <th>{time}</th>
            <td>{props.place}</td>
            <td>{props.superviser}</td>
            <td>{props.reason}</td>
            {props.lnsReq &&
                <td>{props.seat}</td>
            }
            {!props.lnsReq &&
                <td>-</td>
            }
            <td>{props.status}</td>
            <td>
                <button type='button' className='btn btn-outline-secondary d-flex align-content-center p-2' title='삭제' onClick={deleteNs}>
                    <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" className="bi bi-x-lg" viewBox="0 0 16 16">
                        <path d="M2.146 2.854a.5.5 0 1 1 .708-.708L8 7.293l5.146-5.147a.5.5 0 0 1 .708.708L8.707 8l5.147 5.146a.5.5 0 0 1-.708.708L8 8.707l-5.146 5.147a.5.5 0 0 1-.708-.708L7.293 8 2.146 2.854Z"/>
                    </svg>
                </button>
            </td>
        </tr>
    )
}

export default NsState;