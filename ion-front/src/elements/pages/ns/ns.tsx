import React, { useEffect, useState } from 'react';

import LnsRoomSelect from './nsr/lns';
import { changeBit, getBit } from '../../service/bitmask';
import { inRange } from '../../service/checker';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';
import {isLogin} from '../../service/auth'
import CannotAuthorize from '../auth/cannotAuth';

import { Button, Modal } from 'react-bootstrap';
import NsState from './nsState';

function Ns() {
    const [lnsRoomRequired, setLnsRoomRequired] = useState(false);
    const [revTime, setRevTime] = useState(-1);
    const [revPlace, setRevPlace] = useState('');
    const [revSup, setRevSup] = useState('');
    const [revRes, setRevRes] = useState('');
    const [lnsSelected, setLnsSelected] = useState(-1);
    const [formState, setFormState] = useState(0);

    const [working, setWorking] = useState(false);
    const [sErrorState, setSError] = useState(-1);

    const [cNsLst, setNsLst] = useState<any[]>([]);
    const [cNsErr, setNsErr] = useState(false);
    const [date, setDate] = useState('');
    const [uName, setUName] = useState('');

    const [deleteModalShow, setDeleteModalShow] = useState(false);
    const [targetNs, setTargetNs] = useState([]);
    const [deleting, setDeleting] = useState(false);
    const [deleteResult, setDeleteResult] = useState(-1);

    const [seatLst, setSeatLst] = useState<any[]>([]);
    const [lnsError, setLnsError] = useState(0);

    const navigate = useNavigate();

    const [loginState, setLoginState] = useState(-1);
    useEffect(() => {
        isLogin(setLoginState);
        loadNsReqs();
    }, []);

    useEffect(() => {
        loadLns();
    }, [lnsRoomRequired]);

    if(loginState === -1) {
        return <></>;
    }
    if(loginState === 1) {
        navigate('/');
    }
    if(loginState === 2) {
        return <CannotAuthorize/>
    }

    function loadNsReqs() {
        setDeleting(true);

        axios.get('/ns/api/nsr/get')
        .then(res => {
            const reqs = res.data['result'];
            setUName(reqs['name']);
            setDate(reqs['date']);
            setNsLst(reqs['reqs']);
        })
        .catch(err => {
            setNsErr(true);
        }).finally(() => {
            setDeleting(false);
        });
    }

    let rr: any[] = [];
    if(!cNsErr) {
        if(cNsLst.length === 0) {
            rr.push(
                <tr>
                    <td colSpan={7}>
                        <p className='my-2 fw-bold'>신청한 면불이 없습니다.</p>
                    </td>
                </tr>
            );
        }
        else {
            cNsLst.forEach(req => {
                let row = <NsState name={req.time} place={req.place} superviser={req.supervisor} reason={req.reason} seat={req.lnsSeat} lnsReq={req.lnsReq} status={req.status} showDeleteConfirm={() => setDeleteModalShow(true)} setTargetNs={setTargetNs}/>
                rr.push(row);
            });
        }
    }
    else {
        rr.push(
            <tr>
                <td className='table-danger' colSpan={7}>면불 신청 리스트를 받지 못했습니다.</td>
            </tr>
        )
    }

    const AREA = ['A', 'B', 'C', 'D', 'E', 'F'];
    function submit() {
        let state = 0

        // validate
        if(revTime < 0 || revTime > 2) state = changeBit(state, 0);
        if(!inRange(1, 30, revPlace.length)) state = changeBit(state, 1);
        if(!inRange(1, 10, revSup.length)) state = changeBit(state, 2);
        if(!inRange(1, 30, revRes.length)) state = changeBit(state, 3);
        if(lnsRoomRequired && lnsSelected === -1) state = changeBit(state, 4);

        setFormState(state);

        if(state === 0) {
            setWorking(true);

            let time;
            switch(revTime) {
                case 0:
                    time = 'N8';
                    break;
                case 1:
                    time = 'N1';
                    break;
                case 2:
                    time = 'N2';
                    break;
            }
            let lns;
            if(lnsSelected !== -1) {
                let seat = lnsSelected % 10;
                let area = (lnsSelected - seat) / 10
                lns = AREA[area-1] + seat;
            }
            else {
                lns = -1;
            }

            axios.post('/ns/api/nsr/create', {
                time: time,
                place: revPlace,
                supervisor: revSup,
                reason: revRes,
                lnsReq: lnsRoomRequired,
                lnsSeat: lns,
            }).then(res => {
                setSError(0);
                loadNsReqs();
                loadLns();
            }).catch(err => {
                const code = err.response.data['result'];
                switch(code) {
                    case 4:
                        setSError(2);
                        break;
                    case 5:
                        setSError(3);
                        break;
                    default:
                        setSError(1);
                        break;
                }
            }).finally(() => {
                setWorking(false);
            })
        }
    }

    function deleteNs() {
        axios.delete('/ns/api/nsr/delete', {
            params: {
                time: targetNs[0]
            }
        })
        .then(res => {
            closeDeleteConfirm();
            loadNsReqs();
            loadLns();
        })
        .catch(err => {
            setDeleteResult(1);
        });
    }
    function closeDeleteConfirm() {
        setDeleteModalShow(false);
        setDeleteResult(-1);
    }

    function loadLns() {
        if(lnsRoomRequired) {
            axios.get('/ns/api/lns/get')
            .then(res => {
                const dat = res.data['result'];
                const tset: any[] = [];
                dat.forEach(ns => {
                    let rp = {};
                    ns.forEach(e => {
                        if(e['v']) {
                            rp[e['sn']] = {
                                name: e['name'],
                                scode: e['scode']
                            };
                        }
                    });
                    tset.push(rp);
                });
                setSeatLst(tset);
                setLnsError(1);
            })
            .catch(err => {
                setLnsError(2);
            });
        }
    }

    return (
        <main className='container mt-4'>
            <div className='row'>
                <h4>{uName}님의 면불 신청</h4>
                <table className='m-auto table'>
                    <thead>
                        <tr>
                            <th>면학</th>
                            <th>장소</th>
                            <th>담당교사</th>
                            <th>사유</th>
                            <th>노면실 자리</th>
                            <th>상태</th>
                            <th></th>
                        </tr>
                    </thead>
                    <tbody>{rr}</tbody>
                </table>
                <p>{date}</p>
            </div>
            <div className='mt-4'>
                <h4>면불 신청</h4>
                <form className='mx-3'>
                    <div className='row my-2'>
                        <fieldset className='mb-3 col'>
                            <label htmlFor='time' className='form-label'>면학</label>
                            <select className={'form-select'+(getBit(formState, 0) ? ' is-invalid' : '')} id='time' aria-label='면학 시간' disabled={working} value={revTime} onChange={e => setRevTime(Number.parseInt(e.target.value))}>
                                <option value={-1}>면학 시간</option>
                                <option value={0}>8면</option>
                                <option value={1}>1면</option>
                                <option value={2}>2면</option>
                            </select>
                        </fieldset>
                        <fieldset className='mb-3 col'>
                            <label htmlFor='place' className='form-label'>장소</label>
                            <input type='text' className={'form-control'+(getBit(formState, 1) ? ' is-invalid' : '')} id='place' value={revPlace} disabled={working} onChange={e => setRevPlace(e.target.value)}/>
                        </fieldset>
                        <fieldset className='mb-3 col'>
                            <label htmlFor='superviser' className='form-label'>담당교사</label>
                            <input type='text' className={'form-control'+(getBit(formState, 2) ? ' is-invalid' : '')} id='superviser' value={revSup} disabled={working} onChange={e => setRevSup(e.target.value)}/>
                        </fieldset>
                        <fieldset className='mb-3 col'>
                            <label htmlFor='reason' className='form-label'>신청사유</label>
                            <input type='text' className={'form-control'+(getBit(formState, 3) ? ' is-invalid' : '')} id='reason' value={revRes} disabled={working} onChange={e => setRevRes(e.target.value)}/>
                        </fieldset>
                    </div>
                    <div className='row mx-3 my-2'>
                        <fieldset className='form-check'>
                            <input className={'form-check-input'+(getBit(formState, 4) ? ' is-invalid' : '')} type='checkbox' value='' disabled={working} checked={lnsRoomRequired} id='lnsRr' onChange={e => setLnsRoomRequired(e.target.checked)}/>
                            <label className='form-check-label' htmlFor='lnsRr'>노면실 자리를 예약해야 합니다.</label>
                        </fieldset>
                    </div>
                    {lnsRoomRequired &&
                        <div className='mx-4 my-3 border p-3'>
                            <h5>노면실 자리 예약</h5>
                            {revTime === -1 &&
                                <p className='my-0'>면불 시간을 선택해주세요.</p>
                            }
                            {revTime !== -1 &&
                                <LnsRoomSelect selected={lnsSelected} setSelected={(x) => setLnsSelected(x)} nst={revTime} lnsState={lnsError} seatLst={seatLst}/>
                            }
                        </div>
                    }
                    <div className='row mx-1 my-4'>
                        <Button type='button' className='w-auto' disabled={working} onClick={submit}>제출</Button>
                    </div>
                    {sErrorState === 0 &&
                        <div className='alert alert-success'>
                            <p className='my-0'>신청되었습니다.</p>
                        </div>
                    }
                    {sErrorState === 1 &&
                        <div className='alert alert-danger'>
                            <p className='my-0'>신청하지 못했습니다.</p>
                        </div>
                    }
                    {sErrorState === 2 &&
                        <div className='alert alert-danger'>
                            <p className='my-0'>이미 신청한 시간입니다.</p>
                        </div>
                    }
                    {sErrorState === 3 &&
                        <div className='alert alert-danger'>
                            <p className='my-0'>이미 신청된 자리입니다.</p>
                        </div>
                    }
                </form>
            </div>

            <Modal show={deleteModalShow} onHide={closeDeleteConfirm} dialogClassName='modal-dialog-centered'>
                <Modal.Header closeButton>
                    <Modal.Title>면불 신청 삭제</Modal.Title>
                </Modal.Header>
                <Modal.Body>
                    {deleteResult === -1 &&
                        <p>{targetNs[1]} 면불 신청을 삭제할까요?</p>
                    }
                    {deleteResult === 1 &&
                        <p className='bg-danger text-white p-3 rounded'>삭제하지 못했습니다.</p>
                    }
                </Modal.Body>
                <Modal.Footer>
                    {deleteResult === -1 &&
                        <>
                            <Button onClick={deleteNs} disabled={deleting}>예</Button>
                            <Button onClick={closeDeleteConfirm} disabled={deleting}>아니오</Button>
                        </>
                    }
                    {deleteResult === 0 &&
                        <>
                            <Button onClick={closeDeleteConfirm}>확인</Button>
                        </>
                    }
                    {deleteResult === 1 &&
                        <>
                            <Button onClick={deleteNs} disabled={deleting}>재시도</Button>
                            <Button onClick={closeDeleteConfirm} disabled={deleting}>취소</Button>
                        </>
                    }
                </Modal.Footer>
            </Modal>
        </main>
    )
}

export default Ns;
