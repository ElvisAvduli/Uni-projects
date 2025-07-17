<?php
session_start();

// Database connection
$db = new mysqli('localhost', 'root', '', 'login_system');
if ($db->connect_error) {
    die("Connection failed: " . $db->connect_error);
}

if ($db->connect_error) {
    die("Connection failed: " . $db->connect_error);
}

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $username = $_POST['username'] ?? '';
    $password = $_POST['password'] ?? '';
    $remember = isset($_POST['remember']);

    // INSECURE: Direct password comparison (no hashing)
    $stmt = $db->prepare("SELECT * FROM users WHERE username = ? AND password = ?");
    $stmt->bind_param("ss", $username, $password);
    $stmt->execute();
    $result = $stmt->get_result();

    if ($result->num_rows === 1) {
        // Login successful
        if ($remember) {
            // Store credentials in cookies (INSECURE)
            setcookie('remember_username', $username, time() + (30 * 24 * 60 * 60));
            setcookie('remember_password', $password, time() + (30 * 24 * 60 * 60));
            setcookie('remember_me', '1', time() + (30 * 24 * 60 * 60));
        } else {
            // Clear cookies
            setcookie('remember_username', '', time() - 3600);
            setcookie('remember_password', '', time() - 3600);
            setcookie('remember_me', '', time() - 3600);
        }

        // Set session
        $_SESSION['loggedin'] = true;
        $_SESSION['username'] = $username;

        // Redirect to success page
        header('Location: success.php');
        exit();
    } else {
        // Login failed
        header('Location: login.php?error=Invalid+username+or+password');
        exit();
    }
} else {
    header('Location: login.php');
    exit();
}
?>