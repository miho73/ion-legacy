import React from "react";
import { Container } from "react-bootstrap";
import IonIdActivation from "./idActivation";
import QueryIonId from "./queryIonId";
import IonIdChangPrivilege from "./changeState";

function IonIdManage() {
    return (
        <>
            <Container className="p-3">
                <div className="row my-3">
                    <h2 className="mb-3">IonID 조회</h2>
                    <QueryIonId/>
                </div>
                <hr/>
                <div className="row my-3">
                    <h2 className="mb-3">IonID 활성화</h2>
                    <IonIdActivation/>
                </div>
                <hr/>
                <div className="row my-3">
                    <h2 className="mb-3">IonID 권한 변경</h2>
                    <IonIdChangPrivilege/>
                </div>
            </Container>
        </>
    );
}

export default IonIdManage;