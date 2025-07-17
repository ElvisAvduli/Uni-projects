<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Modern Login Form</title>
    <style>
        :root {
            --primary-color: #4361ee;
            --secondary-color: #3f37c9;
            --accent-color: #4895ef;
            --light-color: #f8f9fa;
            --dark-color: #212529;
            --success-color: #4cc9f0;
            --error-color: #f72585;
        }
        
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
        }
        
        body {
            background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
            min-height: 100vh;
            display: flex;
            justify-content: center;
            align-items: center;
            padding: 20px;
        }
        
        .login-container {
            background: white;
            border-radius: 15px;
            box-shadow: 0 15px 35px rgba(0, 0, 0, 0.1);
            width: 100%;
            max-width: 450px;
            overflow: hidden;
            transition: all 0.3s ease;
        }
        
        .login-header {
            background: var(--primary-color);
            color: white;
            padding: 25px;
            text-align: center;
        }
        
        .login-header h2 {
            font-weight: 600;
            font-size: 1.8rem;
        }
        
        .login-form {
            padding: 30px;
        }
        
        .form-group {
            margin-bottom: 20px;
            position: relative;
        }
        
        .form-group label {
            display: block;
            margin-bottom: 8px;
            color: var(--dark-color);
            font-weight: 500;
        }
        
        .form-control {
            width: 100%;
            padding: 12px 15px;
            border: 2px solid #e9ecef;
            border-radius: 8px;
            font-size: 1rem;
            transition: all 0.3s;
        }
        
        .form-control:focus {
            border-color: var(--accent-color);
            outline: none;
            box-shadow: 0 0 0 3px rgba(67, 97, 238, 0.2);
        }
        
        .remember-me {
            display: flex;
            align-items: center;
            margin-bottom: 20px;
        }
        
        .remember-me input {
            margin-right: 10px;
        }
        
        .btn {
            display: inline-block;
            width: 100%;
            padding: 12px;
            background: var(--primary-color);
            color: white;
            border: none;
            border-radius: 8px;
            font-size: 1rem;
            font-weight: 500;
            cursor: pointer;
            transition: all 0.3s;
        }
        
        .btn:hover {
            background: var(--secondary-color);
            transform: translateY(-2px);
        }
        
        .message {
            padding: 15px;
            border-radius: 8px;
            margin-bottom: 20px;
            text-align: center;
        }
        
        .success-message {
            background-color: rgba(76, 201, 240, 0.2);
            color: var(--success-color);
        }
        
        .error-message {
            background-color: rgba(247, 37, 133, 0.2);
            color: var(--error-color);
        }
        
        .login-footer {
            text-align: center;
            margin-top: 20px;
            color: #6c757d;
        }
        
        .login-footer a {
            color: var(--primary-color);
            text-decoration: none;
            font-weight: 500;
        }
        
        @media (max-width: 576px) {
            .login-container {
                border-radius: 10px;
            }
            
            .login-header {
                padding: 20px;
            }
            
            .login-form {
                padding: 20px;
            }
        }
    </style>
</head>
<body>
    <div class="login-container">
        <div class="login-header">
            <h2>Welcome Back</h2>
        </div>
        
        <form class="login-form" action="process_login.php" method="post">
            <?php if (isset($_GET['error'])): ?>
                <div class="message error-message">
                    <?php echo htmlspecialchars($_GET['error']); ?>
                </div>
            <?php endif; ?>
            
            <?php if (isset($_GET['message'])): ?>
                <div class="message success-message">
                    <?php echo htmlspecialchars($_GET['message']); ?>
                </div>
            <?php endif; ?>
            
            <div class="form-group">
                <label for="username">Username</label>
                <input type="text" id="username" name="username" class="form-control" 
                       value="<?php echo htmlspecialchars($username ?? ''); ?>" 
                       placeholder="Enter your username">
            </div>
            
            <div class="form-group">
                <label for="password">Password</label>
                <input type="password" id="password" name="password" class="form-control" 
                       value="<?php echo htmlspecialchars($password ?? ''); ?>" 
                       placeholder="Enter your password">
            </div>
            
            <div class="remember-me">
                <input type="checkbox" id="remember" name="remember" <?php echo $remember ?? ''; ?>>
                <label for="remember">Remember me</label>
            </div>
            
            <button type="submit" class="btn">Login</button>
            
            <div class="login-footer">
                <p>Don't have an account? <a href="#">Sign up</a></p>
            </div>
        </form>
    </div>
</body>
</html>