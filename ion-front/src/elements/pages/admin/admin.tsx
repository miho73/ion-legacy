import React, { useState } from "react";
import { Container, Modal, ModalHeader, Tab, Tabs } from "react-bootstrap";
import IonIdManage from "./ionid/ionid";
import NsManage from "./ns/ns";

function ManagementPage() {
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