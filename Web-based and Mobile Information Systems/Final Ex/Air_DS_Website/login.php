<?php
session_start();
require_once('db_connection.php');

// If user is already logged in, redirect to home page
if (isset($_SESSION['user_id'])) {
    header("Location: index.php");
    exit();
}

$registration_error = '';
$login_error = '';

// Handle registration
if (isset($_POST['register'])) {
    $first_name = trim($_POST['first_name']);
    $last_name = trim($_POST['last_name']);
    $username = trim($_POST['username']);
    $password = $_POST['password'];
    $email = trim($_POST['email']);
    
    // Validate first name and last name (only characters)
    if (!preg_match('/^[a-zA-Z]+$/', $first_name) || !preg_match('/^[a-zA-Z]+$/', $last_name)) {
        $registration_error = "First name and last name must contain only letters";
    }
    // Validate password (4-10 characters with at least one number)
    elseif (strlen($password) < 4 || strlen($password) > 10 || !preg_match('/[0-9]/', $password)) {
        $registration_error = "Password must be between 4 and 10 characters and contain at least one number";
    }
    // Validate email
    elseif (!filter_var($email, FILTER_VALIDATE_EMAIL) || strpos($email, '@') === false) {
        $registration_error = "Please enter a valid email address";
    }
    else {
        // Check if username already exists
        $stmt = $conn->prepare("SELECT user_id FROM users WHERE username = ?");
        $stmt->execute([$username]);
        if ($stmt->rowCount() > 0) {
            $registration_error = "Username already exists";
        }
        else {
            // Check if email already exists
            $stmt = $conn->prepare("SELECT user_id FROM users WHERE email = ?");
            $stmt->execute([$email]);
            if ($stmt->rowCount() > 0) {
                $registration_error = "Email already exists";
            }
            else {
                // Hash password and insert user
                $hashed_password = password_hash($password, PASSWORD_DEFAULT);
                $stmt = $conn->prepare("INSERT INTO users (first_name, last_name, username, password, email) VALUES (?, ?, ?, ?, ?)");
                if ($stmt->execute([$first_name, $last_name, $username, $hashed_password, $email])) {
                    // Registration successful, show login form
                    header("Location: login.php?registered=true");
                    exit();
                } else {
                    $registration_error = "Registration failed. Please try again.";
                }
            }
        }
    }
}

// Handle login
if (isset($_POST['login'])) {
    $username = trim($_POST['login_username']);
    $password = $_POST['login_password'];
    
    $stmt = $conn->prepare("SELECT user_id, password, first_name, last_name FROM users WHERE username = ?");
    $stmt->execute([$username]);
    $user = $stmt->fetch(PDO::FETCH_ASSOC);
    
    if ($user && password_verify($password, $user['password'])) {
        // Login successful
        $_SESSION['user_id'] = $user['user_id'];
        $_SESSION['username'] = $username;
        $_SESSION['first_name'] = $user['first_name'];
        $_SESSION['last_name'] = $user['last_name'];
        
        // Redirect to home page
        header("Location: index.php");
        exit();
    } else {
        $login_error = "Invalid username or password";
    }
}

// Check if registration was successful
$just_registered = isset($_GET['registered']) && $_GET['registered'] === 'true';
?>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Air DS - Login/Register</title>
    <link rel="stylesheet" href="styles.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0-beta3/css/all.min.css">
</head>
<body>
    <?php include('header.php'); ?>

    <main>
        <div class="auth-container">
            <div id="login-form" class="form-container" style="<?= $just_registered ? 'display: none;' : '' ?>">
                <h2 class="form-title">Login</h2>
                <?php if ($login_error): ?>
                    <div style="color: #cc0000; margin-bottom: 1rem;"><?= $login_error ?></div>
                <?php endif; ?>
                <?php if ($just_registered): ?>
                    <div style="color: #008000; margin-bottom: 1rem;">Registration successful! You can now login.</div>
                <?php endif; ?>
                <form method="post" action="">
                    <div class="form-group">
                        <label for="login_username">Username</label>
                        <input type="text" id="login_username" name="login_username" required>
                    </div>
                    <div class="form-group">
                        <label for="login_password">Password</label>
                        <input type="password" id="login_password" name="login_password" required>
                    </div>
                    <button type="submit" name="login" class="button">Login</button>
                </form>
                <p style="margin-top: 1rem;">Don't have an account? <a href="#" id="show-register">Register now</a>.</p>
            </div>
            
            <div id="register-form" class="form-container" style="<?= $just_registered ? '' : 'display: none;' ?>">
                <h2 class="form-title">Register</h2>
                <?php if ($registration_error): ?>
                    <div style="color: #cc0000; margin-bottom: 1rem;"><?= $registration_error ?></div>
                <?php endif; ?>
                <form method="post" action="" id="registrationForm">
                    <div class="form-group">
                        <label for="first_name">First Name</label>
                        <input type="text" id="first_name" name="first_name" required pattern="[A-Za-z]+" title="Only letters allowed">
                    </div>
                    <div class="form-group">
                        <label for="last_name">Last Name</label>
                        <input type="text" id="last_name" name="last_name" required pattern="[A-Za-z]+" title="Only letters allowed">
                    </div>
                    <div class="form-group">
                        <label for="username">Username</label>
                        <input type="text" id="username" name="username" required>
                    </div>
                    <div class="form-group">
                        <label for="password">Password</label>
                        <input type="password" id="password" name="password" required minlength="4" maxlength="10">
                        <small>Password must be between 4 and 10 characters and contain at least one number</small>
                    </div>
                    <div class="form-group">
                        <label for="email">Email</label>
                        <input type="email" id="email" name="email" required>
                    </div>
                    <button type="submit" name="register" class="button">Register</button>
                </form>
                <p style="margin-top: 1rem;">Already have an account? <a href="#" id="show-login">Login</a>.</p>
            </div>
        </div>
    </main>

    <?php include('footer.php'); ?>

    <script>
        // Toggle between login and register forms
        document.getElementById('show-register').addEventListener('click', function(e) {
            e.preventDefault();
            document.getElementById('login-form').style.display = 'none';
            document.getElementById('register-form').style.display = 'block';
        });

        document.getElementById('show-login').addEventListener('click', function(e) {
            e.preventDefault();
            document.getElementById('register-form').style.display = 'none';
            document.getElementById('login-form').style.display = 'block';
        });

        // Form validation
        document.getElementById('registrationForm').addEventListener('submit', function(e) {
            const password = document.getElementById('password').value;
            
            // Check if password contains at least one number
            if (!/\d/.test(password)) {
                e.preventDefault();
                alert('Password must contain at least one number');
            }
        });
        
        // Toggle mobile menu
        document.querySelector('.hamburger').addEventListener('click', function() {
            document.querySelector('.nav-links').classList.toggle('show');
        });
    </script>
</body>
</html>