import React from 'react';
import MyMask from '../mask/mask'
import CC from '../cityCard/cc'
import '../../css/common.css'
import './AC.css'
class AC extends React.Component{
    constructor(props){
        super()
        this.state={
            data:this.getData()
        }
        console.log(this.state.data)
    }
    getData=()=>{
        let arr=[]
        for(let i=0;i<50;i++){
            let item={
                key:i,
                choiced:false
            }
            arr.push(item)
        }

        return arr
    }
    DataToCC = (item)=>(<CC key={item.key} choiced={item.choiced} handleClick={this.handleClick} number={item.key}/>)
    handleClick = (i)=>{
        let data =this.state.data;
        data[i].choiced =!data[i].choiced
        this.setState({data:data})
    }
    render(){
        return (
            <div style={{width:"100%",heightL:"100%"}}>
                {this.props.display?
                    (<div>
                        <MyMask/>
                        <div className="ccFlexColumn ACoutside">
                            <div className="ACopendiv scFlexColumn">
                                <div className="ACcloseButton esFlexRow">
                                    <div className="ACclose" onClick={()=>{this.props.handleCancel()}}></div>
                                </div>
                                <div className="ACword">
                                你可以向任何一位城主发起进攻，若你战胜，你可以取而代之成为新的城主，享受城池的出产率
                                </div>
                                <div className="ccFlexRow ACinfoBar">
                                    <div className="scFlexRow">
                                        <input className="ACinputData" placeholder="请输入城池名称"/>
                                    </div>
                                    <div className="ccFlexRow ACinfoWord">
                                    </div>
                                </div>
                                <div className="scFlexRow AClist">
                                    {this.state.data.map(this.DataToCC)}
                                </div>
                                <div className="ACbutton" onClick={()=>{this.props.handleSuccess()}}></div>
                            </div>
                        </div>
                        
                    </div>)
                    :null}
                
            </div>
        )
    }
}
export default AC;