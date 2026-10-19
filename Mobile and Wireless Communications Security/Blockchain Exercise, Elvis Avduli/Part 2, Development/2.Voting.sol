pragma solidity 0.4.17;

contract StudentVoting {

    struct Student {
        address studentAddress;
        string name;
        string surname;
        uint age;
    }

    address public owner;

    mapping(address => Student) public students;
    mapping(address => bool) public isCandidate;
    mapping(address => uint) public votes;
    mapping(address => bool) public hasVoted;

    address[] public candidates;

    function StudentVoting() public {
        owner = msg.sender;
    }

    // Register student
    function registerStudent(string _name, string _surname, uint _age) public {
        students[msg.sender] = Student(msg.sender, _name, _surname, _age);
    }

    // Add candidate (self or owner)
    function addCandidate(address _candidate) public {
        require(
            msg.sender == owner || msg.sender == _candidate
        );

        require(!isCandidate[_candidate]);

        isCandidate[_candidate] = true;
        candidates.push(_candidate);
    }

    // Vote
    function vote(address _candidate) public {
        require(!hasVoted[msg.sender]);
        require(isCandidate[_candidate]);

        votes[_candidate]++;
        hasVoted[msg.sender] = true;
    }

    // Get winner
    function getWinner() public view returns (address) {
        uint maxVotes = 0;
        address winner;

        for (uint i = 0; i < candidates.length; i++) {
            if (votes[candidates[i]] > maxVotes) {
                maxVotes = votes[candidates[i]];
                winner = candidates[i];
            }
        }

        return winner;
    }
}