import React, { useEffect, useState } from "react";
import { Container, Modal, ModalHeader, Tab, Tabs } from "react-bootstrap";
import IonIdManage from "./ionid/ionid";
import NsManage from "./ns/ns";
import { checkPrivilege, isLogin } from "../../service/auth";
import { useNavigate } from "react-router-dom";
import CannotAuthorize from '../auth/cannotAuth';

function ManagementPage() {
    const navigate = useNavigate();

    const [loginState, setLoginState] = useState(-1);
    useEffect(() => {
        checkPrivilege(setLoginState);
    }, []);

    if(loginState === -1) {
        return <></>;
    }
    if(loginState === 1) {
        navigate('/');
    }
    if(loginState === 2) {
        return <CannotAuthorize/>
    }

    return (
        <>
            <Container className='mt-4'>
                <h1>Ion Management</h1>
                <Tabs
                    defaultActiveKey='ns'
                >
                    <Tab eventKey='ionid' title='IonID'>
                        <IonIdManage/>
                    </Tab>
                    <Tab eventKey='ns' title='면불 승인'>
                        <NsManage/>
                    </Tab>
                </Tabs>
            </Container>
        </>
    )
}

export default ManagementPage;