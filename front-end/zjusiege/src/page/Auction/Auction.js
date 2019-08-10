import React from 'react';
import moment from 'moment';
import AS from '../../tools/AuctionSucess/AS'
import '../../css/common.css'
import './Auction.css'


const getitem = ()=>{
    let arr = []
    for(let i = 0 ; i < 50 ;i++){
        let item={}
        item.rank=i+1
        item.name="玩家"+i
        item.city="长安"
        item.price=10000
        item.time=moment().format("HH:mm:ss")
        arr.push(item)
    }
        
    return arr;
}

const getDiv = (item) =>(
<div className="zsAuctionItem ccFlexRow" key={item.rank}>
    <div className="ccFlexColumn playerInfo">
        {item.rank}
    </div>
    <div className="ccFlexColumn playerInfo">
        {item.name}
    </div>
    <div className="ccFlexColumn playerInfo">
        {item.city}
    </div>
    <div className="ccFlexColumn playerInfo">
        {item.price}
    </div>
    <div className="ccFlexColumn playerInfo">
        {item.time}
    </div>
</div>
)


class PageAuction extends React.Component {
    constructor(props){
        super()
        this.state={
            success:false
        }
    }
    handleAuction =()=>{
        this.setState({success:true})
    }
    handleCancel =()=>{
        this.setState({success:false})
    }
    handleSuccess =()=>{
        this.setState({success:false})
    }
    
    render(){
    return (
        <div className="zsAuctionOutSide ccFlexColumn">
            <AS 
                display={this.state.success} 
                money={1000} 
                city={"长安"}
                handleCancel={this.handleCancel}
                handleSuccess={this.handleSuccess}/>
            <div className="zsAuctionInSide">
                <div className="zsAuctionTitle">
                    <div className="closeButton eeFlexRow">
                        <div className="close">
    
                        </div>
                    </div>
                </div>
                <div className="zsAuctionRule ccFlexColumn">
                    <div className="zsAuctionRuleContent">
                    城池的拥有者通过拍卖竞价得到城池归属权，竞价榜前12名可以获得更优质的“黄金城池”， 助你在战斗中一臂之力。
                    </div>
                </div>
                <div className="zsAuctionMain">
                    <div className="zsAuctionMainTitle ccFlexColumn">
                        <div className="MainTitle"></div>
                    </div>
                    <div className="zsAuctionMainRanking scFlexColumn">
                        <div className="RankingTitle ecFlexColumn">
                            <div className="RankingTitleReal">
    
                            </div>
                        </div>
                        <div className="scrolldiv" id="testDiv">
                            {getitem().map(getDiv)}
                        </div>
                    </div>
                    <div className="zsAuctionMainInfo">
                        <div className="zsAuctionMainInfoTime">
                            <div className="zsAuctionMainInfoTimeTitle">
                                第一轮竞标结束时间
                            </div>
                            <div className="zsAuctionMainInfoTimeContent">
                                15:00
                            </div>
                        </div>
                        <div className="zsAuctionMainInfoMoney">
                            <div className="zsAuctionMainInfoMoneyLogo"/>
                            <input className="zsAuctionMainInfoMoneyInput" type="text" placeholder="请输入金额"/>
                        </div>
                        <div className="zsAuctionMainInfoButton" onClick={()=>{this.handleAuction()}}>
                        </div>
                        
                    </div>
                </div>
            </div>
        </div>
      );
  }
}





export default PageAuction;