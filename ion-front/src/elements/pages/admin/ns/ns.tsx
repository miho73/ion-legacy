import React, { useState } from 'react';
import { Container } from "react-bootstrap";
import QueryNs from './queryNs';
import AddNs from './addNS';
import AcceptNs from './acceptNs';
import PrintNs from './printNs';

function NsManage() {
    const [wScode, setWScode] = useState('');

    return (
        <>
            <Container className="p-3">
                <AcceptNs/>
                <hr/>
                <PrintNs/>
                <hr/>
                <AddNs scode={wScode} setScode={setWScode}/>
                <hr/>
                <QueryNs scode={wScode} setScode={setWScode}/>
            </Container>
        </>
    );
}

export default NsManage;