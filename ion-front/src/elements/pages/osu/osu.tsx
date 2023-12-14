import React, {Component, useEffect, useState} from 'react';
import Alert from 'react-bootstrap/Alert';
import Button from 'react-bootstrap/Button';
import ButtonGroup from 'react-bootstrap/ButtonGroup';
import Form from 'react-bootstrap/Form';
import Image from 'react-bootstrap/Image';
import ErrorPage from "../etc/error";
import axios from "axios";

function Osu() {
    const state :string[][] = [['success', '정답입니다!'], ['danger', '오답입니다!']];
    const [data, setData] = useState(Object);
    const [answer, setAnswer] = useState(String);
    const [Source, setSource] = useState(String);
    const [img, setImg] = useState(String);
    const [workState, setWorkState] = useState(-1);
    const [buttonStateAgain, setButtonStateAgain] = useState(0);
    const [userAnswer, setUserAnswer] = useState('');
    const [answerState, setAnswerState] = useState(state[1]);
    const [alertState, setAlertState] = useState(0);
    useEffect(() => {
        axios.get('https://osu-api.kro.kr:8443/get')
            .then(res => {
                const content = res.data;
                if (content.id > 0) {
                    setData(content);
                    setImg(content.img);
                    setSource(content.source);
                    setAnswer(content.answer);
                    setWorkState(0);
                } else {
                    setWorkState(1);
                }
            })
            .catch(err => {
                console.error(err);
                setWorkState(1);
            })
    }, []);
    useEffect(() => {
        axios.get('https://osu-api.kro.kr:8443/get')
            .then(res => {
                const content = res.data;
                if (content.id > 0) {
                    setData(content);
                    setImg(content.img);
                    setSource(content.source);
                    setAnswer(content.answer);
                    setWorkState(0);
                } else {
                    setWorkState(1);
                }
            })
            .catch(err => {
                console.error(err);
                setWorkState(1);
            })
    }, [buttonStateAgain]);

    const getAnswer = event => {
        setUserAnswer(event.target.value);
    };

    const getUserAnswer = (answer) => {
        setUserAnswer(answer);
        if(userAnswer == answer) {
            alert('정답입니다.');
        }
        else {
            alert('오답입니다.');
        }
    };

    return (
        <div>
            <h3 className="display-5 fw-bold">수능 수학 문제 풀어보기</h3>
            <hr></hr>

            <Form.Group className="mb-3" controlId="AnswerForm.Answer">
                <Form.Label id='source'>[ {Source} ]</Form.Label>
                <br></br>
                <Form.Label>정답</Form.Label>
                <Form.Control type="text" value={userAnswer} onChange={getAnswer}/>
                <br></br>
                <ButtonGroup aria-label="Basic example">
                    <Button variant="success" onClick={() => {
                        setAlertState(1);
                        if(userAnswer === answer) {
                            setAnswerState(state[0]);
                        }
                        else {
                            setAnswerState(state[1])
                        }
                    }}>제출</Button>
                    <Button variant="danger" onClick={() => {
                        setButtonStateAgain(buttonStateAgain + 1)
                        setUserAnswer('');
                        setAlertState(0);
                    }}>재도전</Button>
                </ButtonGroup>
                <br></br>
                <br></br>
                {alertState === 1 &&
                    <Alert variant={answerState[0]}>{answerState[1]}</Alert>
                }

            </Form.Group>

            <br></br>
            {workState === 0 &&
                <Image src={img} fluid />
            }
            {
                workState === 1 &&
                <ErrorPage exp='API에서 받아오는 정보가 없습니다.'/>
            }

        </div>
    );
}

export default Osu;