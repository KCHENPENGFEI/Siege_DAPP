import React from 'react';

import MyCheckBox from '../../tools/checkBox/checkBox'

import './login.css'
import '../../css/common.css'


class Login extends React.Component{
    constructor(props){
        super()
        this.state={
            publicKey:"",
            privateKey:"",
            checked:false,
        }
    }
    handleCheck=()=>{
        this.setState({checked:!this.state.checked})
    }
    handleLogin=()=>{
        console.log("log in!")
    }
    handleRegister=()=>{
        console.log("register!")
    }
    render(){
        return (
            <div className="login ecFlexColumn">
                <div className="loginInfo scFlexColumn infoWord">
                    <div className="ccFlexRow infoBar">
                        <div className="ccFlexRow infoWord">
                            公钥：
                        </div>
                        <div className="ccFlexRow">
                            <input className="inputData"/>
                        </div>
                    </div>
                    <div className="ccFlexRow  infoBar">
                        <div className="ccFlexRow infoWord">
                            私钥：
                        </div>
                        <div className="ccFlexRow">
                            <input className="inputData"/>
                        </div>
                    </div>
                    <div className="ccFlexRow infoBar">
                        <div className="ccFlexRow">
                            <div className="checkBox">
                                <MyCheckBox handleCheck={this.handleCheck} checked={this.state.checked}/>
                            </div>
                            <p className="infoWord2">
                                记住我，下次自动登录
                            </p>
                        </div>
                    </div>
                    <div className="ccFlexRow infoBar">
                        <div className="ccFlexRow LandRButton RButton" onClick={()=>{this.handleRegister()}}></div>
                        <div className="ccFlexRow  LandRButton LButton" onClick={()=>{this.handleLogin()}}></div>
                    </div>
                    <div className="ccFlexColumn infoBar RightBar">
                        <p>抵制不良游戏 拒绝盗版游戏 注意自我保护 谨防受骗上当</p>
                        <p>适度游戏益脑 沉迷游戏伤身 合理安排时间 享受健康生活</p>
                        <p>Copyright 2019. All Rights Reserved. Version 1.0.0</p>
                    </div>
                    
                </div>
            </div>
        )
    }
}

export default Login;