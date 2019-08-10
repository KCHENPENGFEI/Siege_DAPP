import React from 'react';
import MyMask from '../mask/mask'
import '../../css/common.css'
import './cc.css'

class CC extends React.Component{
    constructor(props){
        super()
    }
    render(){
        return (
            <div>
                {!this.props.choiced?
                    <div className="bcFlexColumn ccOutside" onClick={()=>{this.props.handleClick(this.props.number)}}>
                    </div>:
                    <div className="bcFlexColumn ccOutside2" onClick={()=>{this.props.handleClick(this.props.number)}}>
                    </div>    
                }
            </div>
            
            
        )
    }
}
export default CC;