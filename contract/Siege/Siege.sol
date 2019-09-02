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

	/**
		全局变量
	*/
	uint256 PRECISION = 4;
	uint256 CITY_NUM = 25;
	uint256 ENTER_FEE = 50 * 10 ** PRECISION;
	uint256 START_BIDDING_PRICE = 6 * 10 ** PRECISION;
	uint256 INTERVAL = 10;
	uint256 INTERVAL_NUM = 360;  // 300 / 10

	// address deploy_address = 0x72ba7d8e73fe8eb666ea66babc8116a41bfb10e2;
	// gameItem合约地址
	//address gameItemAddr = 0x429F382e15054439B0Bc4Fc1139E729D4dC5E578;
	//address siegeTeamAddr = 0x11BD06F184F3767Fc02c7f27E812f51BC6f28b39;


	address rootAddr;
	address gameAssetAddr;
	
	/**
		链上存储数据结构
	*/
	// 游戏状态
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
		uint256[] soldiers_cellar;   // 玩家重要游戏信息，只提供合约内部查询
		uint256 soldier_selected;
		bool is_round_over;
		gameStage game_stage;
	}

	gameInfo gameInfoInit;

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
		uint256[] point;      // 0 - 4循环
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
		gameInfoInit = gameInfo({
			round_id: 0,
			all_soldiers_point: 0,
			current_soldiers_point: 0,
			soldiers_quantity: 0,
			soldiers_cellar: new uint256[](0),
			soldier_selected: 0,
			is_round_over: true,
			game_stage: gameStage.RUNNING
		});

		// gameInfoInit.round_id = 0;
		// gameInfoInit.all_soldiers_point = 0;
		// gameInfoInit.current_soldiers_point = 0;
		// gameInfoInit.solidity_quantity = 0;
		// gameInfoInit.soldier_selected = 0;
		// gameInfoInit.is_round_over = true;
		// gameInfoInit.game_stage = gameStage.RUNNING;

		// 初始化士兵战力mapping
		soldiersPoint[0] = 0;
		soldiersPoint[1] = 10;
		soldiersPoint[2] = 15;
		soldiersPoint[3] = 20;
		soldiersPoint[4] = 25;
		soldiersPoint[5] = 30;

		// 初始化城池名称mapping
		cityName[0] = "";
		cityName[1] = "长安";
		cityName[2] = "燕京";
		cityName[3] = "洛阳";
		cityName[4] = "金陵";
		cityName[5] = "荆州";
		cityName[6] = "汴州";
		cityName[7] = "临安";
		cityName[8] = "徐州";
		cityName[9] = "襄阳";
		cityName[10] = "汉中";
		cityName[11] = "咸阳";
		cityName[12] = "益州";
		cityName[13] = "晋阳";
		cityName[14] = "广陵";
		cityName[15] = "长平";
		cityName[16] = "邯郸";
		cityName[17] = "临淄";
		cityName[18] = "曲阜";
		cityName[19] = "寿春";
		cityName[20] = "平城";
		cityName[21] = "当阳";
		cityName[22] = "江陵";
		cityName[23] = "雁门";
		cityName[24] = "夷陵";
		cityName[25] = "东瀛";

		// 初始化城池守城指数mapping
		cityDefenseIndex[0] = 0;
		cityDefenseIndex[1] = 120;
		cityDefenseIndex[2] = 118;
		cityDefenseIndex[3] = 117;
		cityDefenseIndex[4] = 116;
		cityDefenseIndex[5] = 115;
		cityDefenseIndex[6] = 114;
		cityDefenseIndex[7] = 112;
		cityDefenseIndex[8] = 110;
		cityDefenseIndex[9] = 109;
		cityDefenseIndex[10] = 108;
		cityDefenseIndex[11] = 107;
		cityDefenseIndex[12] = 106;
		cityDefenseIndex[13] = 100;
		cityDefenseIndex[14] = 100;
		cityDefenseIndex[15] = 100;
		cityDefenseIndex[16] = 100;
		cityDefenseIndex[17] = 100;
		cityDefenseIndex[18] = 100;
		cityDefenseIndex[19] = 100;
		cityDefenseIndex[20] = 100;
		cityDefenseIndex[21] = 100;
		cityDefenseIndex[22] = 100;
		cityDefenseIndex[23] = 100;
		cityDefenseIndex[24] = 100;
		cityDefenseIndex[25] = 100;
	}

	modifier onlyRoot() {
		require(rootAddr == msg.sender, "only root address can do this action");
		_;
	}

	// gameInfo gameInfoInit = gameInfo({
	// 	round_id : 0,
	// 	all_soldiers_point : 0,
	// 	current_soldiers_point : 0,
	// 	solidity_quantity : 0,
	// 	soldier_selected : 0,
	// 	is_round_over : true,
	// 	stage : gameStage.RUNNING
	// });

	// gameInfoInit.round_id = 0;
	// gameInfoInit.all_soldiers_point = 0;
	// gameInfoInit.current_soldiers_point = 0;
	// gameInfoInit.solidity_quantity = 0;
	// gameInfoInit.soldier_selected = 0;
	// gameInfoInit.is_round_over = true;
	// gameInfoInit.stage = RUNNING;


	/**
        @notice 设置游戏资产合约地址
        @param addr      游戏资产合约地址
    */
	function setAssetAddr(address addr) public onlyRoot() {
		gameAssetAddr = addr;
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
		// 	is_round_over: true,
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
    		point: new uint256[](0)
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
	function startGame(address[] calldata playerAddresses) external onlyRoot() returns (uint256) {
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
			citiesTable[gameId][i].realtime_price = START_BIDDING_PRICE;
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
		siegeAsset.transfer(rootAddr, playerAddress, producedBonus / PRECISION, symbol, "player leave city");

		// 更新玩家数据
		player.is_defender = false;
		player.own_city_id = 0;

		// 更新城池数据
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
			// 更新defender数据
			defender.be_attacked_request = false;
			defender.opponent = address(0x0);
			// 更新attacker数据
			attacker.is_defender = true;
			attacker.is_attacker = false;
			attacker.opponent = address(0x0);
			attacker.own_city_id = cityId;
			// 更新城池数据
			city.belong_player = attackerAddress;
			city.produced_bonus = 0;
		}
		else {
			// 城主选择防御
			defender.be_attacked_request = false;
			defender.before_battle = true;

			attacker.before_battle = true;
		}
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
    	player_info.point = playersTable[playerAddress].point;
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




	// function getGlobalTable() public view returns (uint256){
	// 	return globalTable.cities_remain;
	// }
}





