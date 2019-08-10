import React from 'react';
import MyMask from '../mask/mask'
import '../../css/common.css'
import './Wait.css'

class Wait extends React.Component{
    render(){
        return (
            <div >
                {this.props.display?
                    (<div>
                        <MyMask />
                        <div className="ccFlexColumn Waitoutside">
                            { <div className="Waitopendiv scFlexColumn">
                                {/* <div className="WaitcloseButton esFlexRow">
                                    {<div className="Waitclose" onClick={()=>{this.props.handleCancel()}}></div> }
                                    </div>
                                <div className="Waitword">
                                    <p>尊敬的玩家：</p>
                                    <p style={{textIndent:"2em"}}>得城容易、守城难。战火起，战鼓昂。现有玩家 {this.props.player} 向你发起挑战，请问你是否迎战？</p>
                                </div>
                                <div className="bcFlexRow WaitbuttonBar">
                                    <div className="WaitbuttonCancel" onClick={()=>{this.props.handleSuccess()}}></div>
                                    <div className="WaitbuttonStart" onClick={()=>{this.props.handleCancel()}}></div>
                                </div> */}
                                
                            </div> }
                        </div>
                        
                    </div>)
                    :null}
                
            </div>
        )
    }
}
export default Wait;