import React from 'react'
import {Container, Row} from 'react-bootstrap';
import Promote from './promote';

function BulkActions() {
    return (
        <Container className="p-3">
            <p className='fs-3 fw-bold text-danger m-0'>DANGER ZONE</p>
            <p className='fs-6'>이 기능들은 학년이 끝나고 겨울방학기간동안 사용하는걸 권고합니다. 아래 작업에는 SUPERVISOR 권한이 필요합니다.</p>
            <hr/>
            <Row className="my-3">
                <h2 className="mb-3">진급</h2>
                <Promote/>
            </Row>
        </Container>
    );
}

export default BulkActions;