pragma solidity ^0.5.0;

/**
 * Siege合约
 */

contract SiegeAsset {
	function transfer(address _from, address _to, uint256 _value, string calldata _symbol, bytes calldata _data) external;
	function transferNFTs(address _from, address _to, uint256[] calldata _uuids, bytes calldata _data) external;
	function balanceOf(address _owner, string calldata _symbol) external;
	function supplyOf(string calldata _symbol, uint8 _ext) external;

	// event
    event TransferCoin(address indexed _from, address indexed _to, uint256 _value);
    event TransferNFT(address indexed _from, address indexed _to, uint256 _uuid);
}

contract Siege {

	// 游戏参数
	uint256 PRECISION;
	uint256 CITY_NUM;
	uint256 PLAYER_NUM;
	uint256 ENTER_FEE;
	uint256 CITY_PRICE;
	uint256 SOLDIER_NUM;
	uint256 INTERVAL;
	uint256 INTERVAL_NUM;  // 300 / 10

	// address deploy_address = 0x72ba7d8e73fe8eb666ea66babc8116a41bfb10e2;
	// gameItem合约地址
	//address gameItemAddr = 0x429F382e15054439B0Bc4Fc1139E729D4dC5E578;
	//address siegeTeamAddr = 0x11BD06F184F3767Fc02c7f27E812f51BC6f28b39;

	// root地址
	address rootAddr;
	// 游戏资产地址
	address gameAssetAddr;
	
	/**
		链上存储数据结构
	*/
	// 游戏内容参数
	enum gameStage {START, BIDDING, RUNNING, SETTLING, END}
	// 兵种类型
	enum soldierType {none, infantry, spearman, shieldman, archer, cavalry}
	// 兵种战力
	mapping (uint256 => uint256) soldiersPoint;
	// 城池名称
	mapping (uint256 => string) cityName;
	// 城池守城指数
	mapping (uint256 => uint256) cityDefenseIndex;

	// 游戏过程信息
	struct gameInfo {
		uint256 round_id;
		uint256 all_soldiers_point;
		uint256 current_soldiers_point;
		uint256 soldiers_quantity;
		uint256[] crypto_soldiers_cellar;   // 加密后的士兵仓库，战斗前上传用于证明
		soldierType soldier_selected;
		soldierType[] Decrypt_soldiers_cellar;  // 解密后的士兵仓库，战斗结束用于记录
		gameStage game_stage;
	}

	// gameInfo gameInfoInit;

	// 全局信息
	struct globalInfo {
		uint256 cities_remain;
		gameStage game_stage;
		uint256 bonus_pool;     //奖金池
		uint256 produce_rate;   //使用整型保存，需要时进行转换
	}

	// 城池信息
	struct cityInfo {
		uint256 city_id;
		string city_name;
		uint256 defense_index;   //使用整型保存，需要时进行转换
		uint256 realtime_price;
		bool if_be_occupied;
		address belong_player;
		uint256 produced_bonus;
	}

	// 玩家信息
	struct playerInfo {
		address player_address;
		uint256 game_id;     //确定玩家处在哪一场匹配游戏中

		bool is_attacker;
		bool is_defender;
		address opponent;

		// 3种状态进行切换
		bool be_attacked_request;
		bool before_battle;
		bool in_battle;

		uint256 own_city_id;  // 0代表未拥有城池
		uint256 pointer;      // 0 - 4循环
		mapping (uint256 => gameInfo) game_data;   // 储存最近5场的战斗数据
	}

	// 竞标榜信息
	struct biddingRanking {
		uint256 ranking;
		address player_address;
		// uint256 ranking;      // 排名
		uint256 bidding_price;
		uint256 bidding_time;    //时间戳
	}

	// 冻结表信息
	struct frozenInfo {
		address player_address;
		uint256 frozen_rank;    // 冻结等级
		uint256 frozen_time;    // 冻结时间戳
	}

	/**
		构造变量，将数据存储在区块链上
	*/
	// 玩家注册
	mapping (address => bool) players;
	// 设置同时开启的游戏场次为100
	uint256[100] gameIdArray;
	// 游戏过程数据，在构造函数中初始化
	
	/**
		数据表结构
	*/
	// game_id => globalInfo
	mapping (uint256 => globalInfo) globalTable;
	// game_id => city_id => cityInfo
	mapping (uint256 => mapping (uint256 => cityInfo)) citiesTable;
	// player_address => playerInfo
	mapping (address => playerInfo) playersTable;
	// game_id => ranking => biddingRanking
	mapping (uint256 => mapping (uint256 => biddingRanking)) rankingTable;
	// player_address => frozenInfo
	mapping (address => frozenInfo) frozenTable;

	/**
        @notice 构造函数，初始化数据
    */
	constructor () public {
		// 设置root地址
		rootAddr = msg.sender;

		// 初始化gameInfoInit变量
		// gameInfoInit = gameInfo({
		// 	round_id: 0,
		// 	all_soldiers_point: 0,
		// 	current_soldiers_point: 0,
		// 	soldiers_quantity: 0,
		// 	crypto_soldiers_cellar: new uint256[](0),
		// 	soldier_selected: soldierType.none,
		// 	Decrypt_soldiers_cellar: new soldierType[](0),
		// 	game_stage: gameStage.RUNNING
		// });

		// gameInfoInit.round_id = 0;
		// gameInfoInit.all_soldiers_point = 0;
		// gameInfoInit.current_soldiers_point = 0;
		// gameInfoInit.solidity_quantity = 0;
		// gameInfoInit.soldier_selected = 0;
		// gameInfoInit.game_stage = gameStage.RUNNING;

		// 初始化士兵战力mapping
		// soldiersPoint[0] = 0;
		// soldiersPoint[1] = 10 * 10 ** PRECISION;
		// soldiersPoint[2] = 15 * 10 ** PRECISION;
		// soldiersPoint[3] = 20 * 10 ** PRECISION;
		// soldiersPoint[4] = 25 * 10 ** PRECISION;
		// soldiersPoint[5] = 30 * 10 ** PRECISION;

		// 初始化城池名称mapping
		// cityName[0] = "";
		// cityName[1] = "长安";
		// cityName[2] = "燕京";
		// cityName[3] = "洛阳";
		// cityName[4] = "金陵";
		// cityName[5] = "荆州";
		// cityName[6] = "汴州";
		// cityName[7] = "临安";
		// cityName[8] = "徐州";
		// cityName[9] = "襄阳";
		// cityName[10] = "汉中";
		// cityName[11] = "咸阳";
		// cityName[12] = "益州";
		// cityName[13] = "晋阳";
		// cityName[14] = "广陵";
		// cityName[15] = "长平";
		// cityName[16] = "邯郸";
		// cityName[17] = "临淄";
		// cityName[18] = "曲阜";
		// cityName[19] = "寿春";
		// cityName[20] = "平城";
		// cityName[21] = "当阳";
		// cityName[22] = "江陵";
		// cityName[23] = "雁门";
		// cityName[24] = "夷陵";
		// cityName[25] = "东瀛";

		// 初始化城池守城指数mapping
		// cityDefenseIndex[0] = 0;
		// cityDefenseIndex[1] = 120;
		// cityDefenseIndex[2] = 118;
		// cityDefenseIndex[3] = 117;
		// cityDefenseIndex[4] = 116;
		// cityDefenseIndex[5] = 115;
		// cityDefenseIndex[6] = 114;
		// cityDefenseIndex[7] = 112;
		// cityDefenseIndex[8] = 110;
		// cityDefenseIndex[9] = 109;
		// cityDefenseIndex[10] = 108;
		// cityDefenseIndex[11] = 107;
		// cityDefenseIndex[12] = 106;
		// cityDefenseIndex[13] = 100;
		// cityDefenseIndex[14] = 100;
		// cityDefenseIndex[15] = 100;
		// cityDefenseIndex[16] = 100;
		// cityDefenseIndex[17] = 100;
		// cityDefenseIndex[18] = 100;
		// cityDefenseIndex[19] = 100;
		// cityDefenseIndex[20] = 100;
		// cityDefenseIndex[21] = 100;
		// cityDefenseIndex[22] = 100;
		// cityDefenseIndex[23] = 100;
		// cityDefenseIndex[24] = 100;
		// cityDefenseIndex[25] = 100;
	}

	modifier onlyRoot() {
		require(rootAddr == msg.sender, "only root address can do this action");
		_;
	}

	modifier checkReady() {
		// 游戏参数检查
		require(PRECISION != 0, "please set precision");
		require(CITY_NUM != 0, "please set city number");
		require(ENTER_FEE != 0, "please set enter fee");
		require(CITY_PRICE != 0, "please set city price");
		require(SOLDIER_NUM != 0, "please set soldier type number");
		require(INTERVAL != 0, "please set interval");

		// 游戏内容数据检查
		require(soldiersPoint[SOLDIER_NUM] != 0, "please set soldiersPoint");
		require(bytes(cityName[CITY_NUM]).length != 0, "please set cityName");
		require(cityDefenseIndex[CITY_NUM] != 0, "please set cityDefenseIndex");
		_;
	}

	// gameInfo gameInfoInit = gameInfo({
	// 	round_id : 0,
	// 	all_soldiers_point : 0,
	// 	current_soldiers_point : 0,
	// 	solidity_quantity : 0,
	// 	soldier_selected : 0,
	// 	stage : gameStage.RUNNING
	// });

	// gameInfoInit.round_id = 0;
	// gameInfoInit.all_soldiers_point = 0;
	// gameInfoInit.current_soldiers_point = 0;
	// gameInfoInit.solidity_quantity = 0;
	// gameInfoInit.soldier_selected = 0;
	// gameInfoInit.stage = RUNNING;


	/**
        @notice 设置游戏资产合约地址
        @param addr      游戏资产合约地址
    */
	function setAssetAddr(address addr) public onlyRoot() {
		gameAssetAddr = addr;
	}

	/**
        @notice 设置资产的precision
        @param precision       precision，用于避免solidity中出现浮点数
    */
	function setPrecision(uint256 precision) public onlyRoot() {
    	PRECISION = precision;
    }

    /**
        @notice 设置城池数量
        @param cityNum         城池数量(25)
    */
    function setCityNum(uint256 cityNum) public onlyRoot() {
    	CITY_NUM = cityNum;
    	PLAYER_NUM = 2 * cityNum;
    }

    /**
        @notice 设置游戏进场费用
        @param enterFee         游戏入场费(50 SIG)
    */
    function setEnterFee(uint256 enterFee) public onlyRoot() {
    	require(PRECISION != 0, "please set precision first");
    	ENTER_FEE = enterFee * 10 ** PRECISION;
    }

    /**
        @notice 设置城池起拍初始价格
        @param cityPrice         城池初始价，用于竞标(6 SIG)
    */
    function setCityPrice(uint256 cityPrice) public onlyRoot() {
    	require(PRECISION != 0, "please set precision first");
    	CITY_PRICE = cityPrice * 10 ** PRECISION;
    }

    /**
        @notice 设置士兵类型数量
        @param soldierNum        士兵类型数量(5)
    */
    function setSoldierNum(uint256 soldierNum) public onlyRoot() {
    	SOLDIER_NUM = soldierNum;
    }

    /**
        @notice 设置出产率更新时间间隔
        @param interval          出产率每隔interval秒(10 s)更新一次
    */
    function setInterval(uint256 interval) public onlyRoot() {
    	INTERVAL = interval;
    	INTERVAL_NUM = 3600 / interval;
    }

    /**
        @notice 设置兵种战力
        @param soldiersPointList        各个兵种战力列表
    */
    function setSoldiersPoint(uint256[] memory soldiersPointList) public onlyRoot() {
    	require(PRECISION != 0, "please set precision first");
    	require(soldiersPointList.length == SOLDIER_NUM + 1, "soldiersPoint length not match");

    	for (uint256 i = 0; i < soldiersPointList.length; ++i) {
    		soldiersPoint[i] = soldiersPointList[i] * 10 ** PRECISION;
    	}
    }

    /**
        @notice 设置城池名称
        @param cityNameList             城池名称列表
    */
    function setCityName(byte[64][] memory cityNameList) public onlyRoot() {
    	require(CITY_NUM != 0, "please set the number of city first");
    	require(cityNameList.length == CITY_NUM + 1, "the numbber of city not match");

    	for (uint256 i = 0; i < cityNameList.length; ++i) {
    		bytes memory b = new bytes(64);
    		for (uint256 j = 0; j < 64; ++j) {
    			b[j] = cityNameList[i][j];
    		}
    		string memory cityNameStr = bytesToString(b, b.length);
    		cityName[i] = cityNameStr;
    	}
    }

    /**
        @notice 设置城池防御指数τ
        @param cityDefenseIndexList      城池防御指数列表
    */
    function setCityDefenseIndex(uint256[] memory cityDefenseIndexList) public onlyRoot() {
    	require(CITY_NUM != 0, "please set the number of city first");
    	require(cityDefenseIndexList.length == CITY_NUM + 1, "the numbber of city not match");

    	for (uint256 i = 0; i < cityDefenseIndexList.length; ++i) {
    		cityDefenseIndex[i] = cityDefenseIndexList[i];
    	}
    }

	/**
        @notice 玩家注册后，将玩家地址注册游戏，并且初始化玩家信息表
        @param playerAddress      玩家地址
    */
    function register(address playerAddress) public onlyRoot() returns (bool) {
    	// 地址注册
    	players[playerAddress] = true;

    	// 初始化玩家信息
  //   	playerInfo memory p;
  //   	gameInfo memory g = gameInfo({
		// 	round_id: 0,
		// 	all_soldiers_point: 0,
		// 	current_soldiers_point: 0,
		// 	soldiers_quantity: 0,
		// 	soldiers_cellar: new uint256[](0),
		// 	soldier_selected: 0,
		// 	game_stage: gameStage.RUNNING
		// });

  //   	p.player_address = playerAddress;
  //   	p.game_id = 0;
  //   	p.is_attacker = false;
  //   	p.is_defender = false;
  //   	p.opponent = address(0x0);
  //   	p.be_attacked_request = false;
  //   	p.before_battle = false;
  //   	p.in_battle = false;
  //   	p.own_city_id = 0;
  //   	p.game_data[1] = g;

    	// playersTable[playerAddress] = p;
    	// gameInfo[] memory aa = new gameInfo[](0);
    	playersTable[playerAddress] = playerInfo({
    		player_address: playerAddress,
    		game_id: 0,
    		is_attacker: false,
    		is_defender: false,
    		opponent: address(0x0),
    		be_attacked_request: false,
    		before_battle: false,
    		in_battle: false,
    		own_city_id: 0,
    		pointer: 0
    	});
  //   	playersTable[playerAddress].game_id = 0;
		// playersTable[playerAddress].is_attacker = false;
		// playersTable[playerAddress].is_defender = false;
		// playersTable[playerAddress].opponent = address(0x0);
		// playersTable[playerAddress].be_attacked_request = false;
		// playersTable[playerAddress].before_battle = false;
		// playersTable[playerAddress].in_battle = false;
		// playersTable[playerAddress].own_city_id = 0;
		// playersTable[playerAddress].game_data = gameInfoInit;
		return true;
    }
	/**
        @notice 玩家登录。检查玩家是否注册，并且返回gameId
        @param playerAddress      玩家地址
    */
	function login(address playerAddress) public view returns (uint256) {
		// 查询players表，验证玩家是否注册
		require(players[playerAddress] == true, "not register");
		// 查询playerTable表，查看玩家数据是否存在
		uint256 gameId = playersTable[playerAddress].game_id;

		return gameId;
	}

	/**
        @notice 更新游戏阶段
        @param gameId      游戏id
        @param stage       游戏阶段
    */
	function updateGameStage(uint256 gameId, gameStage stage) public onlyRoot() {
		require(stage == gameStage.START || stage == gameStage.BIDDING || stage == gameStage.RUNNING || stage == gameStage.SETTLING || stage == gameStage.END, "stage not match");
		globalTable[gameId].game_stage = stage;
	}

	/**
        @notice 更新bonus出产率
        @param gameId              游戏id
        @param leftIntervalNum     游戏剩余周期数(10秒为单位)
    */
	function updateCityBonus(uint256 gameId, uint256 leftIntervalNum) public onlyRoot() returns(uint256) {
		// 更新出产率
		uint256 produceRate = globalTable[gameId].produce_rate;
		uint256 bonusPool = globalTable[gameId].bonus_pool;
		uint256 citiesRemain = globalTable[gameId].cities_remain;
		if (citiesRemain == CITY_NUM) 
		{
			// 全部玩家都离开了城池，出产率设为0
			globalTable[gameId].produce_rate = 0;
		}
		else 
		{
			// 更新
			globalTable[gameId].produce_rate = bonusPool / (CITY_NUM - citiesRemain) / leftIntervalNum;
		}

		// 更新城池bonus
		uint256 incBonus = produceRate;
		for (uint256 i = 1; i <= CITY_NUM; ++i) 
		{
			if (citiesTable[gameId][i].if_be_occupied) 
			{
				citiesTable[gameId][i].produced_bonus += incBonus;
			}
		}
		return globalTable[gameId].produce_rate;
	}

	/**
        @notice 冻结玩家
        @param playerAddresses      玩家地址列表
        @param rank                 冻结等级
        @param time                 冻结时间戳
    */
	function freezePlayer(address[] calldata playerAddresses, uint256[] calldata rank, uint256[] calldata time) external onlyRoot() {
		require(playerAddresses.length == rank.length && playerAddresses.length == time.length, "playerAddresses length not match");
		for (uint256 i = 0; i < playerAddresses.length; ++i)
		{
			// 3种冻结等级，一天/三天/一周
			require(rank[i] == 1 || rank[i] == 2 || rank[i] == 3, "freeze rank error");
			// 更新冻结表
			address playerAddress = playerAddresses[i];
			frozenTable[playerAddress].player_address = playerAddress;
			frozenTable[playerAddress].frozen_rank = rank[i];
			frozenTable[playerAddress].frozen_time = time[i];
		}
	}

	/**
        @notice 解冻玩家
        @param playerAddresses      玩家地址列表
        @param time                 解冻时间戳
    */
	function unFreezePlayer(address[] calldata playerAddresses, uint256[] calldata time) external onlyRoot() {
		require(playerAddresses.length == time.length, "playerAddresses length not match");
		for (uint256 i = 0; i < playerAddresses.length; ++i)
		{
			uint256 end = time[i];
			uint256 start = frozenTable[playerAddresses[i]].frozen_time;
			uint256 interval = end - start;
			uint256 rank = frozenTable[playerAddresses[i]].frozen_rank;
			if (rank == 1) {
				require(interval > 86400, "unfreeze failed");
			}
			else if (rank == 2) {
				require(interval > 259200, "unfreeze falied");
			}
			else if (rank == 3) {
				require(interval > 604800, "unfreeze falied");
			}
			else {
				require(false, "input rank error");
			}
			delete frozenTable[playerAddresses[i]];
		}
	}

	/**
        @notice 完成匹配后，玩家进入游戏
        @param playerAddresses      成功匹配的所有玩家地址
    */
	function startGame(address[] calldata playerAddresses) external onlyRoot() checkReady() returns (uint256) {
		// 确定游戏Id
		uint256 gameId;
		for (uint256 i = 1; i < gameIdArray.length; ++i)
		{
			if (gameIdArray[i] == 0)
			{
				// 找到gameIdArray中第一个不为0的下标，设置为本场游戏的gameId
				gameId = i;
				gameIdArray[i] = 1;
				break;
			}
		}

		// 更新playersTable表中game_id信息
		for (uint256 i = 0; i < playerAddresses.length; ++i)
		{
			address playerAddress = playerAddresses[i];
			playersTable[playerAddress].game_id = gameId;
		}

		// 初始化globalTable表数据
		globalTable[gameId].cities_remain = CITY_NUM;
		globalTable[gameId].game_stage = gameStage.BIDDING;
		globalTable[gameId].bonus_pool = playerAddresses.length * ENTER_FEE;
		globalTable[gameId].produce_rate = 0;

		// 初始化citiesTable表数据
		for (uint256 i = 1; i <= CITY_NUM; ++i)
		{
			citiesTable[gameId][i].city_id = i;
			citiesTable[gameId][i].city_name = cityName[i];
			citiesTable[gameId][i].defense_index = cityDefenseIndex[i];
			citiesTable[gameId][i].realtime_price = CITY_PRICE;
			citiesTable[gameId][i].if_be_occupied = false;
			citiesTable[gameId][i].belong_player = address(0x0);
			citiesTable[gameId][i].produced_bonus = 0;
		}
		return gameId;
	}

	/**
        @notice 每回合竞标结束后更新链上竞标表
        @param gameId                游戏id
        @param ranking               竞标排名
        @param playerAddresses       竞标玩家地址
        @param price                 竞标价格
        @param time                  竞标时间
    */
	function updateRankingTb(uint256 gameId, uint256[] calldata ranking, address[] calldata playerAddresses, uint256[] calldata price, uint256[] calldata time) external onlyRoot() {
		require(globalTable[gameId].game_stage == gameStage.BIDDING, "game is not in bidding stage");
		require(ranking.length == playerAddresses.length && ranking.length == price.length && ranking.length == time.length, "ranking table size not match");
		for (uint256 i = 0; i < ranking.length; ++i) 
		{
			// 做一些验证
			require(ranking[i] >= 1 && ranking[i] <= CITY_NUM, "ranking is out of range");
			require(playersTable[playerAddresses[i]].game_id == gameId, "player gameId not match");

			// 更新ranking table
			rankingTable[gameId][ranking[i]].ranking = i;
			rankingTable[gameId][ranking[i]].player_address = playerAddresses[i];
			rankingTable[gameId][ranking[i]].bidding_price = price[i];
			rankingTable[gameId][ranking[i]].bidding_time = time[i];
		}
	}

	/**
        @notice 竞标结束后分配城池
        @param gameId      游戏id
        @param playerAddresses     城主地址
        @param cityIds             分配城池的id
        @param price               占领费用
    */
	function allocateCity(uint256 gameId, address[] calldata playerAddresses, uint256[] calldata cityIds, uint256[] calldata price) external onlyRoot() {
		// 确保游戏global状态正确
		require(globalTable[gameId].game_stage == gameStage.BIDDING, "game is not in bidding stage");
		require(globalTable[gameId].cities_remain > 0, "No city remained!");
		require(playerAddresses.length == cityIds.length && playerAddresses.length == price.length, "playerAddresses length not match");
		for (uint256 i = 0; i < playerAddresses.length; ++i) 
		{
			// 做一些验证
			playerInfo storage player = playersTable[playerAddresses[i]];
			cityInfo storage city = citiesTable[gameId][cityIds[i]];
			
			require(player.game_id == gameId, "gameId not match");
			require(player.is_attacker == false, "You now are an attacker!");
			require(player.is_defender == false, "You have already occupied another city!");
			require(city.if_be_occupied == false, "The city has been occupied before.");

			// 更新players table
			player.is_defender = true;
			player.own_city_id = cityIds[i];

			// 更新cities table
			city.realtime_price = price[i];
			city.if_be_occupied = true;
			city.belong_player = playerAddresses[i];

			// 更新global table
			globalTable[gameId].cities_remain -= 1;
			globalTable[gameId].bonus_pool += price[i];
		}
		// 初始化产出率
		uint256 citiesRemain = globalTable[gameId].cities_remain;
		uint256 produceRate = globalTable[gameId].bonus_pool / (CITY_NUM - citiesRemain) / INTERVAL_NUM;
		globalTable[gameId].produce_rate = produceRate;
	}

	/**
        @notice 玩家占领空余城池
        @param gameId              游戏id
        @param playerAddress       玩家地址
        @param cityId              城池id
        @param amount              城池当前价格
        @param symbol              SIG金币的symbol
    */
	function occupyCity(uint256 gameId, address playerAddress, uint256 cityId, uint256 amount, string calldata symbol) external {
		// 确保游戏状态正确
		require(globalTable[gameId].game_stage == gameStage.RUNNING, "game is not in running stage");
		require(globalTable[gameId].cities_remain > 0, "no city left");

		playerInfo storage player = playersTable[playerAddress];
		cityInfo storage city = citiesTable[gameId][cityId];
		// 游戏id正确
		require(player.game_id == gameId, "gameId not match");
		// 确保玩家身份
		require(player.is_attacker == false && player.is_defender == false, "not a vagrant");
		// 确保城池为空
		require(city.if_be_occupied = false, "city not empty");
		// 确保价格正确
		require(amount == city.realtime_price, "city price not match");
		// 缴纳占领费
		SiegeAsset siegeAsset = SiegeAsset(gameAssetAddr);
		siegeAsset.transfer(msg.sender, rootAddr, amount, symbol, "player occupies city");

		// 更新数据
		player.is_defender = true;
		player.own_city_id = cityId;

		city.if_be_occupied = true;
		city.belong_player = playerAddress;

		globalTable[gameId].cities_remain -= 1;
		// 产出率此时不更新
	}

	/**
        @notice 玩家离开城池
        @param gameId              游戏id
        @param playerAddress       城主地址
        @param symbol              SIG金币的symbol
    */
	function leaveCity(uint256 gameId, address playerAddress, string calldata symbol) external onlyRoot() {
		// 确保游戏状态正确
		require(globalTable[gameId].game_stage == gameStage.RUNNING, "game is not in running stage");
		require(globalTable[gameId].cities_remain < CITY_NUM, "All the city is not occupied!");

		playerInfo storage player = playersTable[playerAddress];
		// 游戏id正确
		require(player.game_id == gameId, "gameId not match");

		// 查找玩家拥有的城池
		uint256 cityId = player.own_city_id;
		cityInfo storage city = citiesTable[gameId][cityId];
		// 验证该城池是否为玩家所拥有
		require(city.belong_player == playerAddress, "You don't have this city!");

		uint256 producedBonus = city.produced_bonus;
		// 使用底层call调用gameItem合约中的safeTransferFrom函数
		// bytes4 methodId = bytes4(keccak256("safeTransferFrom(address, address, uint256, uint256, bytes)"));
		// gameItemAddr.call(methodId, siegeTeamAddr, playerAddress, sigId, producedBonus / PRECISION, "player leave city");
		// GameItem(gameItemAddr).safeTransferFrom(siegeTeamAddr, playerAddress, sigId, 1, "player leave city");
		SiegeAsset siegeAsset = SiegeAsset(gameAssetAddr);
		siegeAsset.transfer(rootAddr, playerAddress, producedBonus, symbol, "player leave city");

		// 重置玩家数据
		player.is_attacker = false;
		player.is_defender = false;
		player.opponent = address(0x0);
		player.be_attacked_request = false;
		player.before_battle = false;
		player.in_battle = false;
		player.own_city_id = 0;

		// 重置城池数据
		city.if_be_occupied = false;
		city.belong_player = address(0x0);
		city.produced_bonus = 0;

		// 更新全局数据
		globalTable[gameId].cities_remain += 1;
		// 如果所有玩家都离开城池，则出产率为0
		if (globalTable[gameId].cities_remain == CITY_NUM) {
			globalTable[gameId].produce_rate = 0;
		}
		else {
			// 出产率更新(由于玩家离开而提高)
			// 由于每十秒自动更新，此处不予设计
		}
	}

	/**
        @notice 玩家发起进攻
        @param gameId              游戏id
        @param attackerAddress     进攻者地址
        @param defenderAddress     防守者地址
    */
	function attack(uint256 gameId, address attackerAddress, address defenderAddress) public onlyRoot() {
		// 确保游戏状态正确
		require(globalTable[gameId].game_stage == gameStage.RUNNING, "game is not in running stage");

		playerInfo storage defender = playersTable[defenderAddress];
		playerInfo storage attacker = playersTable[attackerAddress];
		// 游戏id正确
		require(
			defender.game_id == attacker.game_id && 
			attacker.game_id == gameId, "gameId not match");
		// 确保玩家为游民
		require(
			attacker.is_attacker == false &&
			attacker.is_defender == false, 
			"not a vagrant");
		// 确保被攻击者仅被该玩家攻击
		require(defender.is_defender == true, "target not a defender");
		require(
			defender.be_attacked_request == false &&
			defender.before_battle == false &&
			defender.in_battle == false, "target under an attacking now");
		// 标记进攻和防守者
		attacker.is_attacker = true;
		attacker.opponent = defenderAddress;

		defender.be_attacked_request = true;
		defender.opponent = attackerAddress;
	}

	/**
        @notice 玩家防御选择
        @param gameId              游戏id
        @param defenderAddress     防守者地址
        @param attackerAddress     进攻者地址
        @param cityId              城池id
        @param choice              防御者选择: 0 弃城; 1 防守
        @param symbol              SIG金币的symbol
    */
	function defense(uint256 gameId, address defenderAddress, address attackerAddress, uint256 cityId, uint256 choice, string calldata symbol) external onlyRoot() {
		require(choice == 0 || choice == 1, "choice must be 0 or 1");
		// 确保游戏状态正确
		require(globalTable[gameId].game_stage == gameStage.RUNNING, "game is not in running stage");

		playerInfo storage defender = playersTable[defenderAddress];
		playerInfo storage attacker = playersTable[attackerAddress];
		cityInfo storage city = citiesTable[gameId][cityId];
		// 游戏id正确
		require(
			attacker.game_id == defender.game_id && 
			attacker.game_id == gameId, "gameId not match");
		require(attacker.is_attacker == true, "not an attacker");
		require(defender.own_city_id == cityId, "city not belongs to you");
		require(defender.opponent == attackerAddress && attacker.opponent == defenderAddress, "opponent not match");
		require(defender.be_attacked_request == true, "not under attack");

		if (choice == 0) {
			// 城主离开城池
			this.leaveCity(gameId, defenderAddress, symbol);
			// 更新attacker数据
			attacker.is_defender = true;
			attacker.is_attacker = false;
			attacker.opponent = address(0x0);
			attacker.own_city_id = cityId;
			// 更新城池数据
			city.belong_player = attackerAddress;
		}
		else {
			// 城主选择防御
			defender.be_attacked_request = false;
			defender.before_battle = true;

			attacker.before_battle = true;
		}
	}

	/**
        @notice 玩家购买士兵
        @param gameId              游戏id
        @param amount              购买士兵所需要的金额
        @param soldiersbought      玩家购买的士兵(加密后)
        @param allSoldiersPoint    士兵总战力
        @param soldiersQuantity    士兵数量
        @param symbol              SIG金币的symbol
    */
	function buySoldiers(
		uint256 gameId,
		uint256 amount, 
		uint256[] calldata soldiersbought, 
		uint256 allSoldiersPoint, 
		uint256 soldiersQuantity, 
		string calldata symbol) external {
		// 确保游戏状态正确
		require(globalTable[gameId].game_stage == gameStage.RUNNING, "game is not in running stage");

		// 确保玩家状态正确
		playerInfo storage player = playersTable[msg.sender];
		require(player.game_id == gameId, "gameId not match");
		require(player.before_battle == true, "not in preparing stage");

		require(amount == allSoldiersPoint, "soldiersPoint and soldiersPrice not match");
		require(amount <= 100 * 10 ** PRECISION && amount >= 30 * 10 ** PRECISION, "invalid bought");

		// 购买转账，此时奖池不增加，战斗结束后统一结算
		SiegeAsset siegeAsset = SiegeAsset(gameAssetAddr);
		siegeAsset.transfer(msg.sender, rootAddr, amount, symbol, "player buys soldiers");

		// 添加战斗数据
		uint256 pointer = player.pointer;
		player.game_data[pointer].crypto_soldiers_cellar = soldiersbought;
		player.game_data[pointer].soldiers_quantity = soldiersQuantity;
		player.game_data[pointer].all_soldiers_point = allSoldiersPoint;
		player.game_data[pointer].current_soldiers_point = allSoldiersPoint;
	}

	/**
        @notice 玩家集结士兵出发
        @param gameId              游戏id
        @param playerAddress       玩家地址
    */
	function departure(uint256 gameId, address playerAddress) public onlyRoot() {
		// 确保游戏状态正确
		require(globalTable[gameId].game_stage == gameStage.RUNNING, "game is not in running stage");

		playerInfo storage player = playersTable[playerAddress];
		require(player.game_id == gameId, "gameId not match");
		require(player.before_battle == true && player.in_battle == false, "not in departure stage");
		uint256 pointer = player.pointer;
		// 更新玩家状态，切换至战斗状态
		player.before_battle = false;
		player.in_battle = true;

		// 初始化战斗数据
		player.game_data[pointer].round_id = 1;
	}

	/**
        @notice 玩家选择士兵
        @param gameId              游戏id
        @param playerAddress       玩家地址
        @param sType               选择的士兵
    */
	// function pickSoldier(uint256 gameId, address playerAddress, soldierType sType) public onlyRoot() {
	// 	// 确保游戏状态正确
	// 	require(globalTable[gameId].game_stage == gameStage.RUNNING, "game is not in running stage");

	// 	playerInfo storage player = playersTable[playerAddress];
	// 	require(player.game_id == gameId, "gameId not match");
	// 	require(player.in_battle == true, "not in battle stage");

	// 	uint256 pointer = player.pointer;
	// 	gameInfo storage gameData = player.game_data[pointer];
	// 	require(gameData.round_id <= 5, "battle is over");
	// 	require(gameData.is_round_over == true, "last round not over");

	// 	gameData.soldier_selected = sType;
	// 	gameData.is_round_over = false;     // 本轮开启标志
	// 	gameData.Decrypt_soldiers_cellar.push(sType);
	// }

	/**
        @notice 玩家选择士兵
        @param gameId              游戏id
        @param attackerAddress     进攻方地址
        @param defenderAddress     防守方地址
        @param aType               进攻方士兵选择
        @param dType               防守方士兵选择
    */
	function pickAndBattle(
		uint256 gameId, 
		address attackerAddress, 
		address defenderAddress, 
		soldierType aType, 
		soldierType dType) public onlyRoot() returns (string memory){
		// 确保游戏状态正确
		require(globalTable[gameId].game_stage == gameStage.RUNNING, "game is not in running stage");

		playerInfo storage attacker = playersTable[attackerAddress];
		playerInfo storage defender = playersTable[defenderAddress];
		// 玩家状态检测
		require(attacker.game_id == defender.game_id, "gameId not match");
		require(attacker.game_id == gameId, "gameId not match");
		require(attacker.in_battle == true, "attacker not in battle stage");
		require(defender.in_battle == true, "defender not in battle stage");

		uint256 attackerPointer = attacker.pointer;
		uint256 defenderPointer = defender.pointer;
		gameInfo storage attackerGameData = attacker.game_data[attackerPointer];
		gameInfo storage defenderGameData = defender.game_data[defenderPointer];
		require(attackerGameData.round_id <= 5, "battle is over");
		require(defenderGameData.round_id <= 5, "battle is over");

		attackerGameData.soldier_selected = aType;
		defenderGameData.soldier_selected = dType;
		attackerGameData.Decrypt_soldiers_cellar.push(aType);
		defenderGameData.Decrypt_soldiers_cellar.push(dType);

		int8 result = roundResult(aType, dType);
		require(result == 0 || result == 1 || result == -1, "roundResult got error");

		// 获取卡牌克制结果
		if (result == 1) {
			// attacker获胜
			attackerGameData.round_id += 1;

			defenderGameData.round_id += 1;
			defenderGameData.current_soldiers_point -= soldiersPoint[uint256(dType)];

			return("attacker wins this round");
		}
		else if (result == -1) {
			// attacker失败
			attackerGameData.round_id += 1;
			attackerGameData.current_soldiers_point -= soldiersPoint[uint256(aType)];

			defenderGameData.round_id += 1;
			return("defender wins this round");
		}
		else {
			// 战平
			attackerGameData.round_id += 1;
			attackerGameData.current_soldiers_point -= soldiersPoint[uint256(aType)];

			defenderGameData.round_id += 1;
			defenderGameData.current_soldiers_point -= soldiersPoint[uint256(dType)];

			return("tie");
		}
	}

	/**
        @notice 战斗结算
        @param gameId              游戏id
        @param attackerAddress     进攻方地址
        @param defenderAddress     防守方地址
        @param cityId              进攻城池id
        @param aAmount             进攻者购买士兵费用
        @param dAmount             防守者购买士兵费用
        @param symbol              金币SIG的symbol
    */
	function battleEnd(
		uint256 gameId, 
		address attackerAddress, 
		address defenderAddress, 
		uint256 cityId, 
		uint256 aAmount, 
		uint256 dAmount, 
		string calldata symbol) external onlyRoot() returns (string memory){
		// 确保游戏状态正确
		require(globalTable[gameId].game_stage == gameStage.RUNNING, "game is not in running stage");

		playerInfo storage attacker = playersTable[attackerAddress];
		playerInfo storage defender = playersTable[defenderAddress];
		cityInfo storage city = citiesTable[gameId][cityId];

		// // 玩家状态检测
		require(attacker.game_id == defender.game_id, "gameId not match");
		require(attacker.game_id == gameId, "gameId not match");
		require(attacker.in_battle == true, "attacker not in battle stage");
		require(defender.in_battle == true, "defender not in battle stage");
		require(city.belong_player == defenderAddress, "defender not owns this city");
		require(defender.own_city_id == cityId, "defender not owns this city");

		// uint256 attackerPointer = attacker.pointer;
		// uint256 defenderPointer = defender.pointer;
		// gameInfo storage attackerGameData = attacker.game_data[attacker.pointer];
		// gameInfo storage defenderGameData = defender.game_data[defender.pointer];
		// require(attackerGameData.round_id <= 5, "battle is over");
		// require(defenderGameData.round_id <= 5, "battle is over");

		int8 result = battleResult(
			attacker.game_data[attacker.pointer].current_soldiers_point, 
			defender.game_data[defender.pointer].current_soldiers_point, 
			cityId);
		require(result == 0 || result == 1 || result == -1, "battleResult got error");

		if (result == 1) {
			// 进攻者获胜
			// 城主离开城池
			this.leaveCity(gameId, defenderAddress, symbol);
			// 修改城池数据
			city.belong_player = attackerAddress;
			// 修改进攻者数据
			attacker.is_attacker = false;
			attacker.is_defender = true;
			attacker.opponent = address(0x0);
			attacker.in_battle = false;
			attacker.own_city_id = cityId;
			// 资产转移分配
			SiegeAsset siegeAsset = SiegeAsset(gameAssetAddr);
			// 进攻者拿回80%的购买士兵费用
			siegeAsset.transfer(rootAddr, attackerAddress, aAmount * 4 / 5, symbol, "attacker get back the paid fee");
			// 防守者将70%的购买士兵费用转移给进攻者
			siegeAsset.transfer(rootAddr, attackerAddress, dAmount * 7 / 10 , symbol, "defender pay the fee to attacker");
			// // 双方战斗记录指针加1
			// uint256 attackerNewPoint = incPointer(attacker.pointer);
			// uint256 defenderNewPoint = incPointer(defender.pointer);

			// attacker.pointer = attackerNewPoint;
			// defender.pointer = defenderNewPoint;

			// // 初始化战斗记录
			// attacker.game_data[attacker.pointer] = gameInfoInit;
			// defender.game_data[defender.pointer] = gameInfoInit;

			clearGameData(attacker);
			clearGameData(defender);

			return("attacker wins the battle");
		}
		else if (result == -1) {
			// 防守者获胜
			// 解除防守者战斗状态
			defender.opponent = address(0x0);
			defender.in_battle = false;
			// 重置进攻者状态
			attacker.is_attacker = false;
			attacker.is_defender = false;
			attacker.opponent = address(0x0);
			attacker.be_attacked_request = false;
			attacker.before_battle = false;
			attacker.in_battle = false;
			attacker.own_city_id = 0;
			// 资产转移分配
			SiegeAsset siegeAsset = SiegeAsset(gameAssetAddr);
			// 防守者拿回80%的购买士兵费用
			siegeAsset.transfer(rootAddr, defenderAddress, dAmount * 4 / 5, symbol, "defender get back the paid fee");
			// 进攻者将70%的购买士兵费用转移给防守者
			siegeAsset.transfer(rootAddr, defenderAddress, aAmount * 7 / 10, symbol, "attacker pay the fee to defender");

			clearGameData(attacker);
			clearGameData(defender);

			return("defender wins the battle");
		}
		else {
			// 战平
			// 解除防守者战斗状态
			defender.opponent = address(0x0);
			defender.in_battle = false;
			// 重置进攻者状态
			attacker.is_attacker = false;
			attacker.is_defender = false;
			attacker.opponent = address(0x0);
			attacker.be_attacked_request = false;
			attacker.before_battle = false;
			attacker.in_battle = false;
			attacker.own_city_id = 0;
			// 资产返还
			SiegeAsset siegeAsset = SiegeAsset(gameAssetAddr);
			siegeAsset.transfer(rootAddr, attackerAddress, aAmount, symbol, "attacker get back the paid fee");
			siegeAsset.transfer(rootAddr, defenderAddress, dAmount, symbol, "defender get back the paid fee");

			clearGameData(attacker);
			clearGameData(defender);

			return("tie");
		}
	}

	/**
        @notice 游戏结束，分发奖金
        @param gameId          游戏id
        @param playerAddresses 城主列表地址
        @param symbol          SIG金币的symbol
    */
	function settlement(uint256 gameId, address[] calldata playerAddresses, string calldata symbol) external onlyRoot() {
		// 确保游戏状态正确
		require(globalTable[gameId].game_stage == gameStage.SETTLING, "game is not in settlement stage");

		for (uint256 i = 0; i < playerAddresses.length; ++i) {
			address playerAddress = playerAddresses[i];
			playerInfo storage player = playersTable[playerAddress];
			// 游戏id正确
			require(player.game_id == gameId, "gameId not match");
			cityInfo storage city = citiesTable[gameId][player.own_city_id];

			require(city.belong_player == playerAddress, "player not owns this city");
			uint256 producedBonus = city.produced_bonus;
			// 将受益转账给玩家
			SiegeAsset siegeAsset = SiegeAsset(gameAssetAddr);
			siegeAsset.transfer(rootAddr, playerAddress, producedBonus,symbol, "got bonus");
		}
	}

	function endGame(uint256 gameId) public onlyRoot() {
		// 确保游戏状态正确
		require(globalTable[gameId].game_stage == gameStage.END, "game is not in end stage");


	}

	/**
        @notice 查询玩家信息表
        @param playerAddress      玩家地址
    */
    function getPlayersTablePart1(address playerAddress) public view returns (uint256, bool, bool, address, uint256) {
    	// 验证权限

    	uint256 game_id = playersTable[playerAddress].game_id;
    	bool is_attacker = playersTable[playerAddress].is_attacker;
    	bool is_defender = playersTable[playerAddress].is_defender;
    	address opponent = playersTable[playerAddress].opponent;
    	
    	uint256 own_city_id = playersTable[playerAddress].own_city_id;

    	return(game_id, is_attacker, is_defender, opponent, own_city_id);
    }

    /**
        @notice 查询玩家信息表
        @param playerAddress      玩家地址
    */
    function getPlayersTablePart2(address playerAddress) public view returns (bool, bool, bool) {
    	// 验证权限

    	bool be_attacked_request = playersTable[playerAddress].be_attacked_request;
    	bool before_battle = playersTable[playerAddress].before_battle;
    	bool in_battle = playersTable[playerAddress].in_battle;

    	return(be_attacked_request, before_battle, in_battle);
    }

    /**
        @notice 查询指定gameId、cityId的城池信息，未返回城池的名称和守城指数
        @param gameId      游戏id
        @param cityId      城池id
    */
    function getCitiesTable(uint256 gameId, uint256 cityId) public view returns (string memory name, uint256, uint256, bool, address, uint256) {
    	string memory city_name = cityName[cityId];
    	uint256 defense_index = cityDefenseIndex[cityId];
    	uint256 realtime_price = citiesTable[gameId][cityId].realtime_price;
    	bool if_be_occupied = citiesTable[gameId][cityId].if_be_occupied;
    	address belong_player = citiesTable[gameId][cityId].belong_player;
    	uint256 produced_bonus = citiesTable[gameId][cityId].produced_bonus;

    	return(city_name, defense_index, realtime_price, if_be_occupied, belong_player, produced_bonus);
    }

	/**
        @notice 内部函数，查询玩家信息表
        @param playerAddress      玩家地址
    */
    function _getPlayersTable(address playerAddress) internal view returns (playerInfo memory p) {

    	playerInfo memory player_info;
    	player_info.game_id = playersTable[playerAddress].game_id;
    	player_info.is_attacker = playersTable[playerAddress].is_attacker;
    	player_info.is_defender = playersTable[playerAddress].is_defender;
    	player_info.opponent = playersTable[playerAddress].opponent;
    	player_info.be_attacked_request = playersTable[playerAddress].be_attacked_request;
    	player_info.before_battle = playersTable[playerAddress].before_battle;
    	player_info.in_battle = playersTable[playerAddress].in_battle;
    	player_info.own_city_id = playersTable[playerAddress].own_city_id;
    	player_info.pointer = playersTable[playerAddress].pointer;
    	// player_info.game_data = playersTable[playerAddress].game_data;

    	return player_info;
    }

    /**
        @notice 内部函数，查询指定gameId、cityId的城池信息，未返回城池的名称和守城指数
        @param gameId      游戏id
        @param cityId      城池id
    */
    function _getCitiesTable(uint256 gameId, uint256 cityId) internal view returns (cityInfo memory c) {

    	cityInfo memory city_info;
    	city_info.city_name = citiesTable[gameId][cityId].city_name;
    	city_info.defense_index = citiesTable[gameId][cityId].defense_index;
    	city_info.realtime_price = citiesTable[gameId][cityId].realtime_price;
    	city_info.if_be_occupied = citiesTable[gameId][cityId].if_be_occupied;
    	city_info.belong_player = citiesTable[gameId][cityId].belong_player;
    	city_info.produced_bonus = citiesTable[gameId][cityId].produced_bonus;

    	return city_info;
    }

    function bytesToString(bytes memory x, uint length) public pure returns (string memory) {
        bytes memory bytesString = new bytes(length);
        uint charCount = 0;
        for (uint j = 0; j < 32; j++) {
            byte char = x[j];
            if (char != 0) {
                bytesString[charCount] = char;
                charCount++;
            }
        }
        bytes memory bytesStringTrimmed = new bytes(charCount);
        for (uint j = 0; j < charCount; j++) {
            bytesStringTrimmed[j] = bytesString[j];
        }
        return string(bytesStringTrimmed);
    }

    function initGlobalTable(uint256 gameId) internal {
    	globalInfo memory GI = globalInfo({
    		cities_remain: CITY_NUM,
    		game_stage: gameStage.BIDDING,
    		bonus_pool: PLAYER_NUM * ENTER_FEE,
    		produce_rate: 0
    	});
    	globalTable[gameId] = GI;
    }

    function clearGlobalTable(uint256 gameId) internal {
    	globalInfo memory GI = globalInfo({
    		cities_remain: 0,
    		game_stage: gameStage.START,
    		bonus_pool: 0,
    		produce_rate: 0
    	});
    	globalTable[gameId] = GI;
    }

    function clearGameData(playerInfo storage player) internal {
    	gameInfo memory g = gameInfo({
    		round_id: 0,
			all_soldiers_point: 0,
			current_soldiers_point: 0,
			soldiers_quantity: 0,
			crypto_soldiers_cellar: new uint256[](0),
			soldier_selected: soldierType.none,
			Decrypt_soldiers_cellar: new soldierType[](0),
			game_stage: gameStage.RUNNING
    	});
    	uint256 pointer = player.pointer;
    	uint256 newPointer = incPointer(pointer);
    	player.pointer = newPointer;
    	player.game_data[newPointer] = g;
    }

    function incPointer(uint256 pointer) internal pure returns (uint256){
    	return (pointer + 1) % 5;
    }

    function roundResult(soldierType aType, soldierType dType) internal pure returns (int8) {
    	require(aType == soldierType.none 
    		|| aType == soldierType.infantry 
    		|| aType == soldierType.spearman 
    		|| aType == soldierType.shieldman 
    		|| aType == soldierType.archer 
    		|| aType == soldierType.cavalry,
    		"soldierType error");
    	require(dType == soldierType.none 
    		|| dType == soldierType.infantry 
    		|| dType == soldierType.spearman 
    		|| dType == soldierType.shieldman 
    		|| dType == soldierType.archer 
    		|| dType == soldierType.cavalry,
    		"soldierType error");
    	if (aType == soldierType.none && dType == soldierType.none) {
    		return 0;
    	}
    	else if (aType == soldierType.none && dType != soldierType.none) {
    		return -1;
    	}
    	else if (aType != soldierType.none && dType == soldierType.none) {
    		return 1;
    	}
    	else {
    		if (aType == soldierType.infantry) {
    			if (dType == soldierType.shieldman) {
    				return 1;
    			}
    			else if (dType == soldierType.archer || dType == soldierType.cavalry) {
    				return -1;
    			}
    			else {
    				return 0;
    			}
    		}
    		else if (aType == soldierType.spearman) {
    			if (dType == soldierType.cavalry) {
    				return 1;
    			}
    			else if (dType == soldierType.shieldman || dType == soldierType.archer) {
    				return -1;
    			}
    			else {
    				return 0;
    			}
    		}
    		else if (aType == soldierType.shieldman) {
    			if (dType == soldierType.spearman || dType == soldierType.archer) {
    				return 1;
    			}
    			else if (dType == soldierType.infantry || dType == soldierType.cavalry) {
    				return -1;
    			}
    			else {
    				return 0;
    			}
    		}
    		else if (aType == soldierType.archer) {
    			if (dType == soldierType.infantry || dType == soldierType.spearman) {
    				return 1;
    			}
    			else if (dType == soldierType.shieldman) {
    				return -1;
    			}
    			else {
    				return 0;
    			}
    		}
    		else {
    			if (dType == soldierType.infantry || dType == soldierType.shieldman) {
    				return 1;
    			}
    			else if (dType == soldierType.spearman) {
    				return -1;
    			}
    			else {
    				return 0;
    			}
    		}
    	}
    }

    function battleResult(uint256 attackerCurrentPoints, uint256 defenderCurrentPoints, uint256 cityId) internal view returns (int8) {
    	if (attackerCurrentPoints == defenderCurrentPoints) {
    		// 双方战平
    		return 0;
    	}
    	else if (attackerCurrentPoints < defenderCurrentPoints) {
    		// 防守者获胜
    		return -1;
    	}
    	else {
    		uint256 defenderNewPoints = defenderCurrentPoints * cityDefenseIndex[cityId];
    		if (attackerCurrentPoints == defenderNewPoints) {
    			// 双方战平
    			return 0;
    		}
    		else if (attackerCurrentPoints < defenderNewPoints) {
    			// 防守者获胜
    			return -1;
    		}
    		else {
    			// 进攻者获胜
    			return 1;
    		}
    	}
    }




	// function getGlobalTable() public view returns (uint256){
	// 	return globalTable.cities_remain;
	// }
}





