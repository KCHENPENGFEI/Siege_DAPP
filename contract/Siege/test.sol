pragma solidity ^0.5.0;

contract Complex {
    // struct Data {
    //     uint a;
    //     bytes3 b;
    //     mapping (uint => uint) map;
    // }
    // mapping (uint => mapping(bool => Data)) public data;
    enum gameStage {START, BIDDING, RUNNING, SETTLING, END}

    struct gameInfo {
		uint8 round_id;
		uint64 all_soldiers_point;
		uint64 current_soldiers_point;
		uint8 solidity_quantity;
		uint8[] soldiers_cellar;   // 玩家重要游戏信息，只提供合约内部查询
		uint8 soldier_selected;
		bool is_round_over;
		gameStage game_stage;
	}

    struct playerInfo {
		uint64 game_id;     //确定玩家处在哪一场匹配游戏中

		bool is_attacker;
		bool is_defender;
		address opponent;

		// 3种状态进行切换
		bool be_attacked_request;
		bool before_battle;
		bool in_battle;

		uint8 own_city_id;  // 0代表未拥有城池
		// gameInfo game_data;
		uint8 round_id;
		uint64 all_soldiers_point;
		uint64 current_soldiers_point;
		uint8 solidity_quantity;
		uint8[] soldiers_cellar;   // 玩家重要游戏信息，只提供合约内部查询
		uint8 soldier_selected;
		bool is_round_over;
		gameStage game_stage;
	}

	mapping (address => playerInfo) playersTable;
}