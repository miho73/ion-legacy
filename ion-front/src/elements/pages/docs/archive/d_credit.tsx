import React from 'react';
import { Table } from 'react-bootstrap';
import { Link } from 'react-router-dom';

function Dcredit() {
    return (
        <div className='vstack'>
            <h2>Developers</h2>
            <div className='table-cover'>
                <Table className='table-narrow'>
                    <tbody>
                        <tr>
                            <td className='fw-bold'>현창운 (30th)</td>
                            <td>Ion 개발 담당</td>
                        </tr>
                        <tr>
                            <td className='fw-bold'>이승원 (30th)</td>
                            <td>서버 구축</td>
                        </tr>
                        <tr>
                            <td className='fw-bold'>이나경 (30th)</td>
                            <td>디자인</td>
                        </tr>
                    </tbody>
                </Table>
            </div>
            <p>Ion을 위해 가능한 모든 지원을 해주신 이지현 정보 선생님에게 특별한 감사의 말씀을 드립니다.</p>
            <p>Project Ion GitHub: <Link to='https://github.com/miho73/ion' className='text-decoration-none' target='_black'>Link</Link></p>
        </div>
    );
}

export default Dcredit
