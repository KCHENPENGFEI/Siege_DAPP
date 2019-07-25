import React from 'react';
import './Auction.css'


const getitem = ()=>{
    let arr = []
    for(let i = 0 ; i < 50 ;i++)
        arr.push(i)
    return arr;
}

const getDiv = (item) =>(<div className="zsAuctionItem">this is item {item}</div>)


function PageAuction() {
  return (
    <div className="zsAuctionOutSide">
        <div className="zsAuctionInSide">
            <div className="zsAuctionTitle">
                城主招募
            </div>
            <div className="zsAuctionRule">
                <div className="zsAuctionRuleContent">
                城池的拥有者通过拍卖竞价得到城池归属权，竞价较高者在守城过程中会拥有特殊权利——价格更高的城池更难被攻破。
                </div>
            </div>
            <div className="zsAuctionMain">
                <div className="zsAuctionMainTitle">
                    当前竞价
                </div>
                <div className="zsAuctionMainRanking">
                    <div class="scrolldiv" id="testDiv">
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
                        <img className="zsAuctionMainInfoMoneyLogo"/>
                        <input className="zsAuctionMainInfoMoneyInput" type="text"/>
                    </div>
                    <div className="zsAuctionMainInfoButton">
                        确认竞标
                    </div>
                    
                </div>
            </div>
        </div>
    </div>
  );
}





export default PageAuction;