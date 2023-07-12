import React from "react";
import { Button } from "react-bootstrap";

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

    function editNs() {

    }

    return (
        <tr>
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
            <td className={cla}>{props.status}</td>
            <td className="d-flex gap-2">
                <Button variant='outline-secondary' className='d-flex align-content-center p-2' title='삭제' onClick={deleteNs}>
                    <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" className="bi bi-x-lg" viewBox="0 0 16 16">
                        <path d="M2.146 2.854a.5.5 0 1 1 .708-.708L8 7.293l5.146-5.147a.5.5 0 0 1 .708.708L8.707 8l5.147 5.146a.5.5 0 0 1-.708.708L8 8.707l-5.146 5.147a.5.5 0 0 1-.708-.708L7.293 8 2.146 2.854Z"/>
                    </svg>
                </Button>
                <Button variant='outline-secondary' className='d-flex align-content-center p-2' title='수정' onClick={editNs}>
                    <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" className="bi bi-pencil" viewBox="0 0 16 16">
                        <path d="M12.146.146a.5.5 0 0 1 .708 0l3 3a.5.5 0 0 1 0 .708l-10 10a.5.5 0 0 1-.168.11l-5 2a.5.5 0 0 1-.65-.65l2-5a.5.5 0 0 1 .11-.168l10-10zM11.207 2.5 13.5 4.793 14.793 3.5 12.5 1.207 11.207 2.5zm1.586 3L10.5 3.207 4 9.707V10h.5a.5.5 0 0 1 .5.5v.5h.5a.5.5 0 0 1 .5.5v.5h.293l6.5-6.5zm-9.761 5.175-.106.106-1.528 3.821 3.821-1.528.106-.106A.5.5 0 0 1 5 12.5V12h-.5a.5.5 0 0 1-.5-.5V11h-.5a.5.5 0 0 1-.468-.325z"/>
                    </svg>
                </Button>
            </td>
        </tr>
    )
}

export default NsState;