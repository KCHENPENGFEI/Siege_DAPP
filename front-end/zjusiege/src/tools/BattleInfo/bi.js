import React from 'react';
import MyMask from '../mask/mask'
import '../../css/common.css'
import './bi.css'

class BI extends React.Component{
    render(){
        return (
            <div >
                {this.props.display?
                    (<div>
                        <MyMask/>
                        <div className="ccFlexColumn BIoutside">
                            <div className="BIopendiv scFlexColumn">
                                <div className="BIcloseButton esFlexRow">
                                    {/* <div className="BIclose" onClick={()=>{this.props.handleCancel()}}></div> */}
                                    </div>
                                <div className="BIword">
                                    <p>尊敬的玩家：</p>
                                    <p style={{textIndent:"2em"}}>得城容易、守城难。战火起，战鼓昂。现有玩家 {this.props.player} 向你发起挑战，请问你是否迎战？</p>
                                </div>
                                <div className="bcFlexRow BIbuttonBar">
                                    <div className="BIbuttonCancel" onClick={()=>{this.props.handleSuccess()}}></div>
                                    <div className="BIbuttonStart" onClick={()=>{this.props.handleCancel()}}></div>
                                </div>
                                
                            </div>
                        </div>
                        
                    </div>)
                    :null}
                
            </div>
        )
    }
}
export default BI;