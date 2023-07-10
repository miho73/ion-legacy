import React, { useState } from "react";
import { Container, Modal, ModalHeader, Tab, Tabs } from "react-bootstrap";
import IonIdManage from "./ionid/ionid";
import NsManage from "./ns/ns";

function ManagementPage() {
    const [showModal, setShowModal] = useState(false);
    const [modalCfg, setModalCfg] = useState({title:'', content: null, footer: null});

    function closeModal() {
        setShowModal(false);
    }
    
    return (
        <>
            <Container className='mt-4'>
                <h1>Ion Management</h1>
                <Tabs
                    defaultActiveKey='ionid'
                >
                    <Tab eventKey='ionid' title='IonID'>
                        <IonIdManage/>
                    </Tab>
                    <Tab eventKey='ns' title='면불'>
                        <NsManage/>
                    </Tab>
                </Tabs>
            </Container>
            <Modal show={showModal} onHide={closeModal} dialogClassName="modal-dialog-centered">
                <ModalHeader>
                    <Modal.Title>{modalCfg.title}</Modal.Title>
                </ModalHeader>
                <Modal.Body>{modalCfg.content}</Modal.Body>
                <Modal.Footer>{modalCfg.footer}</Modal.Footer>
            </Modal>
        </>
    )
}

export default ManagementPage;