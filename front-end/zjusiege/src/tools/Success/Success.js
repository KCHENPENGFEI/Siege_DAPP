import React from 'react';
import MyMask from '../mask/mask'
import '../../css/common.css'
import './Success.css'

class Success extends React.Component{
    render(){
        return (
            <div style={{width:"100%",heightL:"100%"}}>
                {this.props.display?
                    (<div>
                        <MyMask/>
                        <div className="ccFlexColumn Successoutside">
                            <div className="Successopendiv scFlexColumn">
                                <div className="SuccessTopBar"></div>
                            </div>
                        </div>
                        
                    </div>)
                    :null}
                
            </div>
        )
    }
}
export default Success;