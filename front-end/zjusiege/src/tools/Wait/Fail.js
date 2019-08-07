import React from 'react';
import MyMask from '../mask/mask'
import '../../css/common.css'
import './Fail.css'

class Fail extends React.Component{
    render(){
        return (
            <div >
                {this.props.display?
                    (<div>
                        <MyMask />
                        <div className="ccFlexColumn Failoutside">
                            { <div className="Failopendiv scFlexColumn">
                                {/* <div className="FailcloseButton esFlexRow">
                                    {<div className="Failclose" onClick={()=>{this.props.handleCancel()}}></div> }
                                    </div>
                                <div className="Failword">
                                    <p>尊敬的玩家：</p>
                                    <p style={{textIndent:"2em"}}>得城容易、守城难。战火起，战鼓昂。现有玩家 {this.props.player} 向你发起挑战，请问你是否迎战？</p>
                                </div>
                                <div className="bcFlexRow FailbuttonBar">
                                    <div className="FailbuttonCancel" onClick={()=>{this.props.handleSuccess()}}></div>
                                    <div className="FailbuttonStart" onClick={()=>{this.props.handleCancel()}}></div>
                                </div> */}
                                
                            </div> }
                        </div>
                        
                    </div>)
                    :null}
                
            </div>
        )
    }
}
export default Fail;