import React, { useEffect, useState } from 'react';

import LnsRoomSelect from './nsr/lns';
import { changeBit, getBit } from '../../service/bitmask';
import { inRange } from '../../service/checker';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';

const NsState = function(props) {
    var cla = '';
    switch(props.state) {
        case 0:
            cla = 'table-light';
            break;
        case 1:
            cla = 'table-success';
            break;
        case 2:
            cla = 'table-warning';
            break;
        case 3:
            cla = 'table-danger';
            break;
    }

    return (
        <tr className={cla}>
            <th>{props.name}</th>
            <td>{props.place}</td>
            <td>{props.superviser}</td>
            <td>{props.reason}</td>
            <td>{props.seat}</td>
        </tr>
    )
}

function Ns(props) {
    const [lnsRoomRequired, setLnsRoomRequired] = useState(false);
    const [revTime, setRevTime] = useState(-1);
    const [revPlace, setRevPlace] = useState('');
    const [revSup, setRevSup] = useState('');
    const [revRes, setRevRes] = useState('');
    const [lnsSelected, setLnsSelected] = useState(-1);
    const [formState, setFormState] = useState(0);

    const navigate = useNavigate();

    // check login
    useEffect(() => {
        if(props.iden === null) {
            navigate('/auth/login');
        }
    }, []);
    if(props.iden === null) {
        return (
            <></>
        );
    }
    
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
            axios.post('/ns/api/req/create', {
                time: revTime,
                place: revPlace,
                supervisor: revSup,
                reason: revRes,
                lnsReq: lnsRoomRequired,
                lnsRn: lnsSelected
            }).then(res => {

            }).catch(err => {
                
            })
        }
    }

    return (
        <>
            <div className='row'>
                <h4>님의 면불 신청</h4>
                <table className='m-auto w-75 table'>
                    <thead>
                        <tr>
                            <th>면학</th>
                            <th>장소</th>
                            <th>담당교사</th>
                            <th>사유</th>
                            <th>노면실 자리</th>
                        </tr>
                    </thead>
                    <tbody>
                        <NsState name='8면' place='노면실' superviser='김희원' reason='전자기기 사용' seat='A1' state={0}></NsState>
                        <NsState name='1면' place='노면실' superviser='김희원' reason='전자기기 사용' seat='A1' state={0}></NsState>
                        <NsState name='2면' place='노면실' superviser='김희원' reason='전자기기 사용' seat='A1' state={0}></NsState>
                    </tbody>
                </table>
            </div>
            <hr/>
            <div>
                <h4>면불 신청</h4>
                <form className='mx-3'>
                    <div className='row my-2'>
                        <fieldset className='mb-3 col'>
                            <label htmlFor='time' className='form-label'>면학</label>
                            <select className={'form-select'+(getBit(formState, 0) ? ' is-invalid' : '')} id='time' aria-label='면학 시간' value={revTime} onChange={e => setRevTime(Number.parseInt(e.target.value))}>
                                <option value={-1}>면학 시간</option>
                                <option value={0}>8면</option>
                                <option value={1}>1면</option>
                                <option value={2}>2면</option>
                            </select>
                        </fieldset>
                        <fieldset className='mb-3 col'>
                            <label htmlFor='place' className='form-label'>장소</label>
                            <input type='text' className={'form-control'+(getBit(formState, 1) ? ' is-invalid' : '')} id='place' value={revPlace} onChange={e => setRevPlace(e.target.value)}/>
                        </fieldset>
                        <fieldset className='mb-3 col'>
                            <label htmlFor='superviser' className='form-label'>담당교사</label>
                            <input type='text' className={'form-control'+(getBit(formState, 2) ? ' is-invalid' : '')} id='superviser' value={revSup} onChange={e => setRevSup(e.target.value)}/>
                        </fieldset>
                        <fieldset className='mb-3 col'>
                            <label htmlFor='reason' className='form-label'>신청사유</label>
                            <input type='text' className={'form-control'+(getBit(formState, 3) ? ' is-invalid' : '')} id='reason' value={revRes} onChange={e => setRevRes(e.target.value)}/>
                        </fieldset>
                    </div>
                    <div className='row mx-3 my-2'>
                        <fieldset className='form-check'>
                            <input className={'form-check-input'+(getBit(formState, 4) ? ' is-invalid' : '')} type='checkbox' value='' checked={lnsRoomRequired} id='lnsRr' onChange={e => setLnsRoomRequired(e.target.checked)}/>
                            <label className='form-check-label' htmlFor='lnsRr'>노면실 자리를 예약해야 합니다.</label>
                        </fieldset>
                    </div>
                    {lnsRoomRequired &&
                        <div className='mx-4 my-3 border p-3'>
                            <h5>노면실 자리 예약</h5>
                            <LnsRoomSelect selected={lnsSelected} setSelected={(x) => setLnsSelected(x)}/>
                        </div>
                    }
                    <div className='row mx-1 my-4'>
                        <button type='button' className='btn btn-primary w-auto' onClick={submit}>제출</button>
                    </div>
                </form>
            </div>
        </>
    )
}

export default Ns;
