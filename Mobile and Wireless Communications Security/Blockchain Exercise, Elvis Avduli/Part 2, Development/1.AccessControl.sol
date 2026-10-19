pragma solidity 0.4.17;

contract UserManagement {

    struct User {
        address userAddress;
        string name;
        string surname;
        uint age;
        uint grade;
        string occupation; // "student" or "teacher"
    }

    mapping(address => User) public users;

    // Access control mapping
    mapping(address => mapping(address => bool)) public accessGranted;

    // Add user
    function addUser(
        string _name,
        string _surname,
        uint _age,
        uint _grade,
        string _occupation
    ) public {
        users[msg.sender] = User(
            msg.sender,
            _name,
            _surname,
            _age,
            _grade,
            _occupation
        );
    }

    // Internal check if teacher
    function isTeacher(address _user) internal view returns (bool) {
        return keccak256(users[_user].occupation) == keccak256("teacher");
    }

    // Get grade (restricted)
    function getGrade(address _user) public view returns (uint) {
        require(
            msg.sender == _user ||
            isTeacher(msg.sender) ||
            accessGranted[_user][msg.sender]
        );

        return users[_user].grade;
    }

    // Set grade (restricted)
    function setGrade(address _user, uint _grade) public {
        require(
            msg.sender == _user ||
            isTeacher(msg.sender) ||
            accessGranted[_user][msg.sender]
        );

        users[_user].grade = _grade;
    }

    // Grant access
    function grantAccess(address _to) public {
        accessGranted[msg.sender][_to] = true;
    }

    // Revoke access
    function revokeAccess(address _to) public {
        accessGranted[msg.sender][_to] = false;
    }
}