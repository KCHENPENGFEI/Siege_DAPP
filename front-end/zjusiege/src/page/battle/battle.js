import React from 'react';
import '../../css/common.css'
import './battle.css'
import Success from '../../tools/Success/Success';



class MyBattle extends React.Component {
    constructor(props){
        super()
    }
    render(){
        return(
            <div>
                <Success
                    display={false}/>

            <div className="ccFlexColumn BattleOutSide">
                <div className="enemyMagic"></div>
                <div className="enemyCardBar"></div>
                <div className="bcFlexRow MainBar">
                    <div className="bcFlexColumn BattleInfoBar">
                        <div className="BattleEnemy"></div>
                        <div className="BattleState"></div>
                        <div className="BattleEnemy"></div>
                    </div>
                    <div className="bcFlexColumn BatttleCards">
                        <div className="EnemyBattleCard"></div>
                        <div className="MyBattleCard"></div>
                        
                    </div>
                    <div className="ccFlexColumn BattleInfoBar">

                    </div>
                </div>
                <div className="ceFlexRow MyCardBar">
                    <div className="decideFake"></div>
                    <div className="CardBar"></div>
                    <div className="decide"></div>
                </div>
                <div className="enemyMagic"></div>
            </div>
            </div>
      );
  }
}





export default MyBattle;