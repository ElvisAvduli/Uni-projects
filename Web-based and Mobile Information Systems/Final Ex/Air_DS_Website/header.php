<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Flight Booker</title>
    <link rel="stylesheet" href="/styles.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css">
</head>
<body>
    <header class="site-header">
        <div class="container">
            <nav class="navbar">
            <a href="index.php" class="logo">Air DS</a>
            <button class="hamburger"><i class="fas fa-bars"></i></button>
            <ul class="nav-links">
                <li><a href="index.php">Home</a></li>
                <li><a href="my_trips.php">My Trips</a></li>
                <?php if(isset($_SESSION['user_id'])): ?>
                    <li><a href="logout.php">Logout</a></li>
                <?php else: ?>
                    <li><a href="login.php">Login</a></li>
                <?php endif; ?>
            </ul>
            </nav>
        </div>
    </header>