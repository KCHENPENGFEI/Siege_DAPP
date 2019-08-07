import React from 'react';
import { BrowserRouter as Router,Route} from 'react-router-dom';
//import logo from './logo.svg';
import './App.css';
import MyPrepare from './page/prepaare/Prepare';
import MyBattle from './page/battle/battle';
import MyLoading from './page/Loading/Loading';
import PageAuction from './page/Auction/Auction'
import Login from './page/login/login'
import Map from './page/Map/map'
function App() {
  return (
    <div className="App">
      <Router>
        <Route exact path="/" component={Login}/>
        <Route exact path="/Login" component={Login}/>
        <Route exact path="/Auction" component={PageAuction}/>
        <Route exact path="/Map" component={Map}/>
        <Route exact path="/Loading" component={MyLoading}/>
        <Route exact path="/Prepare" component={MyPrepare}/>

        <Route exact path="/Battle" component={MyBattle}/>

      </Router>
     
    </div>
  );
}




export default App;
