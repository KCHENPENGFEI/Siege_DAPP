import React from 'react';
import './checkBox.css'

class MyCheckBox extends React.Component{
    render(){
        return (
            <div className="MyCheckBoxBG" onClick={()=>{this.props.handleCheck()}}>
                {this.props.checked?<div className="MyCheckBoxIn"></div>:null}
                </div>
        )
    }
}




export default MyCheckBox;