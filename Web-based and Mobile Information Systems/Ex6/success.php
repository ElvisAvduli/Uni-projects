<?php
session_start();

if (!isset($_SESSION['loggedin'])) {
    header('Location: login.php');
    exit();
}
?>

<!DOCTYPE html>
<html>
<head>
    <title>Login Successful</title>
    <style>
        body { font-family: Arial, sans-serif; max-width: 400px; margin: 0 auto; padding: 20px; }
        .success { padding: 15px; background: #dff0d8; color: #3c763d; border: 1px solid #d6e9c6; }
    </style>
</head>
<body>
    <div class="success">
        <h2>Welcome, <?php echo htmlspecialchars($_SESSION['username']); ?>!</h2>
        <p>You have successfully logged in.</p>
        <p><a href="login.php?logout=1">Logout</a></p>
    </div>
</body>
</html>