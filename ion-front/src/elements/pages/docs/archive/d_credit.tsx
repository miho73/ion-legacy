import React from 'react';
import { Table } from 'react-bootstrap';
import { Link } from 'react-router-dom';

function Dcredit() {
    return (
        <div className='vstack'>
            <h2>Developers</h2>
            <Table>
                <tbody>
                    <tr>
                        <td className='fw-bold'>현창운 (30th)</td>
                        <td>Ion 개발</td>
                    </tr>
                    <tr>
                        <td className='fw-bold'>이승원 (30th)</td>
                        <td>서버 구축</td>
                    </tr>
                    <tr>
                        <td className='fw-bold'>이나경 (30th)</td>
                        <td>디자인 자문</td>
                    </tr>
                </tbody>
            </Table>
            <p>Ion Project GitHub: <Link to='https://github.com/miho73/ion' target='_black'>Link</Link></p>
            <hr/>
        </div>
    );
}

export default Dcredit
