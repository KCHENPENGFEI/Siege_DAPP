pragma solidity ^0.5.0;

/**
 * Siege合约
 */

contract Siege {

	/**
		全局变量
	*/
	uint8 CITY_NUM = 25;
	uint64 START_BIDDING_PRICE = 6;
	address DEPLOY_ADDRESS = address(0x11BD06F184F3767FC02C7F27E812F51BC6F28B39);
	
	/**
		链上存储数据结构
	*/
	// 游戏状态
	enum gameStage {START, BIDDING, RUNNING, SETTLING, END}
	// 兵种类型
	enum soldierType {none, infantry, spearman, shieldman, archer, cavalry}

	// 游戏过程信息
	struct gameInfo {
		uint8 round_id = 0;
		uint64 all_soldiers_point = 0;
		uint64 current_soldiers_point = 0;
		uint8 solidity_quantity = 0;
		uint8[] soldiers_cellar;
		uint8 soldier_selected = 0;
		bool is_round_over = true;
		uint8 stage = RUNNING;
	}

	// 全局信息
	struct globalInfo {
		uint8 cities_remain = CITY_NUM;
		uint8 game_stage = 0;
		uint8 produce_rate;   //使用整型保存，需要时进行转换
	}

	// 城池信息
	struct cityInfo {
		string city_name;
		uint8 defense_index;   //使用整型保存，需要时进行转换
		uint64 realtime_price = START_BIDDING_PRICE;
		bool if_be_occupied = false;
		address belong_player = address(0x0);
		uint64 produced_bonus = 0;
	}

	// 玩家信息
	struct playerInfo {
		bool is_attacker = false;
		bool is_defender = false;
		address opponent = address(0x0);

		// 3种状态进行切换
		bool be_attacked_request = false;
		bool before_battle = false;
		bool in_battle = false;

		uint8 own_city_id = 0;  // 0代表未拥有城池
		gameInfo game_data;
	}

	// 竞标榜信息
	struct biddingRanking {
		uint8 ranking;      // 排名
		uint64 bidding_price;
		string bidding_time;
	}

	// 冻结表信息
	struct frozenInfo {
		uint64 frozen_rank;    // 冻结等级
		string frozen_time;
	}

	// 构造变量，将数据存储在区块链上
	globalInfo globalTable;
	mapping (uint8 => cityInfo) citiesTable;
	mapping (address => playerInfo) playersTable;
	mapping (address => biddingRanking) rankingTable;
	mapping (address => frozenInfo) frozenTable;

	function allStart() public {

		// 验证msg.sender是否为DEPLOY_ADDRESS
		require(msg.sender == DEPLOY_ADDRESS, "You don't have the authority");
		globalTable.cities_remain = CITY_NUM;
		globalTable.game_stage = START;
		globalTable.produce_rate = 0;
	}
}





