import React from 'react';
import './mask.css'

class MyMask extends React.Component{
    constructor(props){
        super()
    }
    render(){
        const zIndex =this.props.zIndex?this.props.zIndex:920;
        return (
            <div className="mask" style={{zIndex:zIndex}}></div> 
        )
    }
}
export default MyMask;