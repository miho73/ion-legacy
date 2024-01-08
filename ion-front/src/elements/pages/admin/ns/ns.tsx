import React, {useEffect, useState} from 'react';
import {Container} from "react-bootstrap";
import QueryNs from './queryNs';
import AddNs from './addNS';
import AcceptNs from './acceptNs';
import PrintNs from './printNs';
import axios from "axios";
import {API_PREFIX} from "../../../service/apiUrl";
import ChangeNsPreset from "./changeNsPreset";

function NsManage() {
    const [wScode, setWScode] = useState('');
    const [timePreset, setTimePreset] = useState(-1);

    useEffect(() => {
        axios.get(API_PREFIX+'/manage/api/ns/mode/get')
            .then(res => {
                setTimePreset(res.data['result']);
            })
            .catch(err => {
                setTimePreset(-1);
            });
    }, []);

    return (
        <Container className="p-3">
            <AcceptNs/>
            <hr/>
            <PrintNs timePreset={timePreset}/>
            <hr/>
            <AddNs timePreset={timePreset} scode={wScode} setScode={setWScode}/>
            <hr/>
            <QueryNs scode={wScode} setScode={setWScode}/>
            <hr/>
            <ChangeNsPreset timePreset={timePreset} updatePreset={setTimePreset}/>
        </Container>
    );
}

export default NsManage;