import React from 'react';
import '../../css/common.css'
import './Prepare.css'



class MyPrepare extends React.Component {
    constructor(props){
        super()
    }
    render(){
        return(
            <div className="ccFlexColumn PrepareOutSide">
                <div className="scFlexRow userInfoPictureOutside">
                    <div className="userInfoPicture">
                    </div>
                </div>
                <div className="scFlexRow ChoiceBarOutside">
                    <div className="ChoiceBar">
                    </div>
                </div>
            </div>
      );
  }
}





export default MyPrepare;