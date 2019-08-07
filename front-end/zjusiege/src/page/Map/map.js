import React from 'react';
import '../../css/common.css'
import './map.css'
import BI from '../../tools/BattleInfo/bi';
import AC from '../../tools/AttackCity/AC';
import Wait from '../../tools/Wait/Wait';
import Fail from '../../tools/Wait/Fail';
const cityData={
    userName:"玩家1",
    interval:"15:34",
    price:2000,
    change:2,
    income:3267
}


class Map extends React.Component{
    constructor(props){
        super()
        this.state={
            display:false,
            displayAttack:true
        }
    }
    handleStart = ()=>{
        this.setState({display:true})
    }
    handleSuccess=()=>{
        this.setState({display:false})
        this.setState({displayAttack:false})
    }
    handleCancel=()=>{
        this.setState({display:false})
        this.setState({displayAttack:false})
    }
    getData =()=>{
        let arr=[];
        for(let i=0;i<50;i++){
            arr.push("玩家"+i+" 出价 "+i*200+"攻占城池 金陵")
        }
        return arr
    }
    dataToInfo = (item)=>(<p>{item}</p>)
    render(){
        return (
            <div>
            <BI 
                display={this.state.display}
                player={"ABCD"}
                handleSuccess={this.handleSuccess}
                handleCancel={this.handleCancel}
            />
            <AC display={this.state.displayAttack} handleSuccess={this.handleSuccess}/>
            <Wait display={false}/>
            <Fail display={false}/>
            <div className="MapOutSide bcFlexRow">

                <div className="csFlexColumn userInfo">
                    <div className="ccFlexColumn userBG">

                    </div>
                    <div className="scFlexColumn cityBG">
                        <div className="cityPictureBorder ccFlexColumn">
                            <div className="cityPicture">

                            </div>
                        </div>
                        <div className="cityName">
                             长安
                        </div>
                        <div className="csFlexColumn cityInfo">
                        {/* <p>城主：{cityData.userName}</p> */}
                        <p>攻占时长：{cityData.interval}</p>
                        <p>出价：{cityData.price}</p>
                        <p>城池争夺次数：{cityData.change}次</p>
                        <p>累计收益：{cityData.income} 金币</p>
                        </div>
                        <div className="cancelButton">
                        </div>
                    </div>
                    <div className="ssFlexColumn SystemInfo">
                        <div className="scFlexRow SystemInfoTitle">
                            <div className="SystemInfoTitleReal">

                            </div>
                        </div>
                        <div className="csFlexColumn commonInfo">
                            <div className="ssFlexColumn commonInfoReal">
                            {this.getData().map(this.dataToInfo)}
                            </div>
                        </div>
                    </div>
                    

                </div>
                <div className="csFlexColumn CityInfo">
                    <div className="scFlexColumn enemycityBG">
                            <div className="ccFlexColumn enemycityName">
                                长安
                            </div>
                            <div className="enemycityPictureBorder ccFlexColumn">
                                <div className="enemycityPicture">

                                </div>
                            </div>

                            <div className="csFlexColumn enemycityInfo">
                            <p>城主：{cityData.userName}</p>
                            <p>攻占时长：{cityData.interval}</p>
                            <p>出价：{cityData.price}</p>
                            <p>城池争夺次数：{cityData.change}次</p>
                            <p>累计收益：{cityData.income} 金币</p>
                            </div>
                            <div className="AttackButton" onClick={()=>{this.handleStart()}}>
                            </div>
                        </div>
                </div>
            </div>
            </div>
        )
    }
}
export default Map;