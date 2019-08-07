import React from 'react';
import MyMask from '../mask/mask'
import '../../css/common.css'
import './AS.css'

class AS extends React.Component{
    render(){
        return (
            <div style={{width:"100%",heightL:"100%"}}>
                {this.props.display?
                    (<div>
                        <MyMask/>
                        <div className="ccFlexColumn ASoutside">
                            <div className="opendiv scFlexColumn">
                                <div className="AScloseButton esFlexRow">
                                    <div className="ASclose" onClick={()=>{this.props.handleCancel()}}></div>
                                </div>
                                <div className="ASword">
                                    恭喜！你以 {this.props.money} SIG 竞拍成功，已成为 {this.props.city} 城主
                                </div>
                                <div className="ASbutton" onClick={()=>{this.props.handleSuccess()}}></div>
                            </div>
                        </div>
                        
                    </div>)
                    :null}
                
            </div>
        )
    }
}
export default AS;